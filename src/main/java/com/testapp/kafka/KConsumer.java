package com.testapp.kafka;

import com.testapp.domain.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

public class KConsumer {

    private static final Logger log = LoggerFactory.getLogger(KConsumer.class);

    @KafkaListener(topics = "topic-name", containerFactory = "chatMessageContainerFactory")
    public void chatMessageListener(ChatMessage message) {
        log.info("Received Message {}", message);
    }
}
