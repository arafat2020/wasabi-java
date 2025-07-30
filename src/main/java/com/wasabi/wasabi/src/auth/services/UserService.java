package com.wasabi.wasabi.src.auth.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wasabi.wasabi.src.auth.model.UserModel;
import com.wasabi.wasabi.src.auth.repository.AuthRepository;

@Service
public class UserService {
    @Autowired
    private AuthRepository userRepository;

    public UserModel getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public UserModel save(UserModel user) {
        return userRepository.save(user);
    }
}
