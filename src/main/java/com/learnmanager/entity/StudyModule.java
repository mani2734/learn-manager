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

  @Column(length = 2000) private String description;

  @Column(nullable = false) private Integer ects;

  @Column(nullable = false, precision = 8, scale = 2) private BigDecimal workloadHours;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private User user;

  public StudyModule(User user, String name, String code, String description, Integer ects, BigDecimal workloadHours) {
    this.user = user;
    this.name = name;
    this.code = code;
    this.description = description;
    this.ects = ects;
    this.workloadHours = workloadHours;
  }
}