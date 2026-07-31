package com.learnmanager.controller;

import com.learnmanager.dto.NotificationResponse;
import com.learnmanager.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications/")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping("getAll")
  public List<NotificationResponse> getAll(Authentication authentication) {
    return notificationService.getAll(authentication.getName());
  }

  @GetMapping("getUnread")
  public List<NotificationResponse> getUnread(Authentication authentication) {
    return notificationService.getUnread(authentication.getName());
  }

  @GetMapping("get/{id}")
  public NotificationResponse getById(Authentication authentication, @PathVariable Long id) {
    return notificationService.getById(authentication.getName(), id);
  }

  @PutMapping("markAsRead/{id}")
  public NotificationResponse markAsRead(Authentication authentication, @PathVariable Long id) {
    return notificationService.markAsRead(authentication.getName(), id);
  }

  @PutMapping("markAllAsRead")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void markAllAsRead(Authentication authentication) {
    notificationService.markAllAsRead(authentication.getName());
  }
}