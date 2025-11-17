import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { MessageNotificationProvider } from '../context/MessageNotificationContext';
import ChatRoomList from './ChatRoomList';
import ChatRoom from './ChatRoom';
import CreateRoomModal from './CreateRoomModal';
import { chatRoomApi } from '../services/api';
import type { ChatRoom as ChatRoomType } from '../types';

const Chat: React.FC = () => {
  const [selectedRoom, setSelectedRoom] = useState<ChatRoomType | null>(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [rooms, setRooms] = useState<ChatRoomType[]>([]);
  const { user, logout } = useAuth();

  useEffect(() => {
    loadRooms();
    // Refresh rooms periodically
    const interval = setInterval(loadRooms, 30000); // Every 30 seconds
    return () => clearInterval(interval);
  }, []);

  const loadRooms = async () => {
    try {
      const data = await chatRoomApi.getMyRooms();
      setRooms(data);
    } catch (err) {
      console.error('Failed to load rooms:', err);
    }
  };

  const handleRoomCreated = (room: ChatRoomType) => {
    setSelectedRoom(room);
    setShowCreateModal(false);
    loadRooms(); // Refresh room list
  };

  return (
    <MessageNotificationProvider rooms={rooms} currentRoomId={selectedRoom?.id || null}>
      <div style={styles.container}>
        <div style={styles.sidebar}>
          <div style={styles.userInfo}>
            <div style={styles.userName}>
              {user?.firstName} {user?.lastName}
            </div>
            <div style={styles.userEmail}>{user?.email}</div>
            <button onClick={logout} style={styles.logoutButton}>
              Logout
            </button>
          </div>
          <ChatRoomList
            onSelectRoom={setSelectedRoom}
            onCreateRoom={() => setShowCreateModal(true)}
            currentRoomId={selectedRoom?.id || null}
          />
        </div>
        <div style={styles.mainContent}>
          {selectedRoom ? (
            <ChatRoom room={selectedRoom} onBack={() => setSelectedRoom(null)} />
          ) : (
            <div style={styles.emptyState}>
              <h2>Select a chat room to start chatting</h2>
            </div>
          )}
        </div>
        {showCreateModal && (
          <CreateRoomModal
            onClose={() => setShowCreateModal(false)}
            onRoomCreated={handleRoomCreated}
          />
        )}
      </div>
    </MessageNotificationProvider>
  );
};

const styles: { [key: string]: React.CSSProperties } = {
  container: {
    display: 'flex',
    height: '100vh',
    overflow: 'hidden',
  },
  sidebar: {
    display: 'flex',
    flexDirection: 'column',
    width: '300px',
    borderRight: '1px solid #ddd',
  },
  userInfo: {
    padding: '1rem',
    borderBottom: '1px solid #ddd',
    backgroundColor: 'white',
  },
  userName: {
    fontWeight: '600',
    color: '#333',
    marginBottom: '0.25rem',
  },
  userEmail: {
    fontSize: '0.875rem',
    color: '#666',
    marginBottom: '0.5rem',
  },
  logoutButton: {
    padding: '0.5rem 1rem',
    backgroundColor: '#dc3545',
    color: 'white',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    fontSize: '0.875rem',
  },
  mainContent: {
    flex: 1,
    display: 'flex',
    flexDirection: 'column',
  },
  emptyState: {
    flex: 1,
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    color: '#666',
  },
};

export default Chat;

