package com.testapp.config;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.testapp.domain.ChatMessage;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Autowired
    private KafkaTopicConfig kafkaConfig;

    @Bean
    public ProducerFactory<String, ChatMessage> producerChatMessageFactory() {
        Map<String, Object> props = kafkaConfig.buildProducerProperties();
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, ChatMessage> kafkaChatMessageTemplate() {
        return new KafkaTemplate<>(producerChatMessageFactory());
    }
}
