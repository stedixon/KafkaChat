package com.testapp.kafka;

import com.testapp.domain.dto.ChatMessageDTO;
import com.testapp.domain.server.ChatMessage;
import com.testapp.rest.ServerController;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.testapp.config.Constants.CHAT_MESSAGE_TOPIC;

@Component
@RequiredArgsConstructor
public class KConsumer {

    private static final Logger log = LoggerFactory.getLogger(KConsumer.class);

    @KafkaListener(topics = CHAT_MESSAGE_TOPIC, containerFactory = "chatMessageContainerFactory")
    public void chatMessageListener(ChatMessageDTO messageDTO) {
        log.info("received message {}", messageDTO);
        
        // Convert ChatMessageDTO to ChatMessage and broadcast via WebSocket
        if (messageDTO.getChatRoomDTO() != null && messageDTO.getChatRoomDTO().getId() != null) {
            try {
                ChatMessage wsMessage = ChatMessage.builder()
                        .chatRoomName(messageDTO.getChatRoomDTO().getId())
                        .message(messageDTO.getMessage())
                        .username(messageDTO.getUserId() != null ? messageDTO.getUserId().getUsername() : null)
                        .timeSent(messageDTO.getTimeSent())
                        .build();
                
                ServerController.broadcastMessage(wsMessage);
            } catch (Exception e) {
                log.error("Failed to broadcast message via WebSocket", e);
            }
        }
    }
}
