import React, { createContext, useContext, useState, useEffect, useCallback, useRef, ReactNode } from 'react';
import { useAuth } from './AuthContext';
import { messageApi } from '../services/api';
import type { ChatMessage, ChatRoom } from '../types';

interface MessageNotificationContextType {
  unreadCounts: Record<string, number>;
  incrementUnreadCount: (roomId: string) => void;
  resetUnreadCount: (roomId: string) => void;
  addMessageToRoom: (roomId: string, message: ChatMessage) => void;
  roomMessages: Record<string, ChatMessage[]>;
  getLatestMessage: (roomId: string) => ChatMessage | null;
}

const MessageNotificationContext = createContext<MessageNotificationContextType | undefined>(undefined);

export const useMessageNotifications = () => {
  const context = useContext(MessageNotificationContext);
  if (!context) {
    throw new Error('useMessageNotifications must be used within a MessageNotificationProvider');
  }
  return context;
};

interface MessageNotificationProviderProps {
  children: ReactNode;
  rooms: ChatRoom[];
  currentRoomId: string | null;
}

export const MessageNotificationProvider: React.FC<MessageNotificationProviderProps> = ({
  children,
  rooms,
  currentRoomId,
}) => {
  const [unreadCounts, setUnreadCounts] = useState<Record<string, number>>({});
  const [roomMessages, setRoomMessages] = useState<Record<string, ChatMessage[]>>({});
  const websocketsRef = useRef<Record<string, WebSocket>>({});
  const { user } = useAuth();

  // Initialize WebSocket connections for all rooms
  useEffect(() => {
    const wsConnections: Record<string, WebSocket> = {};
    const reconnectTimeouts: Record<string, NodeJS.Timeout> = {};

    const connectWebSocketForRoom = (room: ChatRoom) => {
      // Skip if already connected or connecting
      if (websocketsRef.current[room.id]) {
        const currentState = websocketsRef.current[room.id].readyState;
        if (currentState === WebSocket.OPEN || currentState === WebSocket.CONNECTING) {
          return;
        }
        // If closed or closing, clean up first
        if (currentState === WebSocket.CLOSED || currentState === WebSocket.CLOSING) {
          delete websocketsRef.current[room.id];
        }
      }

      // Clear any pending reconnection for this room
      if (reconnectTimeouts[room.id]) {
        clearTimeout(reconnectTimeouts[room.id]);
        delete reconnectTimeouts[room.id];
      }

      const wsUrl = `ws://localhost:8080/server/message/${room.id}`;
      const websocket = new WebSocket(wsUrl);

      // Add connection timeout
      const connectionTimeout = setTimeout(() => {
        if (websocket.readyState === WebSocket.CONNECTING) {
          console.error(`WebSocket connection timeout for room ${room.id}`);
          websocket.close();
        }
      }, 5000); // 5 second timeout

      websocket.onopen = () => {
        clearTimeout(connectionTimeout);
        console.log(`WebSocket connected for room ${room.id}`);
        // Clear any pending reconnection on successful connection
        if (reconnectTimeouts[room.id]) {
          clearTimeout(reconnectTimeouts[room.id]);
          delete reconnectTimeouts[room.id];
        }
      };

      websocket.onmessage = async (event) => {
        try {
          const data = JSON.parse(event.data);
          console.log(`WebSocket message received for room ${room.id}:`, data);
          
          // Check if this is a real message (not just connection/disconnection)
          if (data.message && data.chatRoomName === room.id && 
              data.message !== 'Connected' && data.message !== 'Disconnected') {
            console.log(`Processing new message for room ${room.id}`);
            
            // Retry mechanism to handle timing issues with database saves
            const fetchWithRetry = async (retries = 3, delay = 200) => {
              for (let i = 0; i < retries; i++) {
                try {
                  // Fetch the full message details from the API to get the complete message object
                  const messages = await messageApi.getMessages(room.id);
                  if (messages && messages.length > 0) {
                    const latestMessage = messages[messages.length - 1];
                    console.log(`Latest message from API (attempt ${i + 1}):`, latestMessage);
                    
                    // Check if we already have this message
                    setRoomMessages((prev) => {
                      const existing = prev[room.id] || [];
                      if (existing.some((m) => m.id === latestMessage.id)) {
                        console.log(`Message ${latestMessage.id} already exists, skipping`);
                        return prev;
                      }
                      console.log(`Adding new message ${latestMessage.id} to room ${room.id}`);
                      // Always add to roomMessages, even if room is active (ChatRoom component will pick it up)
                      return {
                        ...prev,
                        [room.id]: [...existing, latestMessage].sort((a, b) => 
                          new Date(a.timeSent).getTime() - new Date(b.timeSent).getTime()
                        ),
                      };
                    });
                    
                    // Increment unread count if room is not currently active
                    if (currentRoomId !== room.id) {
                      console.log(`Incrementing unread count for room ${room.id}`);
                      setUnreadCounts((prev) => ({
                        ...prev,
                        [room.id]: (prev[room.id] || 0) + 1,
                      }));
                    } else {
                      console.log(`Room ${room.id} is active, not incrementing unread count (but message added to roomMessages)`);
                    }
                    
                    return; // Success, exit retry loop
                  }
                } catch (err) {
                  console.error(`Failed to fetch messages (attempt ${i + 1}):`, err);
                  if (i < retries - 1) {
                    await new Promise(resolve => setTimeout(resolve, delay));
                  }
                }
              }
            };
            
            // Start fetching with retry
            fetchWithRetry();
          } else {
            console.log(`Ignoring message (connection/disconnection or wrong room):`, data);
          }
        } catch (err) {
          console.error('Failed to parse WebSocket message:', err, event.data);
        }
      };

      websocket.onerror = (error) => {
        console.error(`WebSocket error for room ${room.id}:`, error);
      };

      websocket.onclose = (event) => {
        console.log(`WebSocket disconnected for room ${room.id}`, event.code, event.reason);
        
        // Only attempt to reconnect if the close was not intentional
        // Code 1000 = normal closure
        // Don't reconnect if the room is no longer in the list
        const roomStillExists = rooms.some(r => r.id === room.id);
        if (roomStillExists && event.code !== 1000) {
          console.log(`Attempting to reconnect WebSocket for room ${room.id} in 3 seconds...`);
          reconnectTimeouts[room.id] = setTimeout(() => {
            // Double-check the room still exists before reconnecting
            const currentRooms = rooms; // Capture current rooms at reconnect time
            if (currentRooms.some(r => r.id === room.id)) {
              console.log(`Reconnecting WebSocket for room ${room.id}...`);
              connectWebSocketForRoom(room);
            } else {
              console.log(`Room ${room.id} no longer exists, skipping reconnection`);
            }
          }, 3000);
        }
        
        delete websocketsRef.current[room.id];
      };

      wsConnections[room.id] = websocket;
      websocketsRef.current[room.id] = websocket;
    };

    // Connect WebSocket for each room
    rooms.forEach((room) => {
      connectWebSocketForRoom(room);
    });

    // Close connections for rooms that are no longer in the list
    Object.keys(websocketsRef.current).forEach((roomId) => {
      if (!rooms.some((r) => r.id === roomId)) {
        const ws = websocketsRef.current[roomId];
        if (ws && ws.readyState === WebSocket.OPEN) {
          ws.close();
        }
        delete websocketsRef.current[roomId];
      }
    });

    // Cleanup on unmount or when dependencies change
    return () => {
      // Clear all reconnection timeouts
      Object.values(reconnectTimeouts).forEach(timeout => clearTimeout(timeout));
      
      // Close all connections created in this effect
      Object.values(wsConnections).forEach((ws) => {
        if (ws && (ws.readyState === WebSocket.OPEN || ws.readyState === WebSocket.CONNECTING)) {
          ws.close(1000, 'Component unmounting'); // Normal closure
        }
      });
    };
  }, [rooms.map(r => r.id).join(','), currentRoomId]); // Reconnect when room list changes

  const incrementUnreadCount = useCallback((roomId: string) => {
    setUnreadCounts((prev) => ({
      ...prev,
      [roomId]: (prev[roomId] || 0) + 1,
    }));
  }, []);

  const resetUnreadCount = useCallback((roomId: string) => {
    setUnreadCounts((prev) => {
      const newCounts = { ...prev };
      delete newCounts[roomId];
      return newCounts;
    });
  }, []);

  const addMessageToRoom = useCallback((roomId: string, message: ChatMessage) => {
    setRoomMessages((prev) => {
      const existingMessages = prev[roomId] || [];
      // Check if message already exists
      if (existingMessages.some((m) => m.id === message.id)) {
        return prev;
      }
      return {
        ...prev,
        [roomId]: [...existingMessages, message],
      };
    });
  }, []);

  // Reset unread count when room becomes active
  useEffect(() => {
    if (currentRoomId) {
      resetUnreadCount(currentRoomId);
    }
  }, [currentRoomId, resetUnreadCount]);

  const getLatestMessage = useCallback((roomId: string): ChatMessage | null => {
    const messages = roomMessages[roomId];
    return messages && messages.length > 0 ? messages[messages.length - 1] : null;
  }, [roomMessages]);

  return (
    <MessageNotificationContext.Provider
      value={{
        unreadCounts,
        incrementUnreadCount,
        resetUnreadCount,
        addMessageToRoom,
        roomMessages,
        getLatestMessage,
      }}
    >
      {children}
    </MessageNotificationContext.Provider>
  );
};

