package com.learnmanager.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ModulePlan extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private PlanningPeriod planningPeriod;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private StudyModule studyModule;

  @Column(nullable = false) private Integer periodNumber;

  @Column(nullable = false, precision = 8, scale = 2) private BigDecimal plannedHours;

  public ModulePlan(PlanningPeriod planningPeriod, StudyModule studyModule, Integer periodNumber, BigDecimal plannedHours) {
    this.planningPeriod = planningPeriod;
    this.studyModule = studyModule;
    this.periodNumber = periodNumber;
    this.plannedHours = plannedHours;
  }
}