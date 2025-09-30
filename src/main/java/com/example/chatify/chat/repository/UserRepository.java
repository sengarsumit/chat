package com.example.chatify.chat.repository;

import com.example.chatify.chat.model.users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<users, UUID> {
    users findByUsername(String username);
    Optional<users> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
