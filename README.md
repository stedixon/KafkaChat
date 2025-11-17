# Kafka Chat Application

A full-stack chat application with React TypeScript frontend and Spring Boot backend, using Kafka for message streaming.

## Project Structure

```
KafkaChat/
├── src/                    # Backend (Spring Boot)
│   └── main/
│       ├── java/
│       │   └── com/testapp/
│       │       ├── auth/           # JWT authentication
│       │       ├── config/         # Configuration
│       │       ├── domain/         # Domain models and DTOs
│       │       ├── kafka/          # Kafka producers/consumers
│       │       ├── repository/     # Data repositories
│       │       ├── rest/           # REST controllers
│       │       └── service/       # Business logic
│       └── resources/
│           └── application.yml
└── frontend/               # Frontend (React + TypeScript)
    ├── src/
    │   ├── components/     # React components
    │   ├── context/       # React context (Auth)
    │   ├── services/      # API services
    │   └── types/         # TypeScript types
    └── package.json
```

## Backend Setup

### Prerequisites
- Java 17+
- Maven
- MySQL
- Kafka

### Configuration

1. Set environment variables:
   - `SQL_USERNAME`: MySQL username
   - `SQL_PASSWORD`: MySQL password
   - `JWT_SECRET_KEY`: Secret key for JWT tokens

2. Start MySQL and Kafka (using Docker Compose):
   ```bash
   docker-compose up -d
   ```

3. Run the Spring Boot application:
   ```bash
   mvn spring-boot:run
   ```

The backend will run on `http://localhost:8080`

## Frontend Setup

### Prerequisites
- Node.js 18+
- npm or yarn

### Installation

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Create a `.env` file (optional, defaults to localhost:8080):
   ```
   VITE_API_BASE_URL=http://localhost:8080
   ```

4. Start the development server:
   ```bash
   npm run dev
   ```

The frontend will run on `http://localhost:3000`

## Features

### Backend
- JWT-based authentication
- User registration and login
- Chat room creation and management
- Real-time messaging via Kafka
- WebSocket support for live updates
- RESTful API endpoints

### Frontend
- User registration and login
- JWT token management
- Chat room list and creation
- Real-time messaging interface
- WebSocket integration for live updates
- Modern, responsive UI

## API Endpoints

### Authentication
- `POST /auth/signup` - Register a new user
- `POST /auth/login` - Login and get JWT token

### Users
- `GET /user/me` - Get current user info
- `GET /user/id/{id}` - Get user by ID
- `GET /user/chatRoom/{chatRoomId}` - Get users in a chat room

### Chat Rooms
- `GET /chatRoom/myRooms` - Get chat rooms for current user
- `GET /chatRoom/id/{id}` - Get chat room details
- `POST /chatRoom/create` - Create a new chat room
- `PUT /chatRoom/join/{roomId}` - Join a chat room (current user)

### Messages
- `GET /message/chatRoom/{chatRoomId}` - Get messages for a chat room
- `POST /message/chatRoom/{chatRoomId}` - Send a message to a chat room

### WebSocket
- `ws://localhost:8080/server/message/{chatRoom}` - WebSocket endpoint for real-time messaging

## Usage

1. Start the backend server
2. Start the frontend development server
3. Open `http://localhost:3000` in your browser
4. Register a new account or login
5. Create or join chat rooms
6. Start chatting!

## Technologies

### Backend
- Spring Boot 3.5.7
- Spring Security
- Spring Kafka
- JWT (JSON Web Tokens)
- MySQL
- Liquibase
- Jakarta WebSocket

### Frontend
- React 18
- TypeScript
- Vite
- React Router
- Axios
- WebSocket API

