package com.learnmanager.service;

import com.learnmanager.dto.CreatePlanningPeriodRequest;
import com.learnmanager.dto.PlanningPeriodResponse;
import com.learnmanager.entity.PlanningPeriod;
import com.learnmanager.entity.User;
import com.learnmanager.exception.BusinessRuleException;
import com.learnmanager.repository.PlanningPeriodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanningPeriodService {

  private static final long PERIOD_LENGTH_DAYS = 180;

  private final PlanningPeriodRepository planningPeriodRepository;

  private final HelperService helperService;

  @Transactional
  public PlanningPeriodResponse create(String userEmail, CreatePlanningPeriodRequest request) {
    User user = helperService.findUserByEmail(userEmail);

    LocalDate startDate = request.startDate();
    LocalDate endDate = startDate.plusDays(PERIOD_LENGTH_DAYS - 1);

    validateNoOverlap(user.getEmail(), startDate, endDate);

    return PlanningPeriodResponse.fromEntity(planningPeriodRepository.save(new PlanningPeriod(user, startDate)));
  }

  @Transactional(readOnly = true)
  public List<PlanningPeriodResponse> getAll(String userEmail) {
    return planningPeriodRepository.findAllByUser_EmailIgnoreCaseOrderByStartDateDesc(helperService.normalizeEmail(userEmail))
                                   .stream()
                                   .map(PlanningPeriodResponse::fromEntity)
                                   .toList();
  }

  @Transactional(readOnly = true)
  public PlanningPeriodResponse getById(String userEmail, Long planningPeriodId) {
    return PlanningPeriodResponse.fromEntity(helperService.findOwnedPlanningPeriod(userEmail, planningPeriodId));
  }

  private void validateNoOverlap(String userEmail, LocalDate startDate, LocalDate endDate) {
    boolean overlapsExistingPeriod = planningPeriodRepository.existsByUser_EmailIgnoreCaseAndStartDateLessThanEqualAndEndDateGreaterThanEqual(userEmail,
                                                                                                                                              endDate,
                                                                                                                                              startDate);

    if (overlapsExistingPeriod) {
      throw new BusinessRuleException("The planning period overlaps an existing planning period");
    }
  }
}