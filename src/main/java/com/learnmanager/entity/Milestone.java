package com.learnmanager.entity;

import com.learnmanager.enums.GoalStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Milestone extends BaseEntity {

  @Column(nullable = false, length = 150) private String title;

  @Column(length = 2000) private String description;

  private LocalDate deadline;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private GoalStatus status = GoalStatus.PLANNED;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private LearningGoal learningGoal;

  public Milestone(LearningGoal learningGoal, String title, String description, LocalDate deadline) {
    this.learningGoal = learningGoal;
    this.title = title;
    this.description = description;
    this.deadline = deadline;
  }
}