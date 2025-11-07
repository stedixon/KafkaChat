package com.testapp.repository;


import com.testapp.domain.dto.UserDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserDTO, String> {

    Optional<UserDTO> findByUsername(String username);

    @Query(value = "select u.* " +
            "from user u, room_management rm " +
            "where u.id = rm.user_id " +
            "and rm.chat_room_id = :chatRoomId", nativeQuery = true)
    List<UserDTO> findByChatRoomId(@Param("chatRoomId") String chatRoomId);
}
