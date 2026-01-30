package com.testapp.repository;


import com.testapp.domain.dto.ChatRoomDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoomDTO, String> {

    Optional<ChatRoomDTO> findByDisplayName(String displayName);
}
