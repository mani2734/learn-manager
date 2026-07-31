package com.learnmanager.controller;

import com.learnmanager.dto.request.update.UpdateNotificationSettingsRequest;
import com.learnmanager.dto.response.NotificationSettingsResponse;
import com.learnmanager.service.NotificationSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notificationSettings/")
@RequiredArgsConstructor
public class NotificationSettingsController {

  private final NotificationSettingsService notificationSettingsService;

  @GetMapping("get")
  public NotificationSettingsResponse get(Authentication authentication) {
    return notificationSettingsService.get(authentication.getName());
  }

  @PutMapping("update")
  public NotificationSettingsResponse update(Authentication authentication, @Valid @RequestBody UpdateNotificationSettingsRequest request) {
    return notificationSettingsService.update(authentication.getName(), request);
  }
}