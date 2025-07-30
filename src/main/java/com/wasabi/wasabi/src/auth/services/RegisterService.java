package com.wasabi.wasabi.src.auth.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.wasabi.wasabi.src.auth.dto.AuthenticationRequest;
import com.wasabi.wasabi.src.auth.model.UserModel;
import com.wasabi.wasabi.src.auth.repository.AuthRepository;

@Service
public class RegisterService {
    @Autowired
    private AuthRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public String register(AuthenticationRequest request) {
        // Check if username exists
        UserModel existingUser = userRepository.findByUsername(request.getUsername());

        if (existingUser != null) {
            throw new RuntimeException("Username already taken");
        }

        UserModel user = new UserModel();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Hash password

        userRepository.save(user);
        return "User registered successfully";
    }
}
