package com.testapp.rest;

import com.testapp.domain.ChatMessage;
import com.testapp.domain.User;
import com.testapp.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    private static final Logger log = LoggerFactory.getLogger(MessageController.class);

    @PostMapping("/chatRoom/{chatRoom}")
    public ResponseEntity<?> sendMessage(@PathVariable String chatRoom,
                                        @RequestBody ChatMessage chatMessage) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        chatMessage.setUserId(currentUser);
        log.info("Sending message {} to chat room {}", chatMessage, chatRoom);

        return ResponseEntity.ok().body(messageService.sendMessage(chatRoom, chatMessage));
    }
}
