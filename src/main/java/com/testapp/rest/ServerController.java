package com.testapp.rest;

import com.testapp.domain.server.ChatMessage;
import com.testapp.domain.server.MessageDecoder;
import com.testapp.domain.server.MessageEncoder;
import jakarta.websocket.EncodeException;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ServerEndpoint(value = "/server/message/{chatRoom}",
        decoders = MessageDecoder.class,
        encoders = MessageEncoder.class )
public class ServerController {

    private Session session;
    private static Set<ServerController> chatEndpoints
            = new CopyOnWriteArraySet<>();
    private static HashMap<String, String> rooms = new HashMap<>();

    @OnOpen
    public void onOpen(
            Session session,
            @PathParam("chatRoom") String chatRoom) throws IOException, EncodeException {

        this.session = session;
        chatEndpoints.add(this);
        rooms.put(session.getId(), chatRoom);

        ChatMessage message = ChatMessage.builder()
                .chatRoomName(chatRoom)
                .message("Connected")
                .build();

        broadcast(message);
    }

    @OnMessage
    public void onMessage(Session session, ChatMessage message)
            throws IOException, EncodeException {

        message.setChatRoomName(rooms.get(session.getId()));
        broadcast(message);
    }

    @OnClose
    public void onClose(Session session) throws IOException, EncodeException {

        chatEndpoints.remove(this);
        ChatMessage message = ChatMessage.builder()
                .chatRoomName(rooms.get(session.getId()))
                .message("Disconnected")
                .build();

        broadcast(message);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
    }

    private static void broadcast(ChatMessage message)
            throws IOException, EncodeException {

        chatEndpoints.forEach(endpoint -> {
            synchronized (endpoint) {
                try {
                    // Only broadcast to endpoints in the same room
                    String endpointRoom = rooms.get(endpoint.session.getId());
                    if (endpointRoom != null && endpointRoom.equals(message.getChatRoomName())) {
                        endpoint.session.getBasicRemote().
                                sendObject(message);
                    }
                } catch (IOException | EncodeException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Public method to broadcast messages from Kafka consumer
    public static void broadcastMessage(ChatMessage message) {
        try {
            broadcast(message);
        } catch (IOException | EncodeException e) {
            e.printStackTrace();
        }
    }
}
