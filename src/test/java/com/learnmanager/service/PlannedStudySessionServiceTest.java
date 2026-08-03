package com.learnmanager.service;

import com.learnmanager.dto.request.create.CreatePlannedStudySessionRequest;
import com.learnmanager.dto.request.create.CreatePlannedStudySessionSeriesRequest;
import com.learnmanager.dto.request.update.UpdatePlannedStudySessionRequest;
import com.learnmanager.entity.*;
import com.learnmanager.entity.enums.RecurrenceFrequency;
import com.learnmanager.exception.BusinessRuleException;
import com.learnmanager.exception.ResourceNotFoundException;
import com.learnmanager.repository.PlannedStudySessionRepository;
import com.learnmanager.testsupport.AbstractIntegrationTest;
import com.learnmanager.testsupport.TestDataFactory.CompleteTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.learnmanager.testsupport.TestDataFactory.CURRENT_DATE_TIME;
import static com.learnmanager.testsupport.TestDataFactory.TEST_USER_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlannedStudySessionServiceTest extends AbstractIntegrationTest {

  @Autowired private PlannedStudySessionService plannedStudySessionService;

  @Autowired private PlannedStudySessionRepository plannedStudySessionRepository;

  @Test
  void createShouldCreatePlannedStudySession() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    LocalDateTime startTime = CURRENT_DATE_TIME.plusDays(2);
    LocalDateTime endTime = startTime.plusHours(2);

    CreatePlannedStudySessionRequest request = new CreatePlannedStudySessionRequest(
        testData.studyModule().getId(),
        "  Reading slot  ",
        startTime,
        endTime);

    plannedStudySessionService.create(TEST_USER_EMAIL.toUpperCase(), request);

    flushAndClear();

    assertThat(plannedStudySessionRepository.findAllByUser_EmailIgnoreCaseOrderByStartTimeAsc(TEST_USER_EMAIL)).filteredOn(session -> session.getStartTime()
                                                                                                                                             .equals(
                                                                                                                                                 startTime))
                                                                                                               .singleElement()
                                                                                                               .satisfies(session -> {
                                                                                                                 assertThat(session.getTitle()).isEqualTo(
                                                                                                                     "Reading slot");
                                                                                                                 assertThat(session.getEndTime()).isEqualTo(
                                                                                                                     endTime);
                                                                                                               });
  }

  @Test
  void createShouldRejectInvalidTimeRange() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    CreatePlannedStudySessionRequest request = new CreatePlannedStudySessionRequest(
        testData.studyModule().getId(),
                                                                                    "Invalid slot",
                                                                                    CURRENT_DATE_TIME,
                                                                                    CURRENT_DATE_TIME);

    assertThatThrownBy(() -> plannedStudySessionService.create(TEST_USER_EMAIL, request)).isInstanceOf(BusinessRuleException.class)
                                                                                         .hasMessage("End time must be after start time");
  }

  @Test
  void getAllShouldOnlyReturnPlannedStudySessionsOfRequestedUser() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    User otherUser = testDataFactory.createUser("other-user@learnmanager.local");
    StudyModule otherModule = testDataFactory.createStudyModule(otherUser, "Other module", "OTHER01", BigDecimal.valueOf(100));
    testDataFactory.createPlannedStudySession(otherUser, otherModule);

    flushAndClear();

    assertThat(plannedStudySessionService.getAll(TEST_USER_EMAIL.toUpperCase())).hasSize(1);
    assertThat(plannedStudySessionService.getAllByStudyModule(TEST_USER_EMAIL, testData.studyModule().getId())).hasSize(1);
  }

  @Test
  void getByIdShouldRejectForeignPlannedStudySession() {
    testDataFactory.createUser();

    User otherUser = testDataFactory.createUser("other-user@learnmanager.local");
    StudyModule otherModule = testDataFactory.createStudyModule(otherUser, "Other module", "OTHER01", BigDecimal.valueOf(100));
    PlannedStudySession foreignSession = testDataFactory.createPlannedStudySession(otherUser, otherModule);

    flushAndClear();

    assertThatThrownBy(() -> plannedStudySessionService.getById(TEST_USER_EMAIL, foreignSession.getId())).isInstanceOf(
        ResourceNotFoundException.class);
  }

  @Test
  void updateShouldUpdatePlannedStudySession() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    LocalDateTime startTime = CURRENT_DATE_TIME.plusDays(3);
    LocalDateTime endTime = startTime.plusHours(2);

    UpdatePlannedStudySessionRequest request = new UpdatePlannedStudySessionRequest("  Updated session  ", startTime, endTime);

    plannedStudySessionService.update(TEST_USER_EMAIL, testData.plannedStudySession().getId(), request);

    flushAndClear();

    PlannedStudySession updatedSession = entityManager.find(PlannedStudySession.class, testData.plannedStudySession().getId());

    assertThat(updatedSession.getTitle()).isEqualTo("Updated session");
    assertThat(updatedSession.getStartTime()).isEqualTo(startTime);
    assertThat(updatedSession.getEndTime()).isEqualTo(endTime);
  }

  @Test
  void deleteShouldRejectSessionLinkedToStudyTime() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    StudyTime linkedStudyTime = testDataFactory.createStudyTime(
        testData.user(),
        testData.studyModule(),
        testData.learningGoal(),
        testData.plannedStudySession(),
        CURRENT_DATE_TIME.minusHours(6),
        CURRENT_DATE_TIME.minusHours(5));

    assertThatThrownBy(() -> plannedStudySessionService.delete(TEST_USER_EMAIL, testData.plannedStudySession().getId())).isInstanceOf(
        BusinessRuleException.class).hasMessage("Planned study session cannot be deleted because tracked study time is linked to it");

    assertThat(entityManager.find(StudyTime.class, linkedStudyTime.getId())).isNotNull();
  }

  @Test
  void deleteShouldRejectSessionLinkedToActiveTimer() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    entityManager.persist(new Timer(
        testData.user(),
        testData.studyModule(),
        testData.learningGoal(),
        testData.plannedStudySession(),
        CURRENT_DATE_TIME));
    flushAndClear();

    assertThatThrownBy(() -> plannedStudySessionService.delete(TEST_USER_EMAIL, testData.plannedStudySession().getId())).isInstanceOf(
        BusinessRuleException.class).hasMessage("Planned study session cannot be deleted while an active timer is linked to it");
  }

  @Test
  void createSeriesShouldCreateWeeklyOccurrences() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    LocalDateTime startTime = CURRENT_DATE_TIME.plusDays(5);
    LocalDateTime endTime = startTime.plusHours(1);

    CreatePlannedStudySessionSeriesRequest request = new CreatePlannedStudySessionSeriesRequest(
        testData.studyModule().getId(),
                                                                                                "  Weekly session  ",
                                                                                                startTime,
                                                                                                endTime,
                                                                                                RecurrenceFrequency.WEEKLY,
                                                                                                3);

    plannedStudySessionService.createSeries(TEST_USER_EMAIL, request);

    flushAndClear();

    assertThat(plannedStudySessionRepository.findAllByStudyModule_IdOrderByStartTimeAsc(testData.studyModule()
                                                                                                .getId())).filteredOn(session -> session.getTitle()
                                                                                                                                        .equals(
                                                                                                                                            "Weekly session"))
                                                                                                          .extracting(PlannedStudySession::getStartTime)
                                                                                                          .containsExactly(
                                                                                                              startTime,
                                                                                                              startTime.plusDays(7),
                                                                                                              startTime.plusDays(14));
  }
}
