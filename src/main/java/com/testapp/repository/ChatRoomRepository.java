package com.testapp.repository;


import com.testapp.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {

    @Query(value = "select cr.id, " +
            "cr.display_name, " +
            "cr.description, " +
            "count(rm.user_id) as participant_count " +
            "from chat_room cr join room_management rm " +
            "on cr.id = rm.chat_room_id " +
            "where cr.id = :id", nativeQuery = true)
    Optional<ChatRoom> findByIdWithParticipantCount(@Param("id") String id);

    Optional<ChatRoom> findByDisplayName(String displayName);
}
