package com.learnmanager.repository;

import com.learnmanager.entity.PlanningPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface PlanningPeriodRepository extends JpaRepository<PlanningPeriod, Long> {

  boolean existsByUser_EmailIgnoreCaseAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
      String email,
      LocalDate newEndDate,
      LocalDate newStartDate);
}
