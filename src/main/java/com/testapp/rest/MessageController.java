package com.testapp.rest;

import com.testapp.domain.dto.ChatMessageDTO;
import com.testapp.domain.dto.UserDTO;
import com.testapp.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
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
                                        @RequestBody ChatMessageDTO chatMessageDTO) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDTO currentUser = (UserDTO) authentication.getPrincipal();
        chatMessageDTO.setUserId(currentUser);
        log.info("Sending message {} to chat room {}", chatMessageDTO, chatRoom);

        return ResponseEntity.ok().body(messageService.sendMessage(chatRoom, chatMessageDTO));
    }

    @GetMapping("/chatRoom/{chatRoom}")
    public ResponseEntity<?> getMessages(@PathVariable String chatRoom) {
        log.info("Getting messages for chat room {}", chatRoom);
        try {
            return ResponseEntity.ok(messageService.getMessagesForChatRoom(chatRoom));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to get messages: " + e.getMessage());
        }
    }
}
