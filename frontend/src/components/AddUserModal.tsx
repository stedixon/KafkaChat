import React, { useState, useEffect } from 'react';
import { userApi, chatRoomApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import type { User } from '../types';

interface AddUserModalProps {
  roomId: string;
  onClose: () => void;
  onUserAdded: () => void;
}

const AddUserModal: React.FC<AddUserModalProps> = ({ roomId, onClose, onUserAdded }) => {
  const [allUsers, setAllUsers] = useState<User[]>([]);
  const [roomUsers, setRoomUsers] = useState<User[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);
  const [adding, setAdding] = useState<string | null>(null);
  const [error, setError] = useState<string>('');
  const { user: currentUser } = useAuth();

  useEffect(() => {
    loadData();
  }, [roomId]);

  const loadData = async () => {
    try {
      setLoading(true);
      const [allUsersData, roomUsersData] = await Promise.all([
        userApi.getAllUsers(),
        userApi.getUsersInChatRoom(roomId),
      ]);
      setAllUsers(allUsersData);
      setRoomUsers(roomUsersData);
      setError('');
    } catch (err: any) {
      setError('Failed to load users');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleAddUser = async (userId: string) => {
    setAdding(userId);
    setError('');
    try {
      await chatRoomApi.addUserToRoom(roomId, userId);
      await loadData(); // Reload to get updated user list
      onUserAdded();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to add user to room');
    } finally {
      setAdding(null);
    }
  };

  // Filter users: exclude current user and users already in the room
  const roomUserIds = new Set(roomUsers.map(u => u.id));
  const availableUsers = allUsers.filter(
    (user) =>
      user.id !== currentUser?.id &&
      !roomUserIds.has(user.id) &&
      (searchTerm === '' ||
        user.username.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.firstName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.lastName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.email.toLowerCase().includes(searchTerm.toLowerCase()))
  );

  return (
    <div style={styles.overlay} onClick={onClose}>
      <div style={styles.modal} onClick={(e) => e.stopPropagation()}>
        <div style={styles.header}>
          <h2 style={styles.title}>Add Users to Chat Room</h2>
          <button onClick={onClose} style={styles.closeButton}>×</button>
        </div>

        {error && <div style={styles.error}>{error}</div>}

        <div style={styles.searchContainer}>
          <input
            type="text"
            placeholder="Search users by name, username, or email..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={styles.searchInput}
          />
        </div>

        {loading ? (
          <div style={styles.loading}>Loading users...</div>
        ) : (
          <>
            <div style={styles.section}>
              <h3 style={styles.sectionTitle}>Current Members ({roomUsers.length})</h3>
              <div style={styles.userList}>
                {roomUsers.map((user) => (
                  <div key={user.id} style={styles.userItem}>
                    <div>
                      <div style={styles.userName}>
                        {user.firstName} {user.lastName}
                        {user.id === currentUser?.id && <span style={styles.youLabel}> (You)</span>}
                      </div>
                      <div style={styles.userDetails}>
                        @{user.username} • {user.email}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div style={styles.section}>
              <h3 style={styles.sectionTitle}>
                Available Users ({availableUsers.length})
              </h3>
              {availableUsers.length === 0 ? (
                <div style={styles.empty}>
                  {searchTerm
                    ? 'No users found matching your search'
                    : 'All users are already in this room'}
                </div>
              ) : (
                <div style={styles.userList}>
                  {availableUsers.map((user) => (
                    <div key={user.id} style={styles.userItem}>
                      <div style={styles.userInfo}>
                        <div style={styles.userName}>
                          {user.firstName} {user.lastName}
                        </div>
                        <div style={styles.userDetails}>
                          @{user.username} • {user.email}
                        </div>
                      </div>
                      <button
                        onClick={() => handleAddUser(user.id)}
                        disabled={adding === user.id}
                        style={{
                          ...styles.addButton,
                          ...(adding === user.id ? styles.addButtonDisabled : {}),
                        }}
                      >
                        {adding === user.id ? 'Adding...' : 'Add'}
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </>
        )}
      </div>
    </div>
  );
};

const styles: { [key: string]: React.CSSProperties } = {
  overlay: {
    position: 'fixed',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: 1000,
  },
  modal: {
    backgroundColor: 'white',
    borderRadius: '8px',
    width: '90%',
    maxWidth: '600px',
    maxHeight: '80vh',
    display: 'flex',
    flexDirection: 'column',
    boxShadow: '0 4px 20px rgba(0, 0, 0, 0.2)',
  },
  header: {
    padding: '1.5rem',
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
  closeButton: {
    background: 'none',
    border: 'none',
    fontSize: '2rem',
    color: '#666',
    cursor: 'pointer',
    padding: 0,
    width: '2rem',
    height: '2rem',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    lineHeight: 1,
  },
  error: {
    margin: '1rem 1.5rem',
    padding: '0.75rem',
    backgroundColor: '#fee',
    color: '#c33',
    borderRadius: '4px',
  },
  searchContainer: {
    padding: '1rem 1.5rem',
    borderBottom: '1px solid #ddd',
  },
  searchInput: {
    width: '100%',
    padding: '0.75rem',
    border: '1px solid #ddd',
    borderRadius: '4px',
    fontSize: '1rem',
    boxSizing: 'border-box',
  },
  loading: {
    padding: '2rem',
    textAlign: 'center',
    color: '#666',
  },
  section: {
    padding: '1rem 1.5rem',
    borderBottom: '1px solid #eee',
  },
  sectionTitle: {
    margin: '0 0 1rem 0',
    fontSize: '1rem',
    color: '#555',
    fontWeight: '600',
  },
  userList: {
    maxHeight: '200px',
    overflowY: 'auto',
  },
  userItem: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: '0.75rem',
    borderBottom: '1px solid #f0f0f0',
  },
  userInfo: {
    flex: 1,
  },
  userName: {
    fontWeight: '600',
    color: '#333',
    marginBottom: '0.25rem',
  },
  userDetails: {
    fontSize: '0.875rem',
    color: '#666',
  },
  youLabel: {
    color: '#007bff',
    fontWeight: 'normal',
  },
  addButton: {
    padding: '0.5rem 1rem',
    backgroundColor: '#007bff',
    color: 'white',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    fontSize: '0.875rem',
  },
  addButtonDisabled: {
    backgroundColor: '#ccc',
    cursor: 'not-allowed',
  },
  empty: {
    padding: '1rem',
    textAlign: 'center',
    color: '#666',
    fontStyle: 'italic',
  },
};

export default AddUserModal;

