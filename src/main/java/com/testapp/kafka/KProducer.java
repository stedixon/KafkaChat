package com.testapp.kafka;

import com.testapp.domain.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class KProducer {

    private final KafkaTemplate<String, ChatMessage> kafkaChatMessageTemplate;

    private static final Logger log = LoggerFactory.getLogger(KProducer.class);

    public void sendMessage(ChatMessage message) {
        ProducerRecord<String, ChatMessage> record = new ProducerRecord<>(
                "test-topic", message
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
