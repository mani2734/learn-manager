package com.learnmanager.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class NotificationSettings extends BaseEntity {

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false, unique = true)
  private User user;

  @Column(nullable = false) private boolean plannedSessionReminderEnabled = true;

  @Column(nullable = false) private Integer plannedSessionReminderMinutes = 30;

  @Column(nullable = false) private boolean inactivityReminderEnabled = true;

  @Column(nullable = false) private Integer inactivityThresholdDays = 3;

  @Column(nullable = false) private boolean goalDeadlineReminderEnabled = true;

  @Column(nullable = false) private Integer goalDeadlineReminderDays = 7;

  @Column(nullable = false) private boolean planDeviationReminderEnabled = true;

  @Column(nullable = false) private Integer planDeviationThresholdPercent = 20;

  public NotificationSettings(User user) {
    this.user = user;
  }

  public void update(
      boolean plannedSessionReminderEnabled,
      Integer plannedSessionReminderMinutes,
      boolean inactivityReminderEnabled,
      Integer inactivityThresholdDays,
      boolean goalDeadlineReminderEnabled,
      Integer goalDeadlineReminderDays,
      boolean planDeviationReminderEnabled,
      Integer planDeviationThresholdPercent) {
    this.plannedSessionReminderEnabled = plannedSessionReminderEnabled;
    this.plannedSessionReminderMinutes = plannedSessionReminderMinutes;
    this.inactivityReminderEnabled = inactivityReminderEnabled;
    this.inactivityThresholdDays = inactivityThresholdDays;
    this.goalDeadlineReminderEnabled = goalDeadlineReminderEnabled;
    this.goalDeadlineReminderDays = goalDeadlineReminderDays;
    this.planDeviationReminderEnabled = planDeviationReminderEnabled;
    this.planDeviationThresholdPercent = planDeviationThresholdPercent;
  }
}