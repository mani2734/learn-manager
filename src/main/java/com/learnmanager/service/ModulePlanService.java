package com.learnmanager.service;

import com.learnmanager.dto.request.create.CreateModulePlanRequest;
import com.learnmanager.dto.request.update.UpdateModulePlanRequest;
import com.learnmanager.dto.response.ModulePlanResponse;
import com.learnmanager.entity.ModulePlan;
import com.learnmanager.entity.PlanningPeriod;
import com.learnmanager.entity.StudyModule;
import com.learnmanager.exception.BusinessRuleException;
import com.learnmanager.exception.ResourceNotFoundException;
import com.learnmanager.repository.ModulePlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

  @Transactional(readOnly = true)
  public List<ModulePlanResponse> getAll(String userEmail) {
    return modulePlanRepository.findAllByPlanningPeriod_User_EmailIgnoreCaseOrderByCreatedAtDesc(helperService.normalizeEmail(userEmail))
                               .stream()
                               .map(ModulePlanResponse::fromEntity)
                               .toList();
  }

  @Transactional(readOnly = true)
  public List<ModulePlanResponse> getAllByPlanningPeriod(String userEmail, Long planningPeriodId) {
    PlanningPeriod planningPeriod = helperService.findOwnedPlanningPeriod(userEmail, planningPeriodId);

    return modulePlanRepository.findAllByPlanningPeriod_IdOrderByPeriodNumberAscStudyModule_NameAsc(planningPeriod.getId())
                               .stream()
                               .map(ModulePlanResponse::fromEntity)
                               .toList();
  }

  @Transactional(readOnly = true)
  public ModulePlanResponse getById(String userEmail, Long modulePlanId) {
    return ModulePlanResponse.fromEntity(findOwnedModulePlan(userEmail, modulePlanId));
  }

  @Transactional
  public ModulePlanResponse update(String userEmail, Long modulePlanId, UpdateModulePlanRequest request) {
    ModulePlan modulePlan = findOwnedModulePlan(userEmail, modulePlanId);

    validateModulePlanDoesNotExistForUpdate(
        modulePlan.getPlanningPeriod().getId(),
        modulePlan.getStudyModule().getId(),
        request.periodNumber(),
        modulePlanId);

    modulePlan.update(request.periodNumber(), request.plannedHours());

    return ModulePlanResponse.fromEntity(modulePlanRepository.save(modulePlan));
  }

  @Transactional
  public void delete(String userEmail, Long modulePlanId) {
    modulePlanRepository.delete(findOwnedModulePlan(userEmail, modulePlanId));
  }

  private ModulePlan findOwnedModulePlan(String userEmail, Long modulePlanId) {
    return modulePlanRepository.findByIdAndPlanningPeriod_User_EmailIgnoreCase(modulePlanId, helperService.normalizeEmail(userEmail))
                               .orElseThrow(() -> new ResourceNotFoundException("Module plan not found"));
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

  private void validateModulePlanDoesNotExistForUpdate(Long planningPeriodId, Long studyModuleId, Integer periodNumber, Long modulePlanId) {
    boolean modulePlanExists = modulePlanRepository.existsByPlanningPeriod_IdAndStudyModule_IdAndPeriodNumberAndIdNot(
        planningPeriodId,
        studyModuleId,
        periodNumber,
        modulePlanId);

    if (modulePlanExists) {
      throw new BusinessRuleException("A module plan already exists for this module and period number");
    }
  }

}