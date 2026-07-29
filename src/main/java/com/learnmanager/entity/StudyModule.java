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
@Table
public class StudyModule extends BaseEntity {

  @Column(nullable = false, length = 150) private String name;

  @Column(length = 50) private String code;

  @Column(nullable = false, precision = 8, scale = 2) private BigDecimal workloadHours;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private User user;

  public StudyModule(User user, String name, String code, BigDecimal workloadHours) {
    this.user = user;
    this.name = name;
    this.code = code;
    this.workloadHours = workloadHours;
  }
}