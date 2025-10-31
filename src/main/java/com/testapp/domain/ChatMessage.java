package com.testapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessage {

    private Long userId;
    private String message;
    private Long chatRoomId;

    @Override
    public String toString() {
        return "ChatMessage{" +
                "user=" + userId +
                ", message='" + message + '\'' +
                ", chatRoom=" + chatRoomId +
                '}';
    }
}
