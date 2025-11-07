package com.testapp.kafka;

import com.testapp.domain.dto.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.testapp.config.Constants.CHAT_MESSAGE_TOPIC;

@Component
@RequiredArgsConstructor
public class KConsumer {

    private static final Logger log = LoggerFactory.getLogger(KConsumer.class);

    @KafkaListener(topics = CHAT_MESSAGE_TOPIC, containerFactory = "chatMessageContainerFactory")
    public void chatMessageListener(ChatMessageDTO message) {
        log.info("received message {}", message);
        //TODO send to user controller
    }
}
