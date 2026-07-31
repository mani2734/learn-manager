package com.learnmanager.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class StudyTime extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private StudyModule studyModule;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private LearningGoal learningGoal;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private PlannedStudySession plannedStudySession;

  @Column(nullable = false) private LocalDateTime startTime;

  @Column(nullable = false) private LocalDateTime endTime;

  @Column(length = 2000) private String notes;

  public StudyTime(
      User user,
      StudyModule studyModule,
      LearningGoal learningGoal,
      PlannedStudySession plannedStudySession,
      LocalDateTime startTime,
      LocalDateTime endTime,
      String notes) {
    this.user = user;
    this.studyModule = studyModule;
    this.learningGoal = learningGoal;
    this.plannedStudySession = plannedStudySession;
    this.startTime = startTime;
    this.endTime = endTime;
    this.notes = notes;
  }

  @Transient
  public long getDurationMinutes() {
    if (startTime == null || endTime == null) {
      return 0;
    }

    return Duration.between(startTime, endTime).toMinutes();
  }

  public void update(
      LearningGoal learningGoal,
      PlannedStudySession plannedStudySession,
      LocalDateTime startTime,
      LocalDateTime endTime,
      String notes) {
    this.learningGoal = learningGoal;
    this.plannedStudySession = plannedStudySession;
    this.startTime = startTime;
    this.endTime = endTime;
    this.notes = notes;
  }

}