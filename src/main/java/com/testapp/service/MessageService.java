package com.testapp.service;

import com.testapp.domain.ChatMessage;
import com.testapp.domain.ChatRoom;
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

    public ChatMessage sendMessage(String chatRoomId, ChatMessage chatMessage) {
        chatMessage.setId(UUID.randomUUID().toString());
        chatMessage.setChatRoom(new ChatRoom(chatRoomId));
        chatMessage.setTimeSent(Instant.now());
        messageProducer.sendMessage(chatRoomId, chatMessage);
        return messageRepository.save(chatMessage);
    }
}
