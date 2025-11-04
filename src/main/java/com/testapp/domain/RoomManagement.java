package com.testapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@IdClass(RoomManagement.RoomManagementId.class)
@Table(name = "room_management")
public class RoomManagement implements Serializable {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Id
    @Column(name = "chat_room_id")
    private String chatRoomId;

    @Column(name = "is_admin")
    private boolean isAdmin;

    public RoomManagement(String userId, String chatRoomId) {
        this.userId = userId;
        this.chatRoomId = chatRoomId;
        this.isAdmin = false;
    }

    @Override
    public String toString() {
        return "RoomManagement{" +
                "userId=" + userId +
                ", chatRoomId=" + chatRoomId +
                '}';
    }

    @Data
    public static class RoomManagementId implements Serializable {
        @Column(name = "user_id")
        private String userId;
        @Column(name = "chat_room_id")
        private String chatRoomId;
    }
}
