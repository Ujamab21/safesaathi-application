package com.safesaathi.backend.repository;

import com.safesaathi.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findTop200ByOrderByCreatedAtDesc();
    long countByCreatedAtAfter(LocalDateTime after);
}
