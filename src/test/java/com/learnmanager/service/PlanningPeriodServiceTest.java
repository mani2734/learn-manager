package com.learnmanager.service;

import com.learnmanager.dto.request.create.CreatePlanningPeriodRequest;
import com.learnmanager.dto.request.update.UpdatePlanningPeriodRequest;
import com.learnmanager.entity.ModulePlan;
import com.learnmanager.entity.PlanningPeriod;
import com.learnmanager.entity.StudyModule;
import com.learnmanager.entity.User;
import com.learnmanager.exception.BusinessRuleException;
import com.learnmanager.exception.ResourceNotFoundException;
import com.learnmanager.repository.PlanningPeriodRepository;
import com.learnmanager.testsupport.AbstractIntegrationTest;
import com.learnmanager.testsupport.TestDataFactory.CompleteTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.learnmanager.testsupport.TestDataFactory.CURRENT_DATE;
import static com.learnmanager.testsupport.TestDataFactory.TEST_USER_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlanningPeriodServiceTest extends AbstractIntegrationTest {

  private static final String OTHER_USER_EMAIL = "other-user@learnmanager.local";

  @Autowired private PlanningPeriodService planningPeriodService;

  @Autowired private PlanningPeriodRepository planningPeriodRepository;

  @Test
  void createShouldCreatePlanningPeriodWithExactly180Days() {
    User user = testDataFactory.createUser();

    LocalDate startDate = CURRENT_DATE.plusDays(10);

    CreatePlanningPeriodRequest request = new CreatePlanningPeriodRequest(startDate);

    planningPeriodService.create(TEST_USER_EMAIL.toUpperCase(), request);

    flushAndClear();

    PlanningPeriod planningPeriod = planningPeriodRepository.findAllByUser_EmailIgnoreCaseOrderByStartDateDesc(TEST_USER_EMAIL).getFirst();

    assertThat(planningPeriod.getUser().getId()).isEqualTo(user.getId());

    assertThat(planningPeriod.getStartDate()).isEqualTo(startDate);

    assertThat(planningPeriod.getEndDate()).isEqualTo(startDate.plusDays(179));
  }

  @Test
  void createShouldRejectOverlappingPlanningPeriod() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    LocalDate overlappingStartDate = testData.planningPeriod().getStartDate().plusDays(30);

    CreatePlanningPeriodRequest request = new CreatePlanningPeriodRequest(overlappingStartDate);

    assertThatThrownBy(() -> planningPeriodService.create(TEST_USER_EMAIL, request)).isInstanceOf(BusinessRuleException.class)
                                                                                    .hasMessage(
                                                                                        "The planning period overlaps an existing planning period");
  }

  @Test
  void createShouldRejectPlanningPeriodStartingOnExistingEndDate() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    CreatePlanningPeriodRequest request = new CreatePlanningPeriodRequest(testData.planningPeriod().getEndDate());

    assertThatThrownBy(() -> planningPeriodService.create(TEST_USER_EMAIL, request)).isInstanceOf(BusinessRuleException.class)
                                                                                    .hasMessage(
                                                                                        "The planning period overlaps an existing planning period");
  }

  @Test
  void createShouldRejectPlanningPeriodEndingOnExistingStartDate() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    LocalDate newStartDate = testData.planningPeriod().getStartDate().minusDays(179);

    CreatePlanningPeriodRequest request = new CreatePlanningPeriodRequest(newStartDate);

    assertThatThrownBy(() -> planningPeriodService.create(TEST_USER_EMAIL, request)).isInstanceOf(BusinessRuleException.class)
                                                                                    .hasMessage(
                                                                                        "The planning period overlaps an existing planning period");
  }

  @Test
  void createShouldAllowPlanningPeriodStartingDayAfterExistingEndDate() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    LocalDate startDate = testData.planningPeriod().getEndDate().plusDays(1);

    planningPeriodService.create(TEST_USER_EMAIL, new CreatePlanningPeriodRequest(startDate));

    flushAndClear();

    assertThat(planningPeriodRepository.findAllByUser_EmailIgnoreCaseOrderByStartDateDesc(TEST_USER_EMAIL)).hasSize(2)
                                                                                                           .extracting(PlanningPeriod::getStartDate)
                                                                                                           .contains(startDate);
  }

  @Test
  void createShouldAllowPlanningPeriodEndingDayBeforeExistingStartDate() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    LocalDate startDate = testData.planningPeriod().getStartDate().minusDays(180);

    planningPeriodService.create(TEST_USER_EMAIL, new CreatePlanningPeriodRequest(startDate));

    flushAndClear();

    assertThat(planningPeriodRepository.findAllByUser_EmailIgnoreCaseOrderByStartDateDesc(TEST_USER_EMAIL)).hasSize(2)
                                                                                                           .extracting(PlanningPeriod::getStartDate)
                                                                                                           .contains(startDate);
  }

  @Test
  void createShouldAllowOverlappingDatesForDifferentUsers() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    User otherUser = testDataFactory.createUser(OTHER_USER_EMAIL);

    LocalDate startDate = testData.planningPeriod().getStartDate();

    planningPeriodService.create(OTHER_USER_EMAIL.toUpperCase(), new CreatePlanningPeriodRequest(startDate));

    flushAndClear();

    assertThat(planningPeriodRepository.findAllByUser_EmailIgnoreCaseOrderByStartDateDesc(OTHER_USER_EMAIL)).singleElement()
                                                                                                            .satisfies(planningPeriod -> {
                                                                                                              assertThat(planningPeriod.getUser()
                                                                                                                                       .getId()).isEqualTo(
                                                                                                                  otherUser.getId());

                                                                                                              assertThat(planningPeriod.getStartDate()).isEqualTo(
                                                                                                                  startDate);
                                                                                                            });
  }

  @Test
  void getAllShouldOnlyReturnPlanningPeriodsOfRequestedUser() {
    User user = testDataFactory.createUser();

    testDataFactory.createPlanningPeriod(user, CURRENT_DATE.minusDays(400), CURRENT_DATE.minusDays(221));

    testDataFactory.createPlanningPeriod(user, CURRENT_DATE.minusDays(100), CURRENT_DATE.plusDays(79));

    User otherUser = testDataFactory.createUser(OTHER_USER_EMAIL);

    testDataFactory.createPlanningPeriod(otherUser, CURRENT_DATE.minusDays(100), CURRENT_DATE.plusDays(79));

    flushAndClear();

    assertThat(planningPeriodService.getAll(TEST_USER_EMAIL.toUpperCase())).hasSize(2)
                                                                           .extracting("startDate")
                                                                           .containsExactly(
                                                                               CURRENT_DATE.minusDays(100),
                                                                               CURRENT_DATE.minusDays(400));
  }

  @Test
  void getByIdShouldReturnOwnedPlanningPeriod() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    Long planningPeriodId = testData.planningPeriod().getId();

    flushAndClear();

    assertThat(planningPeriodService.getById(TEST_USER_EMAIL.toUpperCase(), planningPeriodId)).extracting("id").isEqualTo(planningPeriodId);
  }

  @Test
  void getByIdShouldRejectForeignPlanningPeriod() {
    testDataFactory.createUser();

    User otherUser = testDataFactory.createUser(OTHER_USER_EMAIL);

    PlanningPeriod foreignPlanningPeriod = testDataFactory.createPlanningPeriod(otherUser, CURRENT_DATE, CURRENT_DATE.plusDays(179));

    Long planningPeriodId = foreignPlanningPeriod.getId();

    flushAndClear();

    assertThatThrownBy(() -> planningPeriodService.getById(
        TEST_USER_EMAIL,
        planningPeriodId)).isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void getByIdShouldRejectUnknownPlanningPeriod() {
    testDataFactory.createUser();

    assertThatThrownBy(() -> planningPeriodService.getById(TEST_USER_EMAIL, Long.MAX_VALUE)).isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void updateShouldUpdateStartAndEndDate() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    Long planningPeriodId = testData.planningPeriod().getId();

    LocalDate updatedStartDate = CURRENT_DATE.plusDays(200);

    UpdatePlanningPeriodRequest request = new UpdatePlanningPeriodRequest(updatedStartDate);

    planningPeriodService.update(TEST_USER_EMAIL, planningPeriodId, request);

    flushAndClear();

    PlanningPeriod updatedPlanningPeriod = entityManager.find(PlanningPeriod.class, planningPeriodId);

    assertThat(updatedPlanningPeriod.getStartDate()).isEqualTo(updatedStartDate);

    assertThat(updatedPlanningPeriod.getEndDate()).isEqualTo(updatedStartDate.plusDays(179));
  }

  @Test
  void updateShouldAllowUnchangedStartDate() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    Long planningPeriodId = testData.planningPeriod().getId();

    LocalDate originalStartDate = testData.planningPeriod().getStartDate();

    planningPeriodService.update(TEST_USER_EMAIL, planningPeriodId, new UpdatePlanningPeriodRequest(originalStartDate));

    flushAndClear();

    PlanningPeriod planningPeriod = entityManager.find(PlanningPeriod.class, planningPeriodId);

    assertThat(planningPeriod.getStartDate()).isEqualTo(originalStartDate);

    assertThat(planningPeriod.getEndDate()).isEqualTo(originalStartDate.plusDays(179));
  }

  @Test
  void updateShouldRejectOverlapWithAnotherPlanningPeriod() {
    User user = testDataFactory.createUser();

    PlanningPeriod planningPeriodToUpdate = testDataFactory.createPlanningPeriod(
        user,
        CURRENT_DATE.minusDays(400),
        CURRENT_DATE.minusDays(221));

    PlanningPeriod existingPlanningPeriod = testDataFactory.createPlanningPeriod(user, CURRENT_DATE, CURRENT_DATE.plusDays(179));

    Long planningPeriodId = planningPeriodToUpdate.getId();

    LocalDate overlappingStartDate = existingPlanningPeriod.getStartDate().minusDays(30);

    UpdatePlanningPeriodRequest request = new UpdatePlanningPeriodRequest(overlappingStartDate);

    flushAndClear();

    assertThatThrownBy(() -> planningPeriodService.update(
        TEST_USER_EMAIL,
        planningPeriodId,
        request)).isInstanceOf(BusinessRuleException.class)
                 .hasMessage("The planning period overlaps an existing planning period");
  }

  @Test
  void updateShouldAllowAdjacentPlanningPeriod() {
    User user = testDataFactory.createUser();

    PlanningPeriod planningPeriodToUpdate = testDataFactory.createPlanningPeriod(
        user,
        CURRENT_DATE.minusDays(400),
        CURRENT_DATE.minusDays(221));

    PlanningPeriod existingPlanningPeriod = testDataFactory.createPlanningPeriod(user, CURRENT_DATE, CURRENT_DATE.plusDays(179));

    Long planningPeriodId = planningPeriodToUpdate.getId();

    LocalDate updatedStartDate = existingPlanningPeriod.getStartDate().minusDays(180);

    flushAndClear();

    planningPeriodService.update(TEST_USER_EMAIL, planningPeriodId, new UpdatePlanningPeriodRequest(updatedStartDate));

    flushAndClear();

    PlanningPeriod updatedPlanningPeriod = entityManager.find(PlanningPeriod.class, planningPeriodId);

    assertThat(updatedPlanningPeriod.getEndDate()).isEqualTo(existingPlanningPeriod.getStartDate().minusDays(1));
  }

  @Test
  void updateShouldRejectForeignPlanningPeriod() {
    testDataFactory.createUser();

    User otherUser = testDataFactory.createUser(OTHER_USER_EMAIL);

    PlanningPeriod foreignPlanningPeriod = testDataFactory.createPlanningPeriod(otherUser, CURRENT_DATE, CURRENT_DATE.plusDays(179));

    Long planningPeriodId = foreignPlanningPeriod.getId();

    flushAndClear();

    assertThatThrownBy(() -> planningPeriodService.update(
        TEST_USER_EMAIL,
        planningPeriodId,
        new UpdatePlanningPeriodRequest(CURRENT_DATE.plusDays(200)))).isInstanceOf(
        ResourceNotFoundException.class);
  }

  @Test
  void deleteShouldDeletePlanningPeriodAndModulePlans() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    Long planningPeriodId = testData.planningPeriod().getId();

    Long modulePlanId = testData.modulePlan().getId();

    planningPeriodService.delete(TEST_USER_EMAIL, planningPeriodId);

    flushAndClear();

    assertThat(entityManager.find(PlanningPeriod.class, planningPeriodId)).isNull();

    assertThat(entityManager.find(ModulePlan.class, modulePlanId)).isNull();
  }

  @Test
  void deleteShouldDeleteAllModulePlansOfPlanningPeriod() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    StudyModule secondModule = testDataFactory.createStudyModule(testData.user(), "Databases", "DB01", BigDecimal.valueOf(120));

    ModulePlan secondModulePlan = testDataFactory.createModulePlan(testData.planningPeriod(), secondModule, 2, BigDecimal.valueOf(20));

    Long planningPeriodId = testData.planningPeriod().getId();

    Long firstModulePlanId = testData.modulePlan().getId();

    Long secondModulePlanId = secondModulePlan.getId();

    planningPeriodService.delete(TEST_USER_EMAIL, planningPeriodId);

    flushAndClear();

    assertThat(entityManager.find(ModulePlan.class, firstModulePlanId)).isNull();

    assertThat(entityManager.find(ModulePlan.class, secondModulePlanId)).isNull();

    assertThat(entityManager.find(PlanningPeriod.class, planningPeriodId)).isNull();
  }

  @Test
  void deleteShouldRejectForeignPlanningPeriod() {
    testDataFactory.createUser();

    User otherUser = testDataFactory.createUser(OTHER_USER_EMAIL);

    PlanningPeriod foreignPlanningPeriod = testDataFactory.createPlanningPeriod(otherUser, CURRENT_DATE, CURRENT_DATE.plusDays(179));

    Long planningPeriodId = foreignPlanningPeriod.getId();

    flushAndClear();

    assertThatThrownBy(() -> planningPeriodService.delete(TEST_USER_EMAIL, planningPeriodId)).isInstanceOf(ResourceNotFoundException.class);

    assertThat(entityManager.find(PlanningPeriod.class, planningPeriodId)).isNotNull();
  }
}