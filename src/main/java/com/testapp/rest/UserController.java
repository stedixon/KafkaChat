package com.testapp.rest;

import com.testapp.domain.User;
import com.testapp.exceptions.UserExistsException;
import com.testapp.service.UserService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/user")
public class UserController {

    private final UserService userService;

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/me")
    public ResponseEntity<?> getMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        log.info("Getting myself for id {}", currentUser.getId());
        try {
            return ResponseEntity.ok(userService.getUser(currentUser.getId()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to get me");
        }
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> getUser(@PathVariable String id) {
        log.info("Getting user id {}", id);
        try {
            return ResponseEntity.ok(userService.getUser(id));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to get user");
        }
    }

    @GetMapping("/chatRoom/{chatRoomId}")
    public ResponseEntity<?> getUsersInChat(@PathVariable String chatRoomId) {
        log.info("Getting users in chatId {}", chatRoomId);
        try {
            return ResponseEntity.ok(userService.getUsersInChatRoom(chatRoomId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to get users in chat room");
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@Validated @RequestBody User user) {
        log.info("Creating user {}", user);
        try {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(userService.createUser(user));
        } catch (UserExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username is already taken");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to save user: " + e.getMessage());
        }
    }
}
