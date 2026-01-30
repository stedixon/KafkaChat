package com.testapp.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.Serializable;
import java.time.Instant;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(name = "chat_message")
public class ChatMessageDTO implements Serializable {

    @Id
    @JoinColumn(name = "chat_message_id")
    private String id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserDTO userId;
    @NonNull
    private String message;
    @ManyToOne
    @JoinColumn(name = "chat_room_id")
    private ChatRoomDTO chatRoomDTO;
    private Instant timeSent;

    @Override
    public String toString() {
        return "ChatMessage{" +
                "user=" + userId +
                ", message='" + message + '\'' +
                ", chatRoom=" + chatRoomDTO +
                ", timeSent=" + timeSent +
                '}';
    }
}
