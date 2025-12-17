import React, { createContext, useContext, useState, useEffect, useCallback, ReactNode } from 'react';
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

  // Poll for messages in all rooms (except the active one, which is handled by ChatRoom)
  useEffect(() => {
    const pollMessages = async () => {
      for (const room of rooms) {
        // Skip the currently active room - ChatRoom component handles that
        if (room.id === currentRoomId) {
          continue;
        }

        try {
          const messages = await messageApi.getMessages(room.id);
          if (messages && messages.length > 0) {
            setRoomMessages((prev) => {
              const existing = prev[room.id] || [];
              const latestMessage = messages[messages.length - 1];
              
              // Check if we have a new message
              if (!existing.some((m) => m.id === latestMessage.id)) {
                // Increment unread count for new message
                setUnreadCounts((prevCounts) => ({
                  ...prevCounts,
                  [room.id]: (prevCounts[room.id] || 0) + 1,
                }));
                
                return {
                  ...prev,
                  [room.id]: [...existing, latestMessage].sort((a, b) => 
                    new Date(a.timeSent).getTime() - new Date(b.timeSent).getTime()
                  ),
                };
              }
              
              // Update roomMessages even if no new message (to keep it in sync)
              return {
                ...prev,
                [room.id]: messages,
              };
            });
          }
        } catch (err) {
          console.error(`Failed to poll messages for room ${room.id}:`, err);
        }
      }
    };

    // Poll every 3 seconds
    const pollInterval = setInterval(pollMessages, 3000);
    
    // Initial poll
    pollMessages();

    return () => {
      clearInterval(pollInterval);
    };
  }, [rooms, currentRoomId]);

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

