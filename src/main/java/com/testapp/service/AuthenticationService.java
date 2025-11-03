package com.testapp.service;

import com.testapp.domain.LoginUser;
import com.testapp.domain.RegisterUser;
import com.testapp.domain.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;

    private final UserService userService;

    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            AuthenticationManager authenticationManager,
            UserService userService,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public User signup(RegisterUser input) {
        User user = new User();
        user.setFirstName(input.getFirstName());
        user.setLastName(input.getLastName());
        user.setUsername(input.getUsername());
        user.setEmail(input.getEmail());
        user.setPassword(passwordEncoder.encode(input.getPassword()));

        return userService.createUser(user);
    }

    public User authenticate(LoginUser input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getUsername(),
                        input.getPassword()
                )
        );

        User user = userService.getUserByUsername(input.getUsername());

        if (user == null) {
            throw new RuntimeException("Unable to login");
        }

        return user;
    }
}
