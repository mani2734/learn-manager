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
public class PlannedStudySession extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private StudyModule studyModule;

  @Column(nullable = false, length = 150) private String title;

  @Column(nullable = false) private LocalDateTime startTime;

  @Column(nullable = false) private LocalDateTime endTime;

  public PlannedStudySession(User user, StudyModule studyModule, String title, LocalDateTime startTime, LocalDateTime endTime) {
    this.user = user;
    this.studyModule = studyModule;
    this.title = title;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public void update(String title, LocalDateTime startTime, LocalDateTime endTime) {
    this.title = title;
    this.startTime = startTime;
    this.endTime = endTime;
  }
}