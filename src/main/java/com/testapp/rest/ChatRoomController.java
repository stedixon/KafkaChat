package com.testapp.rest;

import com.testapp.domain.ChatRoom;
import com.testapp.domain.User;
import com.testapp.exceptions.UserExistsException;
import com.testapp.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/chatRoom")
public class ChatRoomController {

    private static final Logger log = LoggerFactory.getLogger(ChatRoomController.class);

    private final ChatRoomService chatRoomService;

    @GetMapping("/id/{id}")
    public ResponseEntity<?> getChatRoom(@PathVariable String id) {
        log.info("Getting chat room id {}", id);
        try {
            return ResponseEntity.ok(chatRoomService.getChatRoom(id));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to get chat room");
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createChatRoom(@RequestBody @Validated ChatRoom chatRoom) {
        log.info("Creating chatRoom {}", chatRoom);
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) authentication.getPrincipal();
            chatRoom.setAdmin(currentUser);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(chatRoomService.createChatRoom(chatRoom));
        } catch (UserExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Chat Room name is already taken");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to create Chat Room: " + e.getMessage());
        }
    }

    @PutMapping("/join/roomId/{roomId}/userId/{userId}")
    public ResponseEntity<?> joinRoom(@PathVariable String roomId, @PathVariable String userId) {
        log.info("Adding user {} to room {}", userId, roomId);
        try {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(chatRoomService
                            .addUserToRoom(ChatRoom.builder().id(roomId).build(), User.builder().id(userId).build()));
        } catch (UserExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User is already in room");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to add user to room: " + e.getMessage());
        }
    }
}
