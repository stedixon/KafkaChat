package com.testapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
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
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(name = "room_management")
public class RoomManagement implements Serializable {

    @EmbeddedId
    private RoomManagementId id;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "chat_room_id", insertable = false, updatable = false)
    private ChatRoom chatRoom;

    @Column(name = "is_admin")
    private boolean isAdmin;

    public RoomManagement(User userId, ChatRoom chatRoomId) {
        this.user = userId;
        this.chatRoom = chatRoomId;
        this.isAdmin = false;
    }

    public RoomManagement(User userId, ChatRoom chatRoomId, boolean isAdmin) {
        this.user = userId;
        this.chatRoom = chatRoomId;
        this.isAdmin = isAdmin;
    }

    @Override
    public String toString() {
        return "RoomManagement{" +
                "userId=" + user +
                ", chatRoomId=" + chatRoom +
                '}';
    }

    @Data
    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoomManagementId implements Serializable {
        @Column(name = "user_id")
        private String userId;
        @Column(name = "chat_room_id")
        private String chatRoomId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RoomManagementId that = (RoomManagementId) o;
            return Objects.equals(userId, that.userId) && Objects.equals(chatRoomId, that.chatRoomId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, chatRoomId);
        }

        @Override
        public String toString() {
            return "RoomManagementId{" +
                    "userId='" + userId + '\'' +
                    ", chatRoomId='" + chatRoomId + '\'' +
                    '}';
        }
    }
}
