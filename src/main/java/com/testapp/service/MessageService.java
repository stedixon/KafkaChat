package com.testapp.service;

import com.testapp.domain.dto.ChatMessageDTO;
import com.testapp.domain.dto.ChatRoomDTO;
import com.testapp.kafka.KProducer;
import com.testapp.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    private final KProducer messageProducer;
    private final ChatMessageRepository messageRepository;

    public ChatMessageDTO sendMessage(String chatRoomId, ChatMessageDTO chatMessageDTO) {
        chatMessageDTO.setId(UUID.randomUUID().toString());
        chatMessageDTO.setChatRoomDTO(new ChatRoomDTO(chatRoomId));
        chatMessageDTO.setTimeSent(Instant.now());
        messageProducer.sendMessage(chatRoomId, chatMessageDTO);
        return messageRepository.save(chatMessageDTO);
    }
}
