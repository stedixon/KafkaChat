import React, { useState, useEffect, useRef } from 'react';
import { messageApi, chatRoomApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { useMessageNotifications } from '../context/MessageNotificationContext';
import AddUserModal from './AddUserModal';
import type { ChatRoom as ChatRoomType, ChatMessage } from '../types';

interface ChatRoomProps {
  room: ChatRoomType;
  onBack: () => void;
}

const ChatRoom: React.FC<ChatRoomProps> = ({ room, onBack }) => {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [newMessage, setNewMessage] = useState('');
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [showAddUserModal, setShowAddUserModal] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const wsRef = useRef<WebSocket | null>(null);
  const reconnectTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const { user } = useAuth();
  const { resetUnreadCount, addMessageToRoom, roomMessages } = useMessageNotifications();

  useEffect(() => {
    loadMessages();
    resetUnreadCount(room.id); // Reset unread count when room is opened
    
    // Connect WebSocket after a small delay to ensure room is loaded
    const wsTimeout = setTimeout(() => {
      connectWebSocket();
    }, 100);

    return () => {
      clearTimeout(wsTimeout);
      if (reconnectTimeoutRef.current) {
        clearTimeout(reconnectTimeoutRef.current);
      }
      if (wsRef.current) {
        wsRef.current.close();
        wsRef.current = null;
      }
    };
  }, [room.id]);

  // Also listen to roomMessages from notification context for the active room
  useEffect(() => {
    const contextMessages = roomMessages[room.id] || [];
    if (contextMessages.length > 0) {
      // Merge with current messages, avoiding duplicates
      setMessages((prev) => {
        const existingIds = new Set(prev.map(m => m.id));
        const newMessages = contextMessages.filter(m => !existingIds.has(m.id));
        if (newMessages.length > 0) {
          console.log(`[ChatRoom] Adding ${newMessages.length} new messages from context`);
          const merged = [...prev, ...newMessages].sort((a, b) => 
            new Date(a.timeSent).getTime() - new Date(b.timeSent).getTime()
          );
          return merged;
        }
        // If no new messages but context has more messages, sync to ensure we have all
        if (contextMessages.length > prev.length) {
          console.log(`[ChatRoom] Syncing messages from context (context: ${contextMessages.length}, current: ${prev.length})`);
          return contextMessages;
        }
        return prev;
      });
    }
  }, [roomMessages[room.id]?.length, room.id]); // Use length to trigger on new messages

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const loadMessages = async () => {
    try {
      setLoading(true);
      const data = await messageApi.getMessages(room.id);
      setMessages(data);
    } catch (err) {
      console.error('Failed to load messages:', err);
    } finally {
      setLoading(false);
    }
  };

  const connectWebSocket = () => {
    // Close existing connection if any
    if (wsRef.current) {
      if (wsRef.current.readyState === WebSocket.OPEN || wsRef.current.readyState === WebSocket.CONNECTING) {
        wsRef.current.close();
      }
      wsRef.current = null;
    }

    // Clear any pending reconnection
    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current);
      reconnectTimeoutRef.current = null;
    }

    // WebSocket URL - adjust based on your backend WebSocket endpoint
    const wsUrl = `ws://localhost:8080/server/message/${room.id}`;
    const websocket = new WebSocket(wsUrl);
    wsRef.current = websocket;

    websocket.onopen = () => {
      console.log(`[ChatRoom] WebSocket connected for room ${room.id}`);
      // Clear any pending reconnection on successful connection
      if (reconnectTimeoutRef.current) {
        clearTimeout(reconnectTimeoutRef.current);
        reconnectTimeoutRef.current = null;
      }
    };

    websocket.onmessage = async (event) => {
      try {
        const data = JSON.parse(event.data);
        console.log(`[ChatRoom] WebSocket message received:`, data);
        
        // Only process actual messages, not connection/disconnection messages
        if (data.message && data.chatRoomName === room.id && 
            data.message !== 'Connected' && data.message !== 'Disconnected') {
          console.log(`[ChatRoom] Processing new message for active room ${room.id}`);
          
          // Retry mechanism to fetch the message from API (in case of timing issues)
          const fetchWithRetry = async (retries = 3, delay = 200) => {
            for (let i = 0; i < retries; i++) {
              try {
                const messages = await messageApi.getMessages(room.id);
                console.log(`[ChatRoom] Fetched ${messages.length} messages from API (attempt ${i + 1})`);
                
                if (messages && messages.length > 0) {
                  const latestMessage = messages[messages.length - 1];
                  
                  // Check if this is actually a new message (compare with current messages)
                  setMessages((prev) => {
                    const existingIds = new Set(prev.map(m => m.id));
                    if (existingIds.has(latestMessage.id)) {
                      console.log(`[ChatRoom] Message ${latestMessage.id} already in list`);
                      return prev;
                    }
                    
                    console.log(`[ChatRoom] Adding new message ${latestMessage.id} to active room`);
                    const updated = [...prev, latestMessage].sort((a, b) => 
                      new Date(a.timeSent).getTime() - new Date(b.timeSent).getTime()
                    );
                    // Also add to notification context
                    addMessageToRoom(room.id, latestMessage);
                    return updated;
                  });
                  
                  return; // Success, exit retry loop
                }
              } catch (err) {
                console.error(`[ChatRoom] Failed to fetch messages (attempt ${i + 1}):`, err);
                if (i < retries - 1) {
                  await new Promise(resolve => setTimeout(resolve, delay));
                }
              }
            }
          };
          
          // Start fetching with retry
          fetchWithRetry();
        } else {
          console.log(`[ChatRoom] Ignoring message:`, data);
        }
      } catch (err) {
        console.error('[ChatRoom] Failed to parse WebSocket message:', err, event.data);
      }
    };

    websocket.onerror = (error) => {
      console.error(`[ChatRoom] WebSocket error for room ${room.id}:`, error);
    };

    websocket.onclose = (event) => {
      console.log(`[ChatRoom] WebSocket disconnected for room ${room.id}`, event.code, event.reason);
      
      // Only attempt to reconnect if the close was not intentional (not a normal close)
      // Code 1000 = normal closure, 1001 = going away, 1005 = no status code
      // Don't reconnect if we're cleaning up (wsRef.current is null means we intentionally closed it)
      if (wsRef.current !== null && event.code !== 1000) {
        console.log(`[ChatRoom] Attempting to reconnect WebSocket for room ${room.id} in 3 seconds...`);
        reconnectTimeoutRef.current = setTimeout(() => {
          console.log(`[ChatRoom] Reconnecting WebSocket for room ${room.id}...`);
          connectWebSocket();
        }, 3000);
      } else {
        wsRef.current = null;
      }
    };
  };

  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newMessage.trim() || sending) return;

    setSending(true);
    try {
      await messageApi.sendMessage(room.id, { message: newMessage });
      setNewMessage('');
      // Reload messages to get the latest
      await loadMessages();
    } catch (err) {
      console.error('Failed to send message:', err);
    } finally {
      setSending(false);
    }
  };

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const formatTime = (timeString: string) => {
    const date = new Date(timeString);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  if (loading && messages.length === 0) {
    return (
      <div style={styles.container}>
        <div style={styles.header}>
          <button onClick={onBack} style={styles.backButton}>← Back</button>
          <h2 style={styles.roomTitle}>{room.displayName}</h2>
        </div>
        <div style={styles.loading}>Loading messages...</div>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <button onClick={onBack} style={styles.backButton}>← Back</button>
        <div style={styles.headerContent}>
          <div>
            <h2 style={styles.roomTitle}>{room.displayName}</h2>
            {room.description && (
              <div style={styles.roomDescription}>{room.description}</div>
            )}
          </div>
          <button
            onClick={() => setShowAddUserModal(true)}
            style={styles.addUserButton}
            title="Add users to this room"
          >
            + Add Users
          </button>
        </div>
      </div>
      <div style={styles.messagesContainer}>
        {messages.map((message) => (
          <div
            key={message.id}
            style={{
              ...styles.message,
              ...(message.userId.id === user?.id ? styles.myMessage : {}),
            }}
          >
            <div style={styles.messageHeader}>
              <span style={styles.messageAuthor}>
                {message.userId.firstName} {message.userId.lastName}
              </span>
              <span style={styles.messageTime}>
                {formatTime(message.timeSent)}
              </span>
            </div>
            <div style={styles.messageText}>{message.message}</div>
          </div>
        ))}
        <div ref={messagesEndRef} />
      </div>
      <form onSubmit={handleSendMessage} style={styles.inputContainer}>
        <input
          type="text"
          value={newMessage}
          onChange={(e) => setNewMessage(e.target.value)}
          placeholder="Type a message..."
          style={styles.input}
          disabled={sending}
        />
        <button type="submit" disabled={sending || !newMessage.trim()} style={styles.sendButton}>
          {sending ? 'Sending...' : 'Send'}
        </button>
      </form>
      {showAddUserModal && (
        <AddUserModal
          roomId={room.id}
          onClose={() => setShowAddUserModal(false)}
          onUserAdded={() => {
            // Optionally refresh room data or show a success message
            setShowAddUserModal(false);
          }}
        />
      )}
    </div>
  );
};

const styles: { [key: string]: React.CSSProperties } = {
  container: {
    display: 'flex',
    flexDirection: 'column',
    height: '100vh',
    flex: 1,
  },
  header: {
    padding: '1rem',
    borderBottom: '1px solid #ddd',
    backgroundColor: 'white',
    display: 'flex',
    alignItems: 'center',
    gap: '1rem',
  },
  headerContent: {
    flex: 1,
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  addUserButton: {
    padding: '0.5rem 1rem',
    backgroundColor: '#28a745',
    color: 'white',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    fontSize: '0.875rem',
    fontWeight: '500',
  },
  backButton: {
    padding: '0.5rem 1rem',
    backgroundColor: '#6c757d',
    color: 'white',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    fontSize: '0.875rem',
  },
  roomTitle: {
    margin: 0,
    fontSize: '1.25rem',
    color: '#333',
  },
  roomDescription: {
    fontSize: '0.875rem',
    color: '#666',
    marginTop: '0.25rem',
  },
  messagesContainer: {
    flex: 1,
    overflowY: 'auto',
    padding: '1rem',
    backgroundColor: '#f9f9f9',
  },
  message: {
    marginBottom: '1rem',
    padding: '0.75rem',
    backgroundColor: 'white',
    borderRadius: '8px',
    maxWidth: '70%',
  },
  myMessage: {
    marginLeft: 'auto',
    backgroundColor: '#007bff',
    color: 'white',
  },
  messageHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    marginBottom: '0.5rem',
    fontSize: '0.875rem',
  },
  messageAuthor: {
    fontWeight: '600',
  },
  messageTime: {
    opacity: 0.7,
    fontSize: '0.75rem',
  },
  messageText: {
    wordWrap: 'break-word',
  },
  inputContainer: {
    display: 'flex',
    padding: '1rem',
    borderTop: '1px solid #ddd',
    backgroundColor: 'white',
    gap: '0.5rem',
  },
  input: {
    flex: 1,
    padding: '0.75rem',
    border: '1px solid #ddd',
    borderRadius: '4px',
    fontSize: '1rem',
  },
  sendButton: {
    padding: '0.75rem 1.5rem',
    backgroundColor: '#007bff',
    color: 'white',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    fontSize: '1rem',
  },
  loading: {
    flex: 1,
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    color: '#666',
  },
};

export default ChatRoom;

