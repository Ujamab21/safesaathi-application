package com.safesaathi.backend.controller;

import com.safesaathi.backend.model.AppEvent;
import com.safesaathi.backend.model.User;
import com.safesaathi.backend.repository.AppEventRepository;
import com.safesaathi.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Admin ke liye: sab users aur app ki activity. Sirf sahi ADMIN_KEY wale ko milta hai.
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final ZoneId UTC = ZoneId.of("UTC");

    private final UserRepository userRepository;
    private final AppEventRepository eventRepository;

    // ADMIN_KEY set nahi hai (ya 12 se chhota hai) to admin panel band rehta hai
    @Value("${admin.key:}")
    private String adminKey;

    public AdminController(UserRepository userRepository, AppEventRepository eventRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    private boolean authorized(String provided) {
        if (adminKey == null || adminKey.length() < 12 || provided == null) return false;
        return MessageDigest.isEqual(
                adminKey.getBytes(StandardCharsets.UTF_8),
                provided.getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/summary")
    public ResponseEntity<?> summary(
            @RequestHeader(value = "X-Admin-Key", required = false) String key) {
        if (!authorized(key)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        // "Aaj" India ke hisaab se (raat 12 baje se), DB ka time UTC mein hai
        LocalDateTime startOfToday = ZonedDateTime.now(IST).toLocalDate()
                .atStartOfDay(IST).withZoneSameInstant(UTC).toLocalDateTime();

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("totalUsers", userRepository.count());
        out.put("registrationsToday", userRepository.countByCreatedAtAfter(startOfToday));
        out.put("loginsToday", eventRepository.countByEventTypeAndCreatedAtAfter("LOGIN", startOfToday));
        out.put("failedLoginsToday", eventRepository.countByEventTypeAndCreatedAtAfter("LOGIN_FAILED", startOfToday));
        out.put("sosToday", eventRepository.countByEventTypeAndCreatedAtAfter("SOS", startOfToday));
        out.put("totalEvents", eventRepository.count());

        List<Map<String, Object>> users = new ArrayList<>();
        for (User u : userRepository.findTop200ByOrderByCreatedAtDesc()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("name", u.getFullName());
            m.put("email", u.getEmail());
            m.put("phone", u.getPhone());
            m.put("role", u.getRole());
            m.put("createdAt", u.getCreatedAt() == null ? null : u.getCreatedAt().toString());
            users.add(m);
        }
        out.put("users", users);

        List<Map<String, Object>> events = new ArrayList<>();
        for (AppEvent e : eventRepository.findTop200ByOrderByCreatedAtDesc()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", e.getId());
            m.put("type", e.getEventType());
            m.put("email", e.getUserEmail());
            m.put("details", e.getDetails());
            m.put("createdAt", e.getCreatedAt() == null ? null : e.getCreatedAt().toString());
            events.add(m);
        }
        out.put("events", events);

        return ResponseEntity.ok(out);
    }
}
