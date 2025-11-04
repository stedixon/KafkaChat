package com.testapp.service;

import com.testapp.domain.ChatMessage;
import com.testapp.kafka.KProducer;
import com.testapp.repository.ChatMessageRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    private final KProducer messageProducer;
    private final ChatMessageRepository messageRepository;

    @KafkaListener(topics = "test-topic", containerFactory = "chatMessageContainerFactory")
    public void chatMessageListener(ChatMessage message) {
        log.info("received message {}", message);
        messageRepository.save(message);
    }

    public void sendMessage(ChatMessage chatMessage) {
        messageProducer.sendMessage(chatMessage);
    }
}
