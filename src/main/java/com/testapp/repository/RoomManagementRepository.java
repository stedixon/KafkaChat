package com.testapp.repository;


import com.testapp.domain.ChatRoom;
import com.testapp.domain.RoomManagement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomManagementRepository extends JpaRepository<RoomManagement, String> {

    Optional<RoomManagement> findByUserIdAndChatRoomId(String userId, String chatRoomId);
}
