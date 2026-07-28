package com.learnmanager.entity;

import com.learnmanager.enums.GoalStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table
public class LearningGoal extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private StudyModule studyModule;

  @Column(nullable = false, length = 150) private String title;

  @Column(length = 2000) private String description;

  @Column(nullable = false, precision = 8, scale = 2) private BigDecimal workloadHours;

  @Column private LocalDate deadline;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private GoalStatus status = GoalStatus.PLANNED;

  public LearningGoal(StudyModule studyModule, String title, String description, BigDecimal workloadHours, LocalDate deadline) {
    this.studyModule = studyModule;
    this.title = title;
    this.description = description;
    this.workloadHours = workloadHours;
    this.deadline = deadline;
  }
}