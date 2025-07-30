package com.wasabi.wasabi.src.auth.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wasabi.wasabi.src.auth.model.UserModel;

public interface AuthRepository extends JpaRepository<UserModel, UUID> {
    UserModel findByUsername(String username);
}
