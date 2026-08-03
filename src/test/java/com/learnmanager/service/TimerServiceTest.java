package com.learnmanager.service;

import com.learnmanager.dto.request.create.StartTimerRequest;
import com.learnmanager.entity.*;
import com.learnmanager.entity.enums.GoalStatus;
import com.learnmanager.exception.BusinessRuleException;
import com.learnmanager.exception.ResourceNotFoundException;
import com.learnmanager.repository.StudyTimeRepository;
import com.learnmanager.repository.TimerRepository;
import com.learnmanager.testsupport.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static com.learnmanager.testsupport.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimerServiceTest extends AbstractIntegrationTest {

  @Autowired private TimerService timerService;

  @Autowired private TimerRepository timerRepository;

  @Autowired private StudyTimeRepository studyTimeRepository;

  @Test
  void startShouldCreateTimer() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    StartTimerRequest request = new StartTimerRequest(
        testData.studyModule().getId(),
                                                      testData.learningGoal().getId(),
                                                      testData.plannedStudySession().getId());

    timerService.start(TEST_USER_EMAIL.toUpperCase(), request);

    flushAndClear();

    assertThat(timerRepository.findByUser_EmailIgnoreCase(TEST_USER_EMAIL)).hasValueSatisfying(timer -> {
      assertThat(timer.getStudyModule().getId()).isEqualTo(testData.studyModule().getId());
      assertThat(timer.getLearningGoal().getId()).isEqualTo(testData.learningGoal().getId());
      assertThat(timer.getPlannedStudySession().getId()).isEqualTo(testData.plannedStudySession().getId());
      assertThat(timer.getStartTime()).isNotNull();
    });
  }

  @Test
  void startShouldRejectExistingActiveTimer() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    entityManager.persist(new Timer(testData.user(), testData.studyModule(), null, null, CURRENT_DATE_TIME));
    flushAndClear();

    StartTimerRequest request = new StartTimerRequest(testData.studyModule().getId(), null, null);

    assertThatThrownBy(() -> timerService.start(TEST_USER_EMAIL, request)).isInstanceOf(BusinessRuleException.class)
                                                                          .hasMessage("An active timer already exists");
  }

  @Test
  void startShouldRejectLearningGoalFromDifferentModule() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    StudyModule otherModule = testDataFactory.createStudyModule(testData.user(), "Databases", "DB01", BigDecimal.valueOf(100));
    LearningGoal otherGoal = testDataFactory.createLearningGoal(
        otherModule,
        "Other goal",
        BigDecimal.valueOf(30),
        CURRENT_DATE.plusDays(20),
        GoalStatus.IN_PROGRESS);

    StartTimerRequest request = new StartTimerRequest(testData.studyModule().getId(), otherGoal.getId(), null);

    assertThatThrownBy(() -> timerService.start(TEST_USER_EMAIL, request)).isInstanceOf(BusinessRuleException.class)
                                                                          .hasMessage(
                                                                              "Learning goal does not belong to the selected study module");
  }

  @Test
  void startShouldRejectPlannedSessionFromDifferentModule() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    StudyModule otherModule = testDataFactory.createStudyModule(testData.user(), "Databases", "DB01", BigDecimal.valueOf(100));
    PlannedStudySession otherSession = testDataFactory.createPlannedStudySession(testData.user(), otherModule);

    StartTimerRequest request = new StartTimerRequest(testData.studyModule().getId(), null, otherSession.getId());

    assertThatThrownBy(() -> timerService.start(TEST_USER_EMAIL, request)).isInstanceOf(BusinessRuleException.class)
                                                                          .hasMessage(
                                                                              "Planned study session does not belong to the selected study module");
  }

  @Test
  void getActiveShouldRejectMissingActiveTimer() {
    testDataFactory.createUser();

    flushAndClear();

    assertThatThrownBy(() -> timerService.getActive(TEST_USER_EMAIL)).isInstanceOf(ResourceNotFoundException.class)
                                                                     .hasMessage("Active timer not found");
  }

  @Test
  void stopShouldCreateStudyTimeAndDeleteTimer() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    entityManager.persist(new Timer(
        testData.user(),
        testData.studyModule(),
        testData.learningGoal(),
        null,
        CURRENT_DATE_TIME.minusMinutes(30)));
    flushAndClear();

    timerService.stop(TEST_USER_EMAIL.toUpperCase());

    flushAndClear();

    assertThat(timerRepository.findByUser_EmailIgnoreCase(TEST_USER_EMAIL)).isEmpty();
    assertThat(studyTimeRepository.findAllByUser_EmailIgnoreCaseOrderByStartTimeDesc(TEST_USER_EMAIL)).filteredOn(studyTime -> studyTime.getStartTime()
                                                                                                                                        .equals(
                                                                                                                                            CURRENT_DATE_TIME.minusMinutes(
                                                                                                                                                30)))
                                                                                                      .singleElement()
                                                                                                      .satisfies(studyTime -> assertThat(
                                                                                                          studyTime.getLearningGoal()
                                                                                                                   .getId()).isEqualTo(
                                                                                                          testData.learningGoal().getId()));
  }

  @Test
  void cancelShouldDeleteTimerWithoutCreatingStudyTime() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    int initialStudyTimeCount = studyTimeRepository.findAllByUser_EmailIgnoreCaseOrderByStartTimeDesc(TEST_USER_EMAIL).size();

    entityManager.persist(new Timer(testData.user(), testData.studyModule(), null, null, CURRENT_DATE_TIME));
    flushAndClear();

    timerService.cancel(TEST_USER_EMAIL);

    flushAndClear();

    assertThat(timerRepository.findByUser_EmailIgnoreCase(TEST_USER_EMAIL)).isEmpty();
    assertThat(studyTimeRepository.findAllByUser_EmailIgnoreCaseOrderByStartTimeDesc(TEST_USER_EMAIL)).hasSize(initialStudyTimeCount);
  }

  @Test
  void cancelShouldRejectForeignTimer() {
    User requestedUser = testDataFactory.createUser();
    testDataFactory.createStudyModule(requestedUser);

    User otherUser = testDataFactory.createUser("other-user@learnmanager.local");
    StudyModule otherModule = testDataFactory.createStudyModule(otherUser, "Other module", "OTHER01", BigDecimal.valueOf(100));
    entityManager.persist(new Timer(otherUser, otherModule, null, null, CURRENT_DATE_TIME));

    flushAndClear();

    assertThatThrownBy(() -> timerService.cancel(TEST_USER_EMAIL)).isInstanceOf(ResourceNotFoundException.class)
                                                                  .hasMessage("Active timer not found");
  }

}
