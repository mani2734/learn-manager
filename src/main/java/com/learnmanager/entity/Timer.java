package com.learnmanager.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Timer extends BaseEntity {

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false, unique = true)
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

  public Timer(
      User user,
      StudyModule studyModule,
      LearningGoal learningGoal,
      PlannedStudySession plannedStudySession,
      LocalDateTime startTime) {
    this.user = user;
    this.studyModule = studyModule;
    this.learningGoal = learningGoal;
    this.plannedStudySession = plannedStudySession;
    this.startTime = startTime;
  }
}