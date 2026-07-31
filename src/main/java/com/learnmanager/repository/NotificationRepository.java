package com.learnmanager.repository;

import com.learnmanager.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

  List<Notification> findAllByUser_EmailIgnoreCaseOrderByCreatedAtDesc(String email);

  List<Notification> findAllByUser_EmailIgnoreCaseAndReadStatusFalseOrderByCreatedAtDesc(String email);

  Optional<Notification> findByIdAndUser_EmailIgnoreCase(Long id, String email);
}
