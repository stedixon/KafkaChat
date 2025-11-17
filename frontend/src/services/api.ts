import axios from 'axios';
import type { 
  User, 
  LoginRequest, 
  RegisterRequest, 
  LoginResponse, 
  ChatRoom, 
  ChatRoomCreateRequest,
  ChatMessage,
  ChatMessageSendRequest
} from '../types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle 401 errors (unauthorized)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const authApi = {
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const response = await api.post<LoginResponse>('/auth/login', credentials);
    return response.data;
  },

  register: async (userData: RegisterRequest): Promise<User> => {
    const response = await api.post<User>('/auth/signup', userData);
    return response.data;
  },
};

export const userApi = {
  getMe: async (): Promise<User> => {
    const response = await api.get<User>('/user/me');
    return response.data;
  },

  getAllUsers: async (): Promise<User[]> => {
    const response = await api.get<User[]>('/user/all');
    return response.data;
  },

  getUsersInChatRoom: async (chatRoomId: string): Promise<User[]> => {
    const response = await api.get<User[]>(`/user/chatRoom/${chatRoomId}`);
    return response.data;
  },
};

export const chatRoomApi = {
  getMyRooms: async (): Promise<ChatRoom[]> => {
    const response = await api.get<ChatRoom[]>('/chatRoom/myRooms');
    return response.data;
  },

  getRoom: async (id: string): Promise<ChatRoom> => {
    const response = await api.get<ChatRoom>(`/chatRoom/id/${id}`);
    return response.data;
  },

  createRoom: async (roomData: ChatRoomCreateRequest): Promise<ChatRoom> => {
    const response = await api.post<ChatRoom>('/chatRoom/create', roomData);
    return response.data;
  },

  joinRoom: async (roomId: string): Promise<void> => {
    await api.put(`/chatRoom/join/${roomId}`);
  },

  addUserToRoom: async (roomId: string, userId: string): Promise<void> => {
    await api.put(`/chatRoom/join/roomId/${roomId}/userId/${userId}`);
  },
};

export const messageApi = {
  getMessages: async (chatRoomId: string): Promise<ChatMessage[]> => {
    const response = await api.get<ChatMessage[]>(`/message/chatRoom/${chatRoomId}`);
    return response.data;
  },

  sendMessage: async (chatRoomId: string, message: ChatMessageSendRequest): Promise<ChatMessage> => {
    const response = await api.post<ChatMessage>(`/message/chatRoom/${chatRoomId}`, message);
    return response.data;
  },
};

export default api;

