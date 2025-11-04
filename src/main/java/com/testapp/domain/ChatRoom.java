package com.testapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Objects;

@Data
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "chat_room")
public class ChatRoom implements Serializable {

    @Id
    private String id;
    @NonNull
    @Column(name = "display_name")
    private String displayName;
    private String description;
    @ManyToOne
    @JoinColumn(name = "admin_id")
    private User admin;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatRoom chatRoom = (ChatRoom) o;
        return Objects.equals(id, chatRoom.id) && Objects.equals(displayName, chatRoom.displayName) && Objects.equals(description, chatRoom.description);
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
