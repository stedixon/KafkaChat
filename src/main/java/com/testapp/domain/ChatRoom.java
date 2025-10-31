package com.testapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Data
@Entity
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class ChatRoom implements Serializable {

    @Id
    @JoinColumn(name = "chat_room_id")
    private UUID id;
    private String displayName;
    private String description;
    @OneToMany(mappedBy = "id")
    private List<User> participants;
    @OneToMany(mappedBy = "id")
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
