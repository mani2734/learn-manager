package com.learnmanager.service;

import com.learnmanager.dto.request.create.CreateMilestoneRequest;
import com.learnmanager.dto.request.update.UpdateMilestoneRequest;
import com.learnmanager.entity.Milestone;
import com.learnmanager.entity.StudyModule;
import com.learnmanager.entity.User;
import com.learnmanager.entity.enums.GoalStatus;
import com.learnmanager.exception.ResourceNotFoundException;
import com.learnmanager.repository.MilestoneRepository;
import com.learnmanager.testsupport.AbstractIntegrationTest;
import com.learnmanager.testsupport.TestDataFactory.CompleteTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.learnmanager.testsupport.TestDataFactory.TEST_USER_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MilestoneServiceTest extends AbstractIntegrationTest {

  @Autowired private MilestoneService milestoneService;

  @Autowired private MilestoneRepository milestoneRepository;

  @Test
  void createShouldCreateMilestone() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    CreateMilestoneRequest request = new CreateMilestoneRequest(
        testData.learningGoal().getId(),
        "  Draft API tests  ",
        LocalDate.of(2026, 9, 1));

    milestoneService.create(TEST_USER_EMAIL.toUpperCase(), request);

    flushAndClear();

    assertThat(milestoneRepository.findAllByLearningGoal_IdOrderByCreatedAtDesc(testData.learningGoal()
                                                                                        .getId())).filteredOn(milestone -> milestone.getTitle()
                                                                                                                                    .equals(
                                                                                                                                        "Draft API tests"))
                                                                                                  .singleElement()
                                                                                                  .satisfies(milestone -> {
                                                                                                    assertThat(milestone.getDeadline()).isEqualTo(
                                                                                                        LocalDate.of(2026, 9, 1));
                                                                                                    assertThat(milestone.getStatus()).isEqualTo(
                                                                                                        GoalStatus.PLANNED);
                                                                                                  });
  }

  @Test
  void getAllShouldOnlyReturnMilestonesOfRequestedUser() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    User otherUser = testDataFactory.createUser("other-user@learnmanager.local");
    StudyModule otherModule = testDataFactory.createStudyModule(otherUser, "Other module", "OTHER01", BigDecimal.valueOf(100));
    testDataFactory.createMilestone(testDataFactory.createLearningGoal(otherModule));

    flushAndClear();

    assertThat(milestoneService.getAll(TEST_USER_EMAIL.toUpperCase())).hasSize(1);
    assertThat(milestoneService.getAllByLearningGoal(TEST_USER_EMAIL, testData.learningGoal().getId())).hasSize(1);
  }

  @Test
  void getByIdShouldRejectForeignMilestone() {
    testDataFactory.createUser();

    User otherUser = testDataFactory.createUser("other-user@learnmanager.local");
    StudyModule otherModule = testDataFactory.createStudyModule(otherUser, "Other module", "OTHER01", BigDecimal.valueOf(100));
    Milestone foreignMilestone = testDataFactory.createMilestone(testDataFactory.createLearningGoal(otherModule));

    flushAndClear();

    assertThatThrownBy(() -> milestoneService.getById(
        TEST_USER_EMAIL,
        foreignMilestone.getId())).isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void updateShouldUpdateMilestone() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    UpdateMilestoneRequest request = new UpdateMilestoneRequest("  Updated milestone  ", LocalDate.of(2026, 9, 10), GoalStatus.COMPLETED);

    milestoneService.update(TEST_USER_EMAIL, testData.milestone().getId(), request);

    flushAndClear();

    Milestone updatedMilestone = entityManager.find(Milestone.class, testData.milestone().getId());

    assertThat(updatedMilestone.getTitle()).isEqualTo("Updated milestone");
    assertThat(updatedMilestone.getDeadline()).isEqualTo(LocalDate.of(2026, 9, 10));
    assertThat(updatedMilestone.getStatus()).isEqualTo(GoalStatus.COMPLETED);
  }

  @Test
  void deleteShouldDeleteOwnedMilestone() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    Long milestoneId = testData.milestone().getId();

    milestoneService.delete(TEST_USER_EMAIL, milestoneId);

    flushAndClear();

    assertThat(entityManager.find(Milestone.class, milestoneId)).isNull();
  }

}
