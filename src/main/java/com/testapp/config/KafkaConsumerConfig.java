package com.testapp.config;

import com.testapp.domain.ChatMessage;
import org.apache.kafka.common.serialization.StringDeserializer;
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
    public ConsumerFactory<String, ChatMessage> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
                kafkaConfig.buildConsumerProperties(),
                new StringDeserializer(),
                new JsonDeserializer<>(ChatMessage.class)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ChatMessage> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ChatMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
