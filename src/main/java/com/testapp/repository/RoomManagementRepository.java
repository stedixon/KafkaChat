package com.testapp.repository;


import com.testapp.domain.RoomManagement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomManagementRepository extends JpaRepository<RoomManagement, String> {

    @Query(value = "select count(*) from room_management where chat_room_id = :chatRoomId", nativeQuery = true)
    int findParticipantCountByChatRoomId(@Param("chatRoomId") String chatRoomId);

    List<RoomManagement> findByChatRoomId(@Param("chatRoomId") String chatRoomId);

    Optional<RoomManagement> findByUserIdAndChatRoomId(String userId, String chatRoomId);
}
