package com.edtech.platform.repository;

import com.edtech.platform.entity.Notification;
import com.edtech.platform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    List<Notification> findByStatusOrderByCreatedAtAsc(Notification.Status status);
}