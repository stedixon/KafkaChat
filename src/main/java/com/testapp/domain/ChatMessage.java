package com.testapp.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessage {

    private User user;
    private String message;
    @JsonBackReference
    private ChatRoom chatRoom;

    @Override
    public String toString() {
        return "ChatMessage{" +
                "user=" + user +
                ", message='" + message + '\'' +
                ", chatRoom=" + chatRoom +
                '}';
    }
}
