package com.testapp.rest;

import com.testapp.domain.dto.ChatRoomDTO;
import com.testapp.domain.dto.UserDTO;
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
    public ResponseEntity<?> createChatRoom(@RequestBody @Validated ChatRoomDTO chatRoomDTO) {
        log.info("Creating chatRoom {}", chatRoomDTO);
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDTO currentUser = (UserDTO) authentication.getPrincipal();
            chatRoomDTO.setAdmin(currentUser);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(chatRoomService.createChatRoom(chatRoomDTO));
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
                            .addUserToRoom(ChatRoomDTO.builder().id(roomId).build(), UserDTO.builder().id(userId).build()));
        } catch (UserExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User is already in room");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to add user to room: " + e.getMessage());
        }
    }

    @GetMapping("/myRooms")
    public ResponseEntity<?> getMyChatRooms() {
        log.info("Getting chat rooms for current user");
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDTO currentUser = (UserDTO) authentication.getPrincipal();
            return ResponseEntity.ok(chatRoomService.getChatRoomsForUser(currentUser.getId()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to get chat rooms: " + e.getMessage());
        }
    }

    @PutMapping("/join/{roomId}")
    public ResponseEntity<?> joinRoom(@PathVariable String roomId) {
        log.info("Adding current user to room {}", roomId);
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDTO currentUser = (UserDTO) authentication.getPrincipal();
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(chatRoomService
                            .addUserToRoom(ChatRoomDTO.builder().id(roomId).build(), currentUser));
        } catch (UserExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User is already in room");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to add user to room: " + e.getMessage());
        }
    }
}
