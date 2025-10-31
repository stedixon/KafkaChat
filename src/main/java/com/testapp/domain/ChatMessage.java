package com.testapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessage implements Serializable {

    @Id
    @JoinColumn(name = "chat_message_id")
    private String id;
    private Long userId;
    private String message;
    private ChatRoom chatRoom;
    private Instant timeSent;

    @Override
    public String toString() {
        return "ChatMessage{" +
                "user=" + userId +
                ", message='" + message + '\'' +
                ", chatRoom=" + chatRoom +
                ", timeSent=" + timeSent +
                '}';
    }
}
