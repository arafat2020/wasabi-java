package com.wasabi.wasabi.src.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import com.wasabi.wasabi.src.auth.response.ApiResponse;
import com.wasabi.wasabi.src.auth.response.ErrorType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import com.wasabi.wasabi.src.auth.components.AuthUtils;
import com.wasabi.wasabi.src.auth.dto.AuthenticationRequest;
import com.wasabi.wasabi.src.auth.dto.AuthenticationResponse;
import com.wasabi.wasabi.src.auth.dto.RegisterRequest;
import com.wasabi.wasabi.src.auth.exception.UserAlreadyExistsException;
import com.wasabi.wasabi.src.auth.services.CustomUserDetailsService;
import com.wasabi.wasabi.src.auth.services.RegisterService;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private AuthUtils jwtUtil;

    @Autowired
    private RegisterService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@Valid @RequestBody RegisterRequest request,
            BindingResult bindingResult) {
        logger.info("Registration attempt for username: {}", request != null ? request.getUsername() : "null");

        // Handle validation errors
        if (bindingResult.hasErrors()) {
            logger.warn("Registration validation failed for username: {}", request.getUsername());

            List<String> errorMessages = bindingResult.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.toList());

            return ResponseEntity.badRequest().body(ApiResponse.validationError("Validation failed", errorMessages));
        }

        try {
            // Log the request details (without password)
            if (request != null) {
                logger.debug("Registration request - Username: {}, Email: {}",
                        request.getUsername(), request.getEmail());
            }

            String result = authService.register(request);
            logger.info("Registration successful for username: {}", request.getUsername());

            return ResponseEntity.ok(ApiResponse.success("Registration successful", result));

        } catch (UserAlreadyExistsException ex) {
            logger.warn("Registration failed - User already exists: {}", ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ex.getMessage(), ErrorType.USER_ALREADY_EXISTS.toString()));

        } catch (IllegalArgumentException ex) {
            logger.warn("Registration failed - Invalid input: {}", ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ex.getMessage(), ErrorType.VALIDATION_ERROR.toString()));

        } catch (Exception ex) {
            logger.error("Unexpected error during registration: ", ex);
            return ResponseEntity.internalServerError().body(ApiResponse
                    .internalError("Registration failed: " + ex.getMessage(), ex.getClass().getSimpleName()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody AuthenticationRequest request,
            BindingResult bindingResult) {
        logger.info("Login attempt for username: {}", request != null ? request.getUsername() : "null");

        // Handle validation errors
        if (bindingResult.hasErrors()) {
            logger.warn("Login validation failed for username: {}", request != null ? request.getUsername() : "null");

            List<String> errorMessages = bindingResult.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.toList());

            return ResponseEntity.badRequest().body(ApiResponse.validationError("Validation failed", errorMessages));
        }

        try {
            // Input validation
            if (request == null) {
                logger.error("Login request is null");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Login request cannot be null", ErrorType.VALIDATION_ERROR.toString()));
            }

            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                logger.error("Username is null or empty");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Username is required", ErrorType.VALIDATION_ERROR.toString()));
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                logger.error("Password is null or empty");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Password is required", ErrorType.VALIDATION_ERROR.toString()));
            }

            logger.debug("Attempting authentication for username: {}", request.getUsername());

            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            logger.debug("Authentication successful, loading user details for: {}", request.getUsername());

            final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            final String token = jwtUtil.generateToken(userDetails);

            logger.info("Login successful for username: {}", request.getUsername());

            AuthenticationResponse authResponse = AuthenticationResponse.builder()
                    .token(token)
                    .username(request.getUsername())
                    .build();

            return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));

        } catch (UsernameNotFoundException ex) {
            logger.warn("Login failed - User not found: {}", request.getUsername());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not found", ErrorType.USER_NOT_FOUND.toString()));

        } catch (BadCredentialsException ex) {
            logger.warn("Login failed - Invalid credentials for username: {}", request.getUsername());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid username or password", ErrorType.INVALID_CREDENTIALS.toString()));

        } catch (AuthenticationException ex) {
            logger.warn("Login failed - Authentication error for username: {}: {}", request.getUsername(),
                    ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Authentication failed", ErrorType.AUTHENTICATION_ERROR.toString()));

        } catch (Exception ex) {
            logger.error("Unexpected error during login for username: {}", request.getUsername(), ex);
            return ResponseEntity.internalServerError().body(
                    ApiResponse.internalError("Login failed due to unexpected error", ex.getClass().getSimpleName()));
        }
    }
}
