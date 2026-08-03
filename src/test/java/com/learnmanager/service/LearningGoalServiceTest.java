package com.learnmanager.service;

import com.learnmanager.dto.request.create.CreateLearningGoalRequest;
import com.learnmanager.dto.request.update.UpdateLearningGoalRequest;
import com.learnmanager.entity.*;
import com.learnmanager.entity.enums.GoalStatus;
import com.learnmanager.exception.BusinessRuleException;
import com.learnmanager.exception.ResourceNotFoundException;
import com.learnmanager.repository.LearningGoalRepository;
import com.learnmanager.testsupport.AbstractIntegrationTest;
import com.learnmanager.testsupport.TestDataFactory.CompleteTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.learnmanager.testsupport.TestDataFactory.TEST_USER_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LearningGoalServiceTest extends AbstractIntegrationTest {

  @Autowired private LearningGoalService learningGoalService;

  @Autowired private LearningGoalRepository learningGoalRepository;

  @Test
  void createShouldCreateLearningGoal() {
    User user = testDataFactory.createUser();

    StudyModule studyModule = testDataFactory.createStudyModule(user);

    CreateLearningGoalRequest request = new CreateLearningGoalRequest(
        studyModule.getId(),
                                                                      "  Complete service tests  ",
                                                                      "Create all required integration tests",
                                                                      BigDecimal.valueOf(40),
                                                                      LocalDate.of(2026, 9, 15));

    learningGoalService.create(TEST_USER_EMAIL.toUpperCase(), request);

    flushAndClear();

    List<LearningGoal> learningGoals = learningGoalRepository.findAllByStudyModule_Id(studyModule.getId());

    assertThat(learningGoals).singleElement().satisfies(learningGoal -> {
      assertThat(learningGoal.getStudyModule().getId()).isEqualTo(studyModule.getId());

      assertThat(learningGoal.getTitle()).isEqualTo("Complete service tests");

      assertThat(learningGoal.getWorkloadHours()).isEqualByComparingTo("40");

      assertThat(learningGoal.getDeadline()).isEqualTo(LocalDate.of(2026, 9, 15));
    });
  }

  @Test
  void createShouldRejectWorkloadExceedingModuleWorkload() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    CreateLearningGoalRequest request = new CreateLearningGoalRequest(
        testData.studyModule().getId(),
                                                                      "Excessive learning goal",
                                                                      null,
                                                                      BigDecimal.valueOf(121),
                                                                      LocalDate.of(2026, 10, 1));

    assertThatThrownBy(() -> learningGoalService.create(TEST_USER_EMAIL, request)).isInstanceOf(BusinessRuleException.class);

    flushAndClear();

    assertThat(learningGoalRepository.findAllByStudyModule_Id(testData.studyModule().getId())).hasSize(1);
  }

  @Test
  void getAllShouldOnlyReturnLearningGoalsOfRequestedUser() {
    User requestedUser = testDataFactory.createUser();

    StudyModule requestedUserModule = testDataFactory.createStudyModule(requestedUser);

    testDataFactory.createLearningGoal(requestedUserModule);

    User otherUser = testDataFactory.createUser("other-user@learnmanager.local");

    StudyModule otherUserModule = testDataFactory.createStudyModule(otherUser, "Other module", "OTHER01", BigDecimal.valueOf(100));

    testDataFactory.createLearningGoal(
        otherUserModule,
        "Other user goal",
        BigDecimal.valueOf(30),
        LocalDate.of(2026, 9, 1),
        GoalStatus.PLANNED);

    flushAndClear();

    assertThat(learningGoalService.getAll(TEST_USER_EMAIL.toUpperCase())).hasSize(1);
  }

  @Test
  void getAllByModuleShouldOnlyReturnLearningGoalsOfRequestedModule() {
    User user = testDataFactory.createUser();

    StudyModule firstModule = testDataFactory.createStudyModule(user);

    StudyModule secondModule = testDataFactory.createStudyModule(user, "Databases", "DB01", BigDecimal.valueOf(120));

    testDataFactory.createLearningGoal(firstModule);

    testDataFactory.createLearningGoal(
        secondModule,
        "Complete database implementation",
        BigDecimal.valueOf(40),
        LocalDate.of(2026, 9, 10),
        GoalStatus.IN_PROGRESS);

    flushAndClear();

    assertThat(learningGoalService.getAllByModule(TEST_USER_EMAIL, firstModule.getId())).hasSize(1);
  }

  @Test
  void getAllByModuleShouldRejectForeignModule() {
    testDataFactory.createUser();

    User otherUser = testDataFactory.createUser("other-user@learnmanager.local");

    StudyModule otherUserModule = testDataFactory.createStudyModule(otherUser, "Other module", "OTHER01", BigDecimal.valueOf(100));

    flushAndClear();

    assertThatThrownBy(() -> learningGoalService.getAllByModule(TEST_USER_EMAIL, otherUserModule.getId())).isInstanceOf(
        ResourceNotFoundException.class);
  }

  @Test
  void getByIdShouldReturnOwnedLearningGoal() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    flushAndClear();

    assertThat(learningGoalService.getById(TEST_USER_EMAIL.toUpperCase(), testData.learningGoal().getId())).isNotNull();
  }

  @Test
  void getByIdShouldRejectForeignLearningGoal() {
    testDataFactory.createUser();

    User otherUser = testDataFactory.createUser("other-user@learnmanager.local");

    StudyModule otherUserModule = testDataFactory.createStudyModule(otherUser, "Other module", "OTHER01", BigDecimal.valueOf(100));

    LearningGoal otherUserGoal = testDataFactory.createLearningGoal(
        otherUserModule,
        "Other user goal",
        BigDecimal.valueOf(30),
        LocalDate.of(2026, 9, 1),
        GoalStatus.PLANNED);

    flushAndClear();

    assertThatThrownBy(() -> learningGoalService.getById(
        TEST_USER_EMAIL,
        otherUserGoal.getId())).isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void updateShouldUpdateLearningGoal() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    Long learningGoalId = testData.learningGoal().getId();

    UpdateLearningGoalRequest request = new UpdateLearningGoalRequest(
        "  Updated learning goal  ",
                                                                      BigDecimal.valueOf(50),
                                                                      LocalDate.of(2026, 9, 20),
                                                                      GoalStatus.COMPLETED);

    learningGoalService.update(TEST_USER_EMAIL, learningGoalId, request);

    flushAndClear();

    LearningGoal updatedLearningGoal = entityManager.find(LearningGoal.class, learningGoalId);

    assertThat(updatedLearningGoal.getTitle()).isEqualTo("Updated learning goal");

    assertThat(updatedLearningGoal.getWorkloadHours()).isEqualByComparingTo("50");

    assertThat(updatedLearningGoal.getDeadline()).isEqualTo(LocalDate.of(2026, 9, 20));

    assertThat(updatedLearningGoal.getStatus()).isEqualTo(GoalStatus.COMPLETED);
  }

  @Test
  void updateShouldAllowWorkloadDecrease() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    Long learningGoalId = testData.learningGoal().getId();

    UpdateLearningGoalRequest request = new UpdateLearningGoalRequest(
        "Reduced learning goal",
                                                                      BigDecimal.valueOf(30),
                                                                      testData.learningGoal().getDeadline(),
                                                                      GoalStatus.IN_PROGRESS);

    learningGoalService.update(TEST_USER_EMAIL, learningGoalId, request);

    flushAndClear();

    LearningGoal updatedLearningGoal = entityManager.find(LearningGoal.class, learningGoalId);

    assertThat(updatedLearningGoal.getWorkloadHours()).isEqualByComparingTo("30");
  }

  @Test
  void updateShouldRejectWorkloadIncreaseExceedingModuleWorkload() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    testDataFactory.createLearningGoal(
        testData.studyModule(),
        "Second learning goal",
        BigDecimal.valueOf(100),
        LocalDate.of(2026, 10, 1),
        GoalStatus.PLANNED);

    UpdateLearningGoalRequest request = new UpdateLearningGoalRequest(
        "Updated learning goal",
                                                                      BigDecimal.valueOf(90),
                                                                      LocalDate.of(2026, 9, 20),
                                                                      GoalStatus.IN_PROGRESS);

    assertThatThrownBy(() -> learningGoalService.update(TEST_USER_EMAIL, testData.learningGoal().getId(), request)).isInstanceOf(
        BusinessRuleException.class);

    flushAndClear();

    LearningGoal unchangedLearningGoal = entityManager.find(LearningGoal.class, testData.learningGoal().getId());

    assertThat(unchangedLearningGoal.getWorkloadHours()).isEqualByComparingTo("60");
  }

  @Test
  void updateShouldRejectForeignLearningGoal() {
    testDataFactory.createUser();

    User otherUser = testDataFactory.createUser("other-user@learnmanager.local");

    StudyModule otherUserModule = testDataFactory.createStudyModule(otherUser, "Other module", "OTHER01", BigDecimal.valueOf(100));

    LearningGoal otherUserGoal = testDataFactory.createLearningGoal(
        otherUserModule,
        "Other user goal",
        BigDecimal.valueOf(30),
        LocalDate.of(2026, 9, 1),
        GoalStatus.PLANNED);

    UpdateLearningGoalRequest request = new UpdateLearningGoalRequest(
        "Unauthorized update",
                                                                      BigDecimal.valueOf(20),
                                                                      LocalDate.of(2026, 9, 10),
                                                                      GoalStatus.IN_PROGRESS);

    flushAndClear();

    assertThatThrownBy(() -> learningGoalService.update(TEST_USER_EMAIL, otherUserGoal.getId(), request)).isInstanceOf(
        ResourceNotFoundException.class);
  }

  @Test
  void deleteShouldDeleteLearningGoalAndDependentData() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    Long learningGoalId = testData.learningGoal().getId();

    Long milestoneId = testData.milestone().getId();

    Long studyTimeId = testData.studyTime().getId();

    learningGoalService.delete(TEST_USER_EMAIL, learningGoalId);

    flushAndClear();

    assertThat(entityManager.find(LearningGoal.class, learningGoalId)).isNull();

    assertThat(entityManager.find(Milestone.class, milestoneId)).isNull();

    assertThat(entityManager.find(StudyTime.class, studyTimeId)).isNull();
  }

  @Test
  void deleteShouldRejectForeignLearningGoal() {
    testDataFactory.createUser();

    User otherUser = testDataFactory.createUser("other-user@learnmanager.local");

    StudyModule otherUserModule = testDataFactory.createStudyModule(otherUser, "Other module", "OTHER01", BigDecimal.valueOf(100));

    LearningGoal otherUserGoal = testDataFactory.createLearningGoal(
        otherUserModule,
        "Other user goal",
        BigDecimal.valueOf(30),
        LocalDate.of(2026, 9, 1),
        GoalStatus.PLANNED);

    Long learningGoalId = otherUserGoal.getId();

    flushAndClear();

    assertThatThrownBy(() -> learningGoalService.delete(TEST_USER_EMAIL, learningGoalId)).isInstanceOf(ResourceNotFoundException.class);

    assertThat(entityManager.find(LearningGoal.class, learningGoalId)).isNotNull();
  }
}