package com.testapp.repository;

import com.testapp.domain.dto.ChatMessageDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageDTO, String>  {

    @Query(value = "select * from chat_message where chat_room_id = :chatRoomId order by time_sent asc", nativeQuery = true)
    List<ChatMessageDTO> findByChatRoomId(@Param("chatRoomId") String chatRoomId);
}
