package com.testapp.kafka;

import com.testapp.domain.ChatMessage;
import com.testapp.domain.ChatMessageKey;
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

    private final KafkaTemplate<ChatMessageKey, ChatMessage> kafkaChatMessageTemplate;

    public void sendMessage(String chatRoomId, ChatMessage message) {
        ChatMessageKey key = new ChatMessageKey(chatRoomId, message.getUserId().getId(), message.getId());
        ProducerRecord<ChatMessageKey, ChatMessage> record = new ProducerRecord<>(
                CHAT_MESSAGE_TOPIC,
                key,
                message
        );

        CompletableFuture<SendResult<ChatMessageKey, ChatMessage>> future = kafkaChatMessageTemplate
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
