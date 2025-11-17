import React, { useState, useEffect } from 'react';
import { chatRoomApi } from '../services/api';
import { useMessageNotifications } from '../context/MessageNotificationContext';
import type { ChatRoom } from '../types';

interface ChatRoomListProps {
  onSelectRoom: (room: ChatRoom) => void;
  onCreateRoom: () => void;
  currentRoomId: string | null;
}

const ChatRoomList: React.FC<ChatRoomListProps> = ({ onSelectRoom, onCreateRoom, currentRoomId }) => {
  const [rooms, setRooms] = useState<ChatRoom[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>('');
  const { unreadCounts, resetUnreadCount, getLatestMessage } = useMessageNotifications();

  useEffect(() => {
    loadRooms();
  }, []);

  const handleRoomSelect = (room: ChatRoom) => {
    resetUnreadCount(room.id);
    onSelectRoom(room);
  };

  const loadRooms = async () => {
    try {
      setLoading(true);
      const data = await chatRoomApi.getMyRooms();
      setRooms(data);
      setError('');
    } catch (err: any) {
      setError('Failed to load chat rooms');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div style={styles.loading}>Loading chat rooms...</div>;
  }

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <h2 style={styles.title}>Chat Rooms</h2>
        <button onClick={onCreateRoom} style={styles.createButton}>
          + Create Room
        </button>
      </div>
      {error && <div style={styles.error}>{error}</div>}
      {rooms.length === 0 ? (
        <div style={styles.empty}>No chat rooms yet. Create one to get started!</div>
      ) : (
        <div style={styles.roomList}>
          {rooms.map((room) => {
            const unreadCount = unreadCounts[room.id] || 0;
            const latestMessage = getLatestMessage(room.id);
            const isActive = currentRoomId === room.id;
            
            return (
              <div
                key={room.id}
                onClick={() => handleRoomSelect(room)}
                style={{
                  ...styles.roomItem,
                  ...(isActive ? styles.activeRoomItem : {}),
                }}
              >
                <div style={styles.roomHeader}>
                  <div style={styles.roomName}>{room.displayName}</div>
                  {unreadCount > 0 && (
                    <div style={styles.unreadBadge}>{unreadCount}</div>
                  )}
                </div>
                {latestMessage && !isActive && (
                  <div style={styles.latestMessage}>
                    {latestMessage.userId.firstName}: {latestMessage.message}
                  </div>
                )}
                {room.description && !latestMessage && (
                  <div style={styles.roomDescription}>{room.description}</div>
                )}
                <div style={styles.roomMeta}>
                  {room.participantCount} member{room.participantCount !== 1 ? 's' : ''}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

const styles: { [key: string]: React.CSSProperties } = {
  container: {
    width: '300px',
    height: '100vh',
    borderRight: '1px solid #ddd',
    display: 'flex',
    flexDirection: 'column',
    backgroundColor: '#f9f9f9',
  },
  header: {
    padding: '1rem',
    borderBottom: '1px solid #ddd',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  title: {
    margin: 0,
    fontSize: '1.25rem',
    color: '#333',
  },
  createButton: {
    padding: '0.5rem 1rem',
    backgroundColor: '#007bff',
    color: 'white',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    fontSize: '0.875rem',
  },
  loading: {
    padding: '2rem',
    textAlign: 'center',
    color: '#666',
  },
  error: {
    padding: '1rem',
    backgroundColor: '#fee',
    color: '#c33',
    margin: '1rem',
    borderRadius: '4px',
  },
  empty: {
    padding: '2rem',
    textAlign: 'center',
    color: '#666',
  },
  roomList: {
    flex: 1,
    overflowY: 'auto',
  },
  roomItem: {
    padding: '1rem',
    borderBottom: '1px solid #eee',
    cursor: 'pointer',
    transition: 'background-color 0.2s',
    position: 'relative',
  },
  activeRoomItem: {
    backgroundColor: '#e3f2fd',
    borderLeft: '3px solid #007bff',
  },
  roomHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '0.25rem',
  },
  unreadBadge: {
    backgroundColor: '#007bff',
    color: 'white',
    borderRadius: '12px',
    padding: '0.25rem 0.5rem',
    fontSize: '0.75rem',
    fontWeight: '600',
    minWidth: '20px',
    textAlign: 'center',
  },
  latestMessage: {
    fontSize: '0.875rem',
    color: '#666',
    marginBottom: '0.5rem',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    whiteSpace: 'nowrap',
  },
  roomName: {
    fontWeight: '600',
    color: '#333',
    marginBottom: '0.25rem',
  },
  roomDescription: {
    fontSize: '0.875rem',
    color: '#666',
    marginBottom: '0.5rem',
  },
  roomMeta: {
    fontSize: '0.75rem',
    color: '#999',
  },
};

export default ChatRoomList;

