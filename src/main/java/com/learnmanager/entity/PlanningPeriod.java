package com.learnmanager.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class PlanningPeriod extends BaseEntity {

  private static final long PERIOD_LENGTH_DAYS = 180;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private User user;

  @Column(nullable = false) private LocalDate startDate;

  @Column(nullable = false) private LocalDate endDate;

  public PlanningPeriod(User user, LocalDate startDate) {
    this.user = user;
    updateStartDate(startDate);
  }

  public void updateStartDate(LocalDate startDate) {
    this.startDate = startDate;
    this.endDate = startDate.plusDays(PERIOD_LENGTH_DAYS - 1);
  }
}