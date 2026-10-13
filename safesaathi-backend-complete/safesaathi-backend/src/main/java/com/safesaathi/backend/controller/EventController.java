package com.safesaathi.backend.controller;

import com.safesaathi.backend.model.AppEvent;
import com.safesaathi.backend.model.User;
import com.safesaathi.backend.repository.AppEventRepository;
import com.safesaathi.backend.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// App login ke baad yahan activity bhejti hai (jaise SOS, REPORT, TRIP). Login token zaroori hai.
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final AppEventRepository eventRepository;
    private final UserRepository userRepository;

    public EventController(AppEventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    public record EventRequest(
            @NotBlank @Pattern(regexp = "[A-Za-z_]{1,40}") String type,
            @Size(max = 500) String details) {}

    @PostMapping
    public ResponseEntity<?> log(@Valid @RequestBody EventRequest req, Authentication auth) {
        String email = String.valueOf(auth.getPrincipal());
        Long userId = userRepository.findByEmail(email).map(User::getId).orElse(null);
        eventRepository.save(new AppEvent(userId, email, req.type().toUpperCase(), req.details()));
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
