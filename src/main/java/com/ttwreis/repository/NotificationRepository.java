package com.ttwreis.repository;

import com.ttwreis.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE n.active = true " +
           "AND (n.expiryDate IS NULL OR n.expiryDate >= :today) " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findActiveNotifications(@Param("today") LocalDate today);
}
