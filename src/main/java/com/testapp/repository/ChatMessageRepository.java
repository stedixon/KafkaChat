package com.testapp.repository;

import com.testapp.domain.dto.ChatMessageDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageDTO, String>  {

}
