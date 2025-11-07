package com.testapp.config;

import com.testapp.domain.dto.ChatMessageDTO;
import com.testapp.domain.ChatMessageKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Autowired
    private KafkaTopicConfig kafkaConfig;

    @Bean
    public ConsumerFactory<ChatMessageKey, ChatMessageDTO> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
                kafkaConfig.buildConsumerProperties(),
                new JsonDeserializer<>(ChatMessageKey.class),
                new JsonDeserializer<>(ChatMessageDTO.class)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<ChatMessageKey, ChatMessageDTO> chatMessageContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<ChatMessageKey, ChatMessageDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
