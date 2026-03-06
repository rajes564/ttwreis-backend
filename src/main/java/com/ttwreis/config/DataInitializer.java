package com.ttwreis.config;

import com.ttwreis.entity.Notification;
import com.ttwreis.entity.User;
import com.ttwreis.repository.NotificationRepository;
import com.ttwreis.repository.UserRepository;
import com.ttwreis.security.Sha256Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository         userRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public void run(String... args) {
        createAdminIfAbsent();
        seedNotificationsIfEmpty();
    }

    private void createAdminIfAbsent() {
        if (userRepository.findByRegistrationNumber("ADMIN001").isPresent()) return;

        // Password stored as sha256("Admin@123")
        User admin = User.builder()
                .registrationNumber("ADMIN001")
                .aadhaarNumber      ("000000000001")
                .candidateName      ("System Administrator")
                .email              ("admin@ttwreis.telangana.gov.in")
                .mobileNumber       ("9000000001")
                .password           (Sha256Util.hashPassword("Admin@123"))
                .role               (User.Role.ADMIN)
                .dateOfBirth        (LocalDate.of(1980, 1, 1))
                .gender             (User.Gender.Male)
                .active             (true)
                .build();

        userRepository.save(admin);
        log.info("✅ Admin seeded → registrationNumber=ADMIN001 / password=Admin@123");
    }

    private void seedNotificationsIfEmpty() {
        if (notificationRepository.count() > 0) return;

        notificationRepository.save(Notification.builder()
                .text("Online Admission Registration for Intermediate 2025-26 is now OPEN. Apply before 31st March 2025.")
                .expiryDate(LocalDate.of(2025, 3, 31)).isNew(true).active(true).build());
        notificationRepository.save(Notification.builder()
                .text("Hall Ticket download for Entrance Exam will be available from 10th April 2025.")
                .expiryDate(LocalDate.of(2025, 4, 30)).isNew(false).active(true).build());
        notificationRepository.save(Notification.builder()
                .text("Entrance Examination scheduled on 20th April 2025. Timing: 10:00 AM – 1:00 PM.")
                .expiryDate(LocalDate.of(2025, 4, 20)).isNew(false).active(true).build());
        notificationRepository.save(Notification.builder()
                .text("Phase-1 Seat Allotment list will be published on 5th May 2025.")
                .expiryDate(LocalDate.of(2025, 5, 31)).isNew(true).active(true).build());

        log.info("✅ Sample notifications seeded.");
    }
}
