package com.ssaika.ssiren.domain.notification.repository;

import com.ssaika.ssiren.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
