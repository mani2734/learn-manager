package com.learnmanager.dto.response;

import com.learnmanager.entity.User;
import com.learnmanager.entity.enums.Role;

import java.time.LocalDateTime;

public record AdminUserResponse(Long id, String email, Role role, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {

  public static AdminUserResponse fromEntity(User user) {
    return new AdminUserResponse(user.getId(), user.getEmail(), user.getRole(), user.isActive(), user.getCreatedAt(), user.getUpdatedAt());
  }
}