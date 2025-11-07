package com.testapp.config;

import com.testapp.domain.dto.ChatMessageDTO;
import com.testapp.domain.ChatMessageKey;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaProducerConfig {

    private final KafkaTopicConfig kafkaConfig;

    @Bean
    public ProducerFactory<ChatMessageKey, ChatMessageDTO> producerChatMessageFactory() {
        Map<String, Object> props = kafkaConfig.buildProducerProperties();
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<ChatMessageKey, ChatMessageDTO> kafkaChatMessageTemplate() {
        return new KafkaTemplate<>(producerChatMessageFactory());
    }

    //TODO user notifications topic
}
