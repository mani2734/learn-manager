package com.learnmanager.service;

import com.learnmanager.dto.CreateModulePlanRequest;
import com.learnmanager.dto.ModulePlanResponse;
import com.learnmanager.entity.ModulePlan;
import com.learnmanager.entity.PlanningPeriod;
import com.learnmanager.entity.StudyModule;
import com.learnmanager.exception.BusinessRuleException;
import com.learnmanager.repository.ModulePlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ModulePlanService {

  private final ModulePlanRepository modulePlanRepository;

  private final HelperService helperService;

  @Transactional
  public ModulePlanResponse create(String userEmail, CreateModulePlanRequest request) {
    PlanningPeriod planningPeriod = helperService.findOwnedPlanningPeriod(userEmail, request.planningPeriodId());

    StudyModule studyModule = helperService.findOwnedStudyModule(userEmail, request.studyModuleId());

    validateModulePlanDoesNotExist(planningPeriod.getId(), studyModule.getId(), request.periodNumber());

    return ModulePlanResponse.fromEntity(modulePlanRepository.save(new ModulePlan(
        planningPeriod,
                                                                                  studyModule,
                                                                                  request.periodNumber(),
                                                                                  request.plannedHours())));
  }

  private void validateModulePlanDoesNotExist(Long planningPeriodId, Long studyModuleId, Integer periodNumber) {
    boolean modulePlanExists = modulePlanRepository.existsByPlanningPeriod_IdAndStudyModule_IdAndPeriodNumber(
        planningPeriodId,
        studyModuleId,
        periodNumber);

    if (modulePlanExists) {
      throw new BusinessRuleException("A module plan already exists for this module and period number");
    }
  }
}