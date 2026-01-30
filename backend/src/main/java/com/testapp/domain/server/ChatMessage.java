package com.testapp.domain.server;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {

    private String username;
    private String message;
    private String chatRoomName;
    private Instant timeSent;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatMessage that = (ChatMessage) o;
        return Objects.equals(username, that.username) && Objects.equals(message, that.message) && Objects.equals(chatRoomName, that.chatRoomName) && Objects.equals(timeSent, that.timeSent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, message, chatRoomName, timeSent);
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "username='" + username + '\'' +
                ", message='" + message + '\'' +
                ", chatRoomName='" + chatRoomName + '\'' +
                ", timeSent=" + timeSent +
                '}';
    }
}
