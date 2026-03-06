package com.ttwreis.controller;

import com.ttwreis.entity.Notification;
import com.ttwreis.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    /**
     * GET /api/notifications/active  (public – shown on home page scroller)
     * Returns all notifications that are active and not yet expired.
     */
    @GetMapping("/active")
    public ResponseEntity<List<Notification>> getActive() {
        return ResponseEntity.ok(
                notificationRepository.findActiveNotifications(LocalDate.now()));
    }

    /**
     * POST /api/notifications  (admin only)
     * Creates a new notification.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Notification> create(@RequestBody Notification notification) {
        return ResponseEntity.ok(notificationRepository.save(notification));
    }

    /**
     * DELETE /api/notifications/{id}  (admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setActive(false);
            notificationRepository.save(n);
        });
        return ResponseEntity.noContent().build();
    }
}
