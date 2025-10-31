package com.testapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class ChatRoom {

    @JsonValue
    private Long id;
    private String displayName;
    private String description;
    private List<User> participants;
    private List<ChatMessage> chatHistory;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatRoom chatRoom = (ChatRoom) o;
        return Objects.equals(id, chatRoom.id) && Objects.equals(displayName, chatRoom.displayName) && Objects.equals(description, chatRoom.description) && Objects.equals(participants, chatRoom.participants) && Objects.equals(chatHistory, chatRoom.chatHistory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, displayName, description, participants, chatHistory);
    }

    @Override
    public String toString() {
        return "ChatRoom{" +
                "id=" + id +
                ", displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", participants=" + participants +
                ", chatHistory=" + chatHistory +
                '}';
    }
}
