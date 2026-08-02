package com.learnmanager.repository;

import com.learnmanager.entity.ModulePlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModulePlanRepository extends JpaRepository<ModulePlan, Long> {

  void deleteAllByStudyModule_Id(
      Long studyModuleId);

  void deleteAllByPlanningPeriod_Id(Long planningPeriodId);

  boolean existsByPlanningPeriod_IdAndStudyModule_IdAndPeriodNumber(Long planningPeriodId, Long studyModuleId, Integer periodNumber);

  List<ModulePlan> findAllByPlanningPeriod_User_EmailIgnoreCaseOrderByCreatedAtDesc(String email);

  List<ModulePlan> findAllByPlanningPeriod_IdOrderByPeriodNumberAscStudyModule_NameAsc(Long planningPeriodId);

  Optional<ModulePlan> findByIdAndPlanningPeriod_User_EmailIgnoreCase(Long id, String email);

  boolean existsByPlanningPeriod_IdAndStudyModule_IdAndPeriodNumberAndIdNot(
      Long planningPeriodId,
      Long studyModuleId,
      Integer periodNumber,
      Long excludedModulePlanId);

  List<ModulePlan> findAllByStudyModule_User_EmailIgnoreCaseOrderByCreatedAtDesc(String email);

  List<ModulePlan> findAllByPlanningPeriod_IdAndPeriodNumberOrderByStudyModule_NameAsc(Long planningPeriodId, Integer periodNumber);
}
