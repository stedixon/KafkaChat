export interface User {
  id: string;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  firstName: string;
  lastName: string;
  email: string;
}

export interface LoginResponse {
  token: string;
  expiresIn: number;
}

export interface ChatRoom {
  id: string;
  displayName: string;
  description?: string;
  admin: User;
  participantCount: number;
}

export interface ChatRoomCreateRequest {
  displayName: string;
  description?: string;
}

export interface ChatMessage {
  id: string;
  userId: User;
  message: string;
  chatRoomDTO?: {
    id: string;
  };
  timeSent: string;
}

export interface ChatMessageSendRequest {
  message: string;
}

