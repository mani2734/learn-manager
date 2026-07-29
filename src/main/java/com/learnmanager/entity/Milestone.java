package com.learnmanager.entity;

import com.learnmanager.entity.enums.GoalStatus;
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

  private LocalDate deadline;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private GoalStatus status = GoalStatus.PLANNED;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private LearningGoal learningGoal;

  public Milestone(LearningGoal learningGoal, String title, LocalDate deadline) {
    this.learningGoal = learningGoal;
    this.title = title;
    this.deadline = deadline;
  }
}