package com.testapp.kafka;

import com.testapp.domain.ChatMessage;
import com.testapp.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

import static com.testapp.config.Constants.CHAT_MESSAGE_TOPIC;

@Component
@RequiredArgsConstructor
public class KProducer {

    private static final Logger log = LoggerFactory.getLogger(KProducer.class);

    private final KafkaTemplate<String, ChatMessage> kafkaChatMessageTemplate;

    public void sendMessage(String chatRoomId, ChatMessage message) {
        ProducerRecord<String, ChatMessage> record = new ProducerRecord<>(
                CHAT_MESSAGE_TOPIC, chatRoomId, message
        );

        CompletableFuture<SendResult<String, ChatMessage>> future = kafkaChatMessageTemplate
                .send(record);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("sent message {}", message);
            } else {
                log.error("Failed to send message {}", message);
            }
        });
    }
}
