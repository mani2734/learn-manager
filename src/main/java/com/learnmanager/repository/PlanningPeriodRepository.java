package com.learnmanager.repository;

import com.learnmanager.entity.PlanningPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PlanningPeriodRepository extends JpaRepository<PlanningPeriod, Long> {

  boolean existsByUser_EmailIgnoreCaseAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
      String email,
      LocalDate newEndDate,
      LocalDate newStartDate);

  List<PlanningPeriod> findAllByUser_EmailIgnoreCaseOrderByStartDateDesc(String email);

  Optional<PlanningPeriod> findByIdAndUser_EmailIgnoreCase(Long id, String email);

  boolean existsByUser_EmailIgnoreCaseAndIdNotAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
      String email,
      Long excludedPlanningPeriodId,
      LocalDate newEndDate,
      LocalDate newStartDate);
}
