package com.learnmanager.repository;

import com.learnmanager.entity.ModulePlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModulePlanRepository extends JpaRepository<ModulePlan, Long> {

  void deleteAllByStudyModule_Id(
      Long studyModuleId);

  void deleteAllByPlanningPeriod_Id(Long planningPeriodId);

  boolean existsByPlanningPeriod_IdAndStudyModule_IdAndPeriodNumber(Long planningPeriodId, Long studyModuleId, Integer periodNumber);
}
