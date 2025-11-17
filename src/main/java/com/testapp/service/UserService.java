package com.testapp.service;

import com.testapp.domain.dto.UserDTO;
import com.testapp.exceptions.UserExistsException;
import com.testapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDTO getUser(String id) {
        return userRepository.findById(id).orElse(null);
    }

    public UserDTO getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public List<UserDTO> getUsersInChatRoom(String chatRoomId) {
        return userRepository.findByChatRoomId(chatRoomId);
    }

    public UserDTO createUser(UserDTO user) {
        Optional<UserDTO> existing = userRepository.findByUsername(user.getUsername());
        if (existing.isPresent()) {
            throw new UserExistsException("Username " + user.getUsername() + " is already taken.");
        }

        user.setId(UUID.randomUUID().toString());
        return userRepository.save(user);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll();
    }
}
