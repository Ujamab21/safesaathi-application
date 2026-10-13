package com.safesaathi.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

// App mein hui har activity ka record (register, login, SOS, report...). Sirf admin dekh sakta hai.
@Entity
@Table(name = "app_events")
@Getter
@Setter
@NoArgsConstructor
public class AppEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(length = 200)
    private String userEmail;

    @Column(nullable = false, length = 40)
    private String eventType;

    @Column(length = 500)
    private String details;

    // UTC mein save hota hai, dashboard India time mein dikhata hai
    private LocalDateTime createdAt = LocalDateTime.now(ZoneOffset.UTC);

    public AppEvent(Long userId, String userEmail, String eventType, String details) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.eventType = eventType;
        this.details = details;
    }
}
