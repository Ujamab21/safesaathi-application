package com.safesaathi.backend.controller;

import com.safesaathi.backend.model.AppEvent;
import com.safesaathi.backend.model.User;
import com.safesaathi.backend.repository.AppEventRepository;
import com.safesaathi.backend.repository.UserRepository;
import com.safesaathi.backend.security.JwtUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AppEventRepository eventRepository;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil,
                          AppEventRepository eventRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.eventRepository = eventRepository;
    }

    // Admin dashboard ke liye activity record karta hai. Ye fail ho to bhi login/register nahi rukte.
    private void logEvent(Long userId, String email, String type, String details) {
        try {
            String safeEmail = email == null ? null : (email.length() > 200 ? email.substring(0, 200) : email);
            eventRepository.save(new AppEvent(userId, safeEmail, type, details));
        } catch (Exception ignored) {
        }
    }

    public record RegisterRequest(
            @NotBlank String fullName,
            @NotBlank @Email String email,
            String phone,
            @NotBlank @Size(min = 6) String password) {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {}

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        }
        User user = new User();
        user.setFullName(req.fullName());
        user.setEmail(req.email());
        user.setPhone(req.phone());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        userRepository.save(user);
        logEvent(user.getId(), user.getEmail(), "REGISTER", null);
        return ResponseEntity.ok(Map.of("message", "Registered successfully", "id", user.getId()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        Optional<User> found = userRepository.findByEmail(req.email());
        if (found.isEmpty() || !passwordEncoder.matches(req.password(), found.get().getPasswordHash())) {
            logEvent(null, req.email(), "LOGIN_FAILED", null);
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
        }
        User user = found.get();
        String role = String.valueOf(user.getRole());
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), role);
        logEvent(user.getId(), user.getEmail(), "LOGIN", null);
        return ResponseEntity.ok(Map.of(
                "token", token,
                "id", user.getId(),
                "name", user.getFullName(),
                "email", user.getEmail(),
                "role", role));
    }
}