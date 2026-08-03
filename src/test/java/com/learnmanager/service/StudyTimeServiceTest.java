package com.learnmanager.service;

import com.learnmanager.dto.request.create.CreateStudyTimeRequest;
import com.learnmanager.dto.request.update.UpdateStudyTimeRequest;
import com.learnmanager.entity.*;
import com.learnmanager.entity.enums.GoalStatus;
import com.learnmanager.exception.BusinessRuleException;
import com.learnmanager.exception.ResourceNotFoundException;
import com.learnmanager.repository.StudyTimeRepository;
import com.learnmanager.testsupport.AbstractIntegrationTest;
import com.learnmanager.testsupport.TestDataFactory.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StudyTimeServiceTest extends AbstractIntegrationTest {

  @Autowired private StudyTimeService studyTimeService;

  @Autowired private StudyTimeRepository studyTimeRepository;

  @Test
  void createShouldCreateStudyTime() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    LocalDateTime startTime = CURRENT_DATE_TIME.minusHours(5);

    LocalDateTime endTime = CURRENT_DATE_TIME.minusHours(4);

    CreateStudyTimeRequest request = createRequest(
        testData.studyModule().getId(),
        testData.learningGoal().getId(),
        testData.plannedStudySession().getId(),
        startTime,
        endTime);

    studyTimeService.create(TEST_USER_EMAIL.toUpperCase(), request);

    flushAndClear();

    List<StudyTime> studyTimes = studyTimeRepository.findAllByUser_EmailIgnoreCaseOrderByStartTimeDesc(TEST_USER_EMAIL);

    assertThat(studyTimes).filteredOn(studyTime -> studyTime.getStartTime().equals(startTime)).singleElement().satisfies(studyTime -> {
      assertThat(studyTime.getUser().getId()).isEqualTo(testData.user().getId());

      assertThat(studyTime.getStudyModule().getId()).isEqualTo(testData.studyModule().getId());

      assertThat(studyTime.getLearningGoal().getId()).isEqualTo(testData.learningGoal().getId());

      assertThat(studyTime.getPlannedStudySession().getId()).isEqualTo(testData.plannedStudySession().getId());

      assertThat(studyTime.getEndTime()).isEqualTo(endTime);
    });
  }

  @Test
  void createShouldAllowNullOptionalReferences() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    LocalDateTime startTime = CURRENT_DATE_TIME.minusHours(5);

    LocalDateTime endTime = CURRENT_DATE_TIME.minusHours(4);

    CreateStudyTimeRequest request = createRequest(testData.studyModule().getId(), null, null, startTime, endTime);

    studyTimeService.create(TEST_USER_EMAIL, request);

    flushAndClear();

    StudyTime studyTime = studyTimeRepository.findAllByUser_EmailIgnoreCaseOrderByStartTimeDesc(TEST_USER_EMAIL)
                                             .stream()
                                             .filter(savedStudyTime -> savedStudyTime.getStartTime().equals(startTime))
                                             .findFirst()
                                             .orElseThrow();

    assertThat(studyTime.getLearningGoal()).isNull();

    assertThat(studyTime.getPlannedStudySession()).isNull();
  }

  @Test
  void createShouldRejectEndTimeBeforeStartTime() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    CreateStudyTimeRequest request = createRequest(
        testData.studyModule().getId(),
        null,
        null,
        CURRENT_DATE_TIME,
        CURRENT_DATE_TIME.minusMinutes(1));

    assertThatThrownBy(() -> studyTimeService.create(TEST_USER_EMAIL, request)).isInstanceOf(BusinessRuleException.class)
                                                                               .hasMessage("End time must be after start time");
  }

  @Test
  void createShouldRejectEqualStartAndEndTime() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    CreateStudyTimeRequest request = createRequest(testData.studyModule().getId(), null, null, CURRENT_DATE_TIME, CURRENT_DATE_TIME);

    assertThatThrownBy(() -> studyTimeService.create(TEST_USER_EMAIL, request)).isInstanceOf(BusinessRuleException.class)
                                                                               .hasMessage("End time must be after start time");
  }

  @Test
  void createShouldRejectOverlappingStudyTime() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    CreateStudyTimeRequest request = createRequest(
        testData.studyModule().getId(),
        null,
        null,
        testData.studyTime().getStartTime().plusMinutes(15),
        testData.studyTime().getEndTime().plusMinutes(15));

    assertThatThrownBy(() -> studyTimeService.create(TEST_USER_EMAIL, request)).isInstanceOf(BusinessRuleException.class)
                                                                               .hasMessage("Study time overlaps an existing study time");
  }

  @Test
  void createShouldAllowTimeRangeTouchingExistingBoundary() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    LocalDateTime startTime = testData.studyTime().getEndTime();

    LocalDateTime endTime = startTime.plusHours(1);

    CreateStudyTimeRequest request = createRequest(testData.studyModule().getId(), null, null, startTime, endTime);

    studyTimeService.create(TEST_USER_EMAIL, request);

    flushAndClear();

    assertThat(studyTimeRepository.findAllByUser_EmailIgnoreCaseOrderByStartTimeDesc(TEST_USER_EMAIL)).filteredOn(studyTime -> studyTime.getStartTime()
                                                                                                                                        .equals(
                                                                                                                                            startTime))
                                                                                                      .hasSize(1);
  }

  @Test
  void createShouldRejectLearningGoalFromDifferentModule() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    StudyModule otherModule = testDataFactory.createStudyModule(testData.user(), "Databases", "DB01", BigDecimal.valueOf(100));

    LearningGoal otherLearningGoal = testDataFactory.createLearningGoal(
        otherModule,
        "Complete database exercises",
        BigDecimal.valueOf(30),
        CURRENT_DATE.plusDays(20),
        GoalStatus.IN_PROGRESS);

    CreateStudyTimeRequest request = createRequest(
        testData.studyModule().getId(),
        otherLearningGoal.getId(),
        null,
        CURRENT_DATE_TIME.minusHours(5),
        CURRENT_DATE_TIME.minusHours(4));

    assertThatThrownBy(() -> studyTimeService.create(TEST_USER_EMAIL, request)).isInstanceOf(BusinessRuleException.class)
                                                                               .hasMessage(
                                                                                   "Learning goal does not belong to the selected study module");
  }

  @Test
  void createShouldRejectPlannedSessionFromDifferentModule() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    StudyModule otherModule = testDataFactory.createStudyModule(testData.user(), "Databases", "DB01", BigDecimal.valueOf(100));

    PlannedStudySession otherSession = testDataFactory.createPlannedStudySession(
        testData.user(),
        otherModule,
        "Database study session",
        CURRENT_DATE_TIME.plusDays(1),
        CURRENT_DATE_TIME.plusDays(1).plusHours(1));

    CreateStudyTimeRequest request = createRequest(
        testData.studyModule().getId(),
        null,
        otherSession.getId(),
        CURRENT_DATE_TIME.minusHours(5),
        CURRENT_DATE_TIME.minusHours(4));

    assertThatThrownBy(() -> studyTimeService.create(TEST_USER_EMAIL, request)).isInstanceOf(BusinessRuleException.class)
                                                                               .hasMessage(
                                                                                   "Planned study session does not belong to the selected study module");
  }

  @Test
  void getAllShouldOnlyReturnStudyTimesOfRequestedUser() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    User otherUser = testDataFactory.createUser("other-user@learnmanager.local");

    StudyModule otherModule = testDataFactory.createStudyModule(otherUser, "Other module", "OTHER01", BigDecimal.valueOf(100));

    testDataFactory.createStudyTime(otherUser, otherModule, null, null, CURRENT_DATE_TIME.minusHours(6), CURRENT_DATE_TIME.minusHours(5));

    flushAndClear();

    assertThat(studyTimeService.getAll(TEST_USER_EMAIL.toUpperCase())).hasSize(1);
  }

  @Test
  void getAllByStudyModuleShouldOnlyReturnMatchingStudyTimes() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    StudyModule otherModule = testDataFactory.createStudyModule(testData.user(), "Databases", "DB01", BigDecimal.valueOf(100));

    testDataFactory.createStudyTime(
        testData.user(),
        otherModule,
        null,
        null,
        CURRENT_DATE_TIME.minusHours(6),
        CURRENT_DATE_TIME.minusHours(5));

    flushAndClear();

    assertThat(studyTimeService.getAllByStudyModule(TEST_USER_EMAIL, testData.studyModule().getId())).hasSize(1);
  }

  @Test
  void getAllByLearningGoalShouldOnlyReturnMatchingStudyTimes() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    LearningGoal secondLearningGoal = testDataFactory.createLearningGoal(
        testData.studyModule(),
        "Second learning goal",
        BigDecimal.valueOf(30),
        CURRENT_DATE.plusDays(40),
        GoalStatus.PLANNED);

    testDataFactory.createStudyTime(
        testData.user(),
        testData.studyModule(),
        secondLearningGoal,
        null,
        CURRENT_DATE_TIME.minusHours(6),
        CURRENT_DATE_TIME.minusHours(5));

    flushAndClear();

    assertThat(studyTimeService.getAllByLearningGoal(TEST_USER_EMAIL, testData.learningGoal().getId())).hasSize(1);
  }

  @Test
  void getByIdShouldReturnOwnedStudyTime() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    flushAndClear();

    assertThat(studyTimeService.getById(TEST_USER_EMAIL.toUpperCase(), testData.studyTime().getId())).isNotNull();
  }

  @Test
  void getByIdShouldRejectForeignStudyTime() {
    testDataFactory.createUser();

    User otherUser = testDataFactory.createUser("other-user@learnmanager.local");

    StudyModule otherModule = testDataFactory.createStudyModule(otherUser, "Other module", "OTHER01", BigDecimal.valueOf(100));

    StudyTime foreignStudyTime = testDataFactory.createStudyTime(
        otherUser,
        otherModule,
        null,
        null,
        CURRENT_DATE_TIME.minusHours(6),
        CURRENT_DATE_TIME.minusHours(5));

    flushAndClear();

    assertThatThrownBy(() -> studyTimeService.getById(
        TEST_USER_EMAIL,
        foreignStudyTime.getId())).isInstanceOf(ResourceNotFoundException.class)
                                  .hasMessage("Study time not found");
  }

  @Test
  void updateShouldUpdateStudyTime() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    Long studyTimeId = testData.studyTime().getId();

    LocalDateTime updatedStartTime = CURRENT_DATE_TIME.minusHours(6);

    LocalDateTime updatedEndTime = CURRENT_DATE_TIME.minusHours(5);

    UpdateStudyTimeRequest request = updateRequest(null, null, updatedStartTime, updatedEndTime);

    studyTimeService.update(TEST_USER_EMAIL, studyTimeId, request);

    flushAndClear();

    StudyTime updatedStudyTime = entityManager.find(StudyTime.class, studyTimeId);

    assertThat(updatedStudyTime.getStartTime()).isEqualTo(updatedStartTime);

    assertThat(updatedStudyTime.getEndTime()).isEqualTo(updatedEndTime);

    assertThat(updatedStudyTime.getLearningGoal()).isNull();

    assertThat(updatedStudyTime.getPlannedStudySession()).isNull();
  }

  @Test
  void updateShouldAllowUnchangedTimeRange() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    Long studyTimeId = testData.studyTime().getId();

    UpdateStudyTimeRequest request = updateRequest(
        testData.learningGoal().getId(),
        testData.studyTime().getPlannedStudySession() == null ? null : testData.studyTime().getPlannedStudySession().getId(),
        testData.studyTime().getStartTime(),
        testData.studyTime().getEndTime());

    studyTimeService.update(TEST_USER_EMAIL, studyTimeId, request);

    flushAndClear();

    assertThat(entityManager.find(StudyTime.class, studyTimeId)).isNotNull();
  }

  @Test
  void updateShouldRejectOverlapWithOtherStudyTime() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    StudyTime otherStudyTime = testDataFactory.createStudyTime(
        testData.user(),
        testData.studyModule(),
        null,
        null,
        CURRENT_DATE_TIME.minusHours(6),
        CURRENT_DATE_TIME.minusHours(5));

    UpdateStudyTimeRequest request = updateRequest(
        null,
        null,
        otherStudyTime.getStartTime().plusMinutes(15),
        otherStudyTime.getEndTime().plusMinutes(15));

    assertThatThrownBy(() -> studyTimeService.update(TEST_USER_EMAIL, testData.studyTime().getId(), request)).isInstanceOf(
        BusinessRuleException.class).hasMessage("Study time overlaps an existing study time");
  }

  @Test
  void updateShouldRejectInvalidTimeRange() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    UpdateStudyTimeRequest request = updateRequest(null, null, CURRENT_DATE_TIME, CURRENT_DATE_TIME);

    assertThatThrownBy(() -> studyTimeService.update(TEST_USER_EMAIL, testData.studyTime().getId(), request)).isInstanceOf(
        BusinessRuleException.class).hasMessage("End time must be after start time");
  }

  @Test
  void deleteShouldDeleteOwnedStudyTime() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    Long studyTimeId = testData.studyTime().getId();

    studyTimeService.delete(TEST_USER_EMAIL, studyTimeId);

    flushAndClear();

    assertThat(entityManager.find(StudyTime.class, studyTimeId)).isNull();
  }

  @Test
  void deleteShouldRejectForeignStudyTime() {
    testDataFactory.createUser();

    User otherUser = testDataFactory.createUser("other-user@learnmanager.local");

    StudyModule otherModule = testDataFactory.createStudyModule(otherUser, "Other module", "OTHER01", BigDecimal.valueOf(100));

    StudyTime foreignStudyTime = testDataFactory.createStudyTime(
        otherUser,
        otherModule,
        null,
        null,
        CURRENT_DATE_TIME.minusHours(6),
        CURRENT_DATE_TIME.minusHours(5));

    Long foreignStudyTimeId = foreignStudyTime.getId();

    flushAndClear();

    assertThatThrownBy(() -> studyTimeService.delete(TEST_USER_EMAIL, foreignStudyTimeId)).isInstanceOf(ResourceNotFoundException.class)
                                                                                          .hasMessage("Study time not found");

    assertThat(entityManager.find(StudyTime.class, foreignStudyTimeId)).isNotNull();
  }

  private CreateStudyTimeRequest createRequest(
      Long studyModuleId,
      Long learningGoalId,
      Long plannedStudySessionId,
      LocalDateTime startTime,
      LocalDateTime endTime) {

    Map<String, Object> values = new HashMap<>();

    values.put("studyModuleId", studyModuleId);

    values.put("learningGoalId", learningGoalId);

    values.put("plannedStudySessionId", plannedStudySessionId);

    values.put("startTime", startTime);

    values.put("endTime", endTime);

    return instantiateRecord(CreateStudyTimeRequest.class, values);
  }

  private UpdateStudyTimeRequest updateRequest(
      Long learningGoalId,
      Long plannedStudySessionId,
      LocalDateTime startTime,
      LocalDateTime endTime) {

    Map<String, Object> values = new HashMap<>();

    values.put("learningGoalId", learningGoalId);

    values.put("plannedStudySessionId", plannedStudySessionId);

    values.put("startTime", startTime);

    values.put("endTime", endTime);

    return instantiateRecord(UpdateStudyTimeRequest.class, values);
  }

  private <T> T instantiateRecord(Class<T> recordType, Map<String, Object> values) {

    try {
      RecordComponent[] components = recordType.getRecordComponents();

      Class<?>[] parameterTypes = Arrays.stream(components).map(RecordComponent::getType).toArray(Class<?>[]::new);

      Object[] arguments = Arrays.stream(components).map(component -> values.get(component.getName())).toArray();

      return recordType.getDeclaredConstructor(parameterTypes).newInstance(arguments);
    } catch (ReflectiveOperationException exception) {
      throw new IllegalStateException("Could not create request record " + recordType.getSimpleName(), exception);
    }
  }
}