package com.safesaathi.backend.repository;

import com.safesaathi.backend.model.AppEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppEventRepository extends JpaRepository<AppEvent, Long> {
    List<AppEvent> findTop200ByOrderByCreatedAtDesc();
    long countByEventTypeAndCreatedAtAfter(String eventType, LocalDateTime after);
}
