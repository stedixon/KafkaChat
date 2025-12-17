import React, { useState, useEffect, useRef } from 'react';
import { messageApi } from '../services/api';
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
  const { user } = useAuth();
  const { resetUnreadCount } = useMessageNotifications();

  useEffect(() => {
    loadMessages();
    resetUnreadCount(room.id); // Reset unread count when room is opened
    
    // Poll for new messages every 2 seconds
    const pollInterval = setInterval(() => {
      loadMessages();
    }, 1000);

    return () => {
      clearInterval(pollInterval);
    };
  }, [room.id]);

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

