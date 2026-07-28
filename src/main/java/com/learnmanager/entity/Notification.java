package com.learnmanager.entity;

import com.learnmanager.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Notification extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 40)
  private NotificationType type;

  @Column(nullable = false, length = 150) private String title;

  @Column(nullable = false, length = 2000) private String message;

  @Column(nullable = false) private boolean read = false;

  public Notification(User user, NotificationType type, String title, String message) {
    this.user = user;
    this.type = type;
    this.title = title;
    this.message = message;
  }
}