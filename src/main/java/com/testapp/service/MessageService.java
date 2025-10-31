package com.testapp.service;

import com.testapp.domain.ChatMessage;
import com.testapp.kafka.KProducer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    private final KProducer messageProducer;

    public MessageService(@NonNull KProducer messageProducer) {
        this.messageProducer = messageProducer;
    }

    public void sendMessage(ChatMessage chatMessage) {
        messageProducer.sendMessage(chatMessage);
    }
}
