package com.testapp.service;

import com.testapp.domain.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    @KafkaListener(topics = "test-topic", containerFactory = "chatMessageContainerFactory")
    public void chatMessageListener(ChatMessage message) {
        log.info("received message {}", message);
    }
}
