package com.testapp.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "chat_room")
public class ChatRoomDTO implements Serializable {

    @Id
    @JoinColumn(name = "chat_room_id")
    private String id;
    @Column(name = "display_name")
    private String displayName;
    private String description;
    @ManyToOne
    @JoinColumn(name = "admin_id")
    private UserDTO admin;

    public ChatRoomDTO(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatRoomDTO chatRoomDTO = (ChatRoomDTO) o;
        return Objects.equals(id, chatRoomDTO.id) && Objects.equals(displayName, chatRoomDTO.displayName) && Objects.equals(description, chatRoomDTO.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, displayName, description);
    }

    @Override
    public String toString() {
        return "ChatRoom{" +
                "id=" + id +
                ", displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
