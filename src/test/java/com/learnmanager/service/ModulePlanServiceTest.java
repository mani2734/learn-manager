package com.learnmanager.service;

import com.learnmanager.dto.request.create.CreateModulePlanRequest;
import com.learnmanager.dto.request.update.UpdateModulePlanRequest;
import com.learnmanager.entity.ModulePlan;
import com.learnmanager.entity.StudyModule;
import com.learnmanager.entity.User;
import com.learnmanager.exception.BusinessRuleException;
import com.learnmanager.exception.ResourceNotFoundException;
import com.learnmanager.repository.ModulePlanRepository;
import com.learnmanager.testsupport.AbstractIntegrationTest;
import com.learnmanager.testsupport.TestDataFactory.CompleteTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static com.learnmanager.testsupport.TestDataFactory.TEST_USER_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ModulePlanServiceTest extends AbstractIntegrationTest {

  @Autowired private ModulePlanService modulePlanService;

  @Autowired private ModulePlanRepository modulePlanRepository;

  @Test
  void createShouldCreateModulePlan() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    CreateModulePlanRequest request = new CreateModulePlanRequest(
        testData.planningPeriod().getId(),
                                                                  testData.studyModule().getId(),
                                                                  2,
                                                                  BigDecimal.valueOf(25));

    modulePlanService.create(TEST_USER_EMAIL.toUpperCase(), request);

    flushAndClear();

    assertThat(modulePlanRepository.findAllByStudyModule_User_EmailIgnoreCaseOrderByCreatedAtDesc(TEST_USER_EMAIL)).filteredOn(modulePlan -> modulePlan.getPeriodNumber()
                                                                                                                                                       .equals(
                                                                                                                                                           2))
                                                                                                                   .singleElement()
                                                                                                                   .satisfies(modulePlan -> assertThat(
                                                                                                                       modulePlan.getPlannedHours()).isEqualByComparingTo(
                                                                                                                       "25"));
  }

  @Test
  void createShouldRejectDuplicateModulePlanForPeriod() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    CreateModulePlanRequest request = new CreateModulePlanRequest(
        testData.planningPeriod().getId(),
                                                                  testData.studyModule().getId(),
                                                                  testData.modulePlan().getPeriodNumber(),
                                                                  BigDecimal.valueOf(25));

    assertThatThrownBy(() -> modulePlanService.create(TEST_USER_EMAIL, request)).isInstanceOf(BusinessRuleException.class)
                                                                                .hasMessage(
                                                                                    "A module plan already exists for this module and period number");
  }

  @Test
  void createShouldRejectForeignStudyModule() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    User otherUser = testDataFactory.createUser("other-user@learnmanager.local");
    StudyModule otherModule = testDataFactory.createStudyModule(otherUser, "Other module", "OTHER01", BigDecimal.valueOf(100));

    CreateModulePlanRequest request = new CreateModulePlanRequest(
        testData.planningPeriod().getId(),
        otherModule.getId(),
        2,
        BigDecimal.TEN);

    flushAndClear();

    assertThatThrownBy(() -> modulePlanService.create(TEST_USER_EMAIL, request)).isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void getAllShouldOnlyReturnModulePlansOfRequestedUser() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    User otherUser = testDataFactory.createUser("other-user@learnmanager.local");
    StudyModule otherModule = testDataFactory.createStudyModule(otherUser, "Other module", "OTHER01", BigDecimal.valueOf(100));
    modulePlanRepository.save(new ModulePlan(testDataFactory.createPlanningPeriod(otherUser), otherModule, 1, BigDecimal.TEN));

    flushAndClear();

    assertThat(modulePlanService.getAll(TEST_USER_EMAIL.toUpperCase())).hasSize(1);
    assertThat(modulePlanService.getAllByPlanningPeriod(TEST_USER_EMAIL, testData.planningPeriod().getId())).hasSize(1);
  }

  @Test
  void updateShouldUpdateModulePlan() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    UpdateModulePlanRequest request = new UpdateModulePlanRequest(3, BigDecimal.valueOf(45));

    modulePlanService.update(TEST_USER_EMAIL, testData.modulePlan().getId(), request);

    flushAndClear();

    ModulePlan updatedModulePlan = entityManager.find(ModulePlan.class, testData.modulePlan().getId());

    assertThat(updatedModulePlan.getPeriodNumber()).isEqualTo(3);
    assertThat(updatedModulePlan.getPlannedHours()).isEqualByComparingTo("45");
  }

  @Test
  void deleteShouldDeleteOwnedModulePlan() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    Long modulePlanId = testData.modulePlan().getId();

    modulePlanService.delete(TEST_USER_EMAIL, modulePlanId);

    flushAndClear();

    assertThat(entityManager.find(ModulePlan.class, modulePlanId)).isNull();
  }

}
