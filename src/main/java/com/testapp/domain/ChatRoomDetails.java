package com.testapp.domain;

import com.testapp.domain.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoomDetails {

    private String id;
    private String displayName;
    private String description;
    private UserDTO admin;
    private int participantCount;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatRoomDetails that = (ChatRoomDetails) o;
        return participantCount == that.participantCount && Objects.equals(id, that.id) && Objects.equals(displayName, that.displayName) && Objects.equals(description, that.description) && Objects.equals(admin, that.admin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, displayName, description, admin, participantCount);
    }

    @Override
    public String toString() {
        return "ChatRoomDetails{" +
                "id='" + id + '\'' +
                ", displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", admin=" + admin +
                ", participantCount=" + participantCount +
                '}';
    }
}
