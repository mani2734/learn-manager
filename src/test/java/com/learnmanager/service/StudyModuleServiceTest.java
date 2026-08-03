package com.learnmanager.service;

import com.learnmanager.dto.request.create.CreateStudyModuleRequest;
import com.learnmanager.dto.request.update.UpdateStudyModuleRequest;
import com.learnmanager.entity.StudyModule;
import com.learnmanager.entity.User;
import com.learnmanager.exception.BusinessRuleException;
import com.learnmanager.exception.ResourceNotFoundException;
import com.learnmanager.repository.StudyModuleRepository;
import com.learnmanager.testsupport.AbstractIntegrationTest;
import com.learnmanager.testsupport.TestDataFactory.CompleteTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;

import static com.learnmanager.testsupport.TestDataFactory.TEST_USER_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StudyModuleServiceTest extends AbstractIntegrationTest {

  @Autowired private StudyModuleService studyModuleService;

  @Autowired private StudyModuleRepository studyModuleRepository;

  @Test
  void createShouldCreateStudyModule() {
    testDataFactory.createUser();

    CreateStudyModuleRequest request = new CreateStudyModuleRequest("  Algorithms  ", "  CS01  ", BigDecimal.valueOf(120));

    studyModuleService.create(TEST_USER_EMAIL.toUpperCase(), request);

    flushAndClear();

    assertThat(studyModuleRepository.findAllByUserEmailIgnoreCaseOrderByCreatedAtDesc(TEST_USER_EMAIL)).singleElement()
                                                                                                       .satisfies(studyModule -> {
                                                                                                         assertThat(studyModule.getName()).isEqualTo(
                                                                                                             "Algorithms");
                                                                                                         assertThat(studyModule.getCode()).isEqualTo(
                                                                                                             "CS01");
                                                                                                         assertThat(studyModule.getWorkloadHours()).isEqualByComparingTo(
                                                                                                             "120");
                                                                                                       });
  }

  @Test
  void createShouldRejectMissingUser() {
    CreateStudyModuleRequest request = new CreateStudyModuleRequest("Algorithms", null, BigDecimal.valueOf(120));

    assertThatThrownBy(() -> studyModuleService.create(TEST_USER_EMAIL, request)).isInstanceOf(UsernameNotFoundException.class);
  }

  @Test
  void getAllShouldOnlyReturnStudyModulesOfRequestedUser() {
    User user = testDataFactory.createUser();
    testDataFactory.createStudyModule(user);

    User otherUser = testDataFactory.createUser("other-user@learnmanager.local");
    testDataFactory.createStudyModule(otherUser, "Other module", "OTHER01", BigDecimal.valueOf(100));

    flushAndClear();

    assertThat(studyModuleService.getAll(TEST_USER_EMAIL.toUpperCase())).hasSize(1);
  }

  @Test
  void getByIdShouldRejectForeignStudyModule() {
    testDataFactory.createUser();

    User otherUser = testDataFactory.createUser("other-user@learnmanager.local");
    StudyModule otherModule = testDataFactory.createStudyModule(otherUser, "Other module", "OTHER01", BigDecimal.valueOf(100));

    flushAndClear();

    assertThatThrownBy(() -> studyModuleService.getById(
        TEST_USER_EMAIL,
        otherModule.getId())).isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void updateShouldUpdateStudyModule() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    UpdateStudyModuleRequest request = new UpdateStudyModuleRequest("  Updated module  ", "  UPD01  ", BigDecimal.valueOf(120));

    studyModuleService.update(TEST_USER_EMAIL, testData.studyModule().getId(), request);

    flushAndClear();

    StudyModule updatedStudyModule = entityManager.find(StudyModule.class, testData.studyModule().getId());

    assertThat(updatedStudyModule.getName()).isEqualTo("Updated module");
    assertThat(updatedStudyModule.getCode()).isEqualTo("UPD01");
    assertThat(updatedStudyModule.getWorkloadHours()).isEqualByComparingTo("120");
  }

  @Test
  void updateShouldRejectWorkloadBelowAssignedLearningGoalWorkload() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    UpdateStudyModuleRequest request = new UpdateStudyModuleRequest("Updated module", null, BigDecimal.valueOf(50));

    assertThatThrownBy(() -> studyModuleService.update(TEST_USER_EMAIL, testData.studyModule().getId(), request)).isInstanceOf(
        BusinessRuleException.class);
  }

  @Test
  void deleteShouldDeleteStudyModuleAndDependentData() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    Long studyModuleId = testData.studyModule().getId();

    studyModuleService.delete(TEST_USER_EMAIL, studyModuleId);

    flushAndClear();

    assertThat(entityManager.find(StudyModule.class, studyModuleId)).isNull();
  }

}
