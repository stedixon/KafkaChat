package com.testapp.service;

import com.testapp.domain.ChatMessage;
import com.testapp.domain.User;
import com.testapp.exceptions.UserExistsException;
import com.testapp.kafka.KProducer;
import com.testapp.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUser(String id) {
        return userRepository.findById(id).orElse(null);
    }

    public User createUser(User user) {
        Optional<User> existing = userRepository.findByUsername(user.getUsername());
        if (existing.isPresent()) {
            throw new UserExistsException("Username " + user.getUsername() + " is already taken.");
        }

        user.setId(UUID.randomUUID().toString());
        return userRepository.save(user);
    }
}
