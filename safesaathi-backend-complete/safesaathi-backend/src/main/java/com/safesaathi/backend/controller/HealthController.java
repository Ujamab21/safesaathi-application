package com.safesaathi.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// Server chal raha hai ya nahi, ye check karne ke liye (login ki zaroorat nahi).
// Phone ke browser mein  https://<aapka-server>/api/health  kholo -> {"status":"ok"}
@RestController
public class HealthController {

    @GetMapping({"/", "/api/health"})
    public Map<String, String> health() {
        return Map.of("status", "ok", "service", "safesaathi-backend");
    }
}
