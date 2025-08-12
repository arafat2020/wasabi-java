package com.wasabi.wasabi.src.auth.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.wasabi.wasabi.src.auth.dto.AuthenticationRequest;
import com.wasabi.wasabi.src.auth.dto.RegisterRequest;
import com.wasabi.wasabi.src.utils.exception.UserAlreadyExistsException;
import com.wasabi.wasabi.src.auth.model.UserModel;
import com.wasabi.wasabi.src.auth.repository.AuthRepository;

@Service
public class RegisterService {
    private static final Logger logger = LoggerFactory.getLogger(RegisterService.class);

    @Autowired
    private AuthRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public String register(RegisterRequest request) {
        if (request == null) {
            logger.error("Registration request is null");
            throw new IllegalArgumentException("Registration request cannot be null");
        }
        logger.info("Starting registration process for username: {}", request.getUsername());

        try {
            // Input validation
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                logger.error("Username is null or empty");
                throw new IllegalArgumentException("Username cannot be empty");
            }

            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                logger.error("Email is null or empty");
                throw new IllegalArgumentException("Email cannot be empty");
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                logger.error("Password is null or empty");
                throw new IllegalArgumentException("Password cannot be empty");
            }

            logger.debug("Input validation passed for user: {}", request.getUsername());

            // Check if username or email exists
            logger.debug("Checking if username exists: {}", request.getUsername());
            if (userRepository.existsByUsername(request.getUsername())) {
                logger.warn("Registration failed - Username already exists: {}", request.getUsername());
                throw new UserAlreadyExistsException("Username already taken");
            }

            logger.debug("Checking if email exists: {}", request.getEmail());
            if (userRepository.existsByEmail(request.getEmail())) {
                logger.warn("Registration failed - Email already exists: {}", request.getEmail());
                throw new UserAlreadyExistsException("Email already taken");
            }

            logger.debug("Creating new user entity for: {}", request.getUsername());
            UserModel user = new UserModel();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEmail(request.getEmail());

            logger.debug("Saving user to database: {}", request.getUsername());
            userRepository.save(user);

            logger.info("User registered successfully: {}", request.getUsername());
            return "User registered successfully";

        } catch (UserAlreadyExistsException | IllegalArgumentException e) {
            logger.error("Registration validation failed: {}", e.getMessage());
            throw e;
        } catch (DataIntegrityViolationException e) {
            logger.error("Database constraint violation during registration: {}", e.getMessage(), e);
            // Handle database constraint violations
            String message = e.getMessage().toLowerCase();
            if (message.contains("username") || message.contains("users_username_key")) {
                throw new UserAlreadyExistsException("Username already taken (database constraint)");
            } else if (message.contains("email") || message.contains("users_email_key")) {
                throw new UserAlreadyExistsException("Email already taken (database constraint)");
            } else {
                throw new UserAlreadyExistsException(
                        "User registration failed due to data constraints: " + e.getMessage());
            }
        } catch (Exception e) {
            logger.error("Unexpected error during registration for user: {}", request.getUsername(), e);
            throw new RuntimeException("Registration failed due to unexpected error: " + e.getMessage(), e);
        }
    }
}
