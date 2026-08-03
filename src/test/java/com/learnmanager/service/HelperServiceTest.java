package com.learnmanager.service;

import com.learnmanager.entity.LearningGoal;
import com.learnmanager.entity.PlannedStudySession;
import com.learnmanager.entity.StudyModule;
import com.learnmanager.entity.User;
import com.learnmanager.entity.enums.GoalStatus;
import com.learnmanager.exception.BusinessRuleException;
import com.learnmanager.exception.ResourceNotFoundException;
import com.learnmanager.testsupport.AbstractIntegrationTest;
import com.learnmanager.testsupport.TestDataFactory.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HelperServiceTest extends AbstractIntegrationTest {

  @Autowired private HelperService helperService;

  @Test
  void normalizeOptionalTextShouldTrimAndConvertBlankToNull() {
    assertThat(helperService.normalizeOptionalText("  SWE01  ")).isEqualTo("SWE01");
    assertThat(helperService.normalizeOptionalText("   ")).isNull();
    assertThat(helperService.normalizeOptionalText(null)).isNull();
  }

  @Test
  void normalizeEmailShouldTrimAndLowercase() {
    assertThat(helperService.normalizeEmail("  USER@LearnManager.Local  ")).isEqualTo("user@learnmanager.local");
  }

  @Test
  void findOwnedMethodsShouldRejectForeignResources() {
    testDataFactory.createUser();

    User otherUser = testDataFactory.createUser("other-user@learnmanager.local");
    StudyModule otherModule = testDataFactory.createStudyModule(otherUser, "Other module", "OTHER01", BigDecimal.valueOf(100));
    LearningGoal otherGoal = testDataFactory.createLearningGoal(otherModule);
    PlannedStudySession otherSession = testDataFactory.createPlannedStudySession(otherUser, otherModule);

    flushAndClear();

    assertThatThrownBy(() -> helperService.findOwnedStudyModule(TEST_USER_EMAIL, otherModule.getId())).isInstanceOf(
        ResourceNotFoundException.class);
    assertThatThrownBy(() -> helperService.findOwnedLearningGoal(
        TEST_USER_EMAIL,
        otherGoal.getId())).isInstanceOf(ResourceNotFoundException.class);
    assertThatThrownBy(() -> helperService.findOwnedPlannedStudySession(TEST_USER_EMAIL, otherSession.getId())).isInstanceOf(
        ResourceNotFoundException.class);
  }

  @Test
  void calculateLearningGoalProgressShouldUseTrackedTimeAndCapAtOneHundredPercent() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    testDataFactory.createStudyTime(
        testData.user(),
        testData.studyModule(),
        testData.learningGoal(),
        null,
        CURRENT_DATE_TIME.minusHours(5),
        CURRENT_DATE_TIME.minusHours(4));

    flushAndClear();

    LearningGoal learningGoal = helperService.findOwnedLearningGoal(TEST_USER_EMAIL, testData.learningGoal().getId());

    assertThat(helperService.calculateLearningGoalProgress(learningGoal)).isEqualByComparingTo("3.33");

    learningGoal.setStatus(GoalStatus.COMPLETED);

    assertThat(helperService.calculateLearningGoalProgress(learningGoal)).isEqualByComparingTo("100.00");
  }

  @Test
  void validateWorkloadAgainstLearningGoalsShouldRejectExcessWorkload() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    assertThatThrownBy(() -> helperService.validateWorkloadAgainstLearningGoals(
        testData.studyModule(),
        BigDecimal.valueOf(121),
        true)).isInstanceOf(BusinessRuleException.class);
  }

  @Test
  void findUserByEmailShouldRejectUnknownUser() {
    assertThatThrownBy(() -> helperService.findUserByEmail("missing@learnmanager.local")).isInstanceOf(UsernameNotFoundException.class)
                                                                                         .hasMessage("Authenticated user no longer exists");
  }

  @Test
  void validateNoStudyTimeOverlapShouldRejectOverlapAndAllowBoundaryTouching() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    assertThatThrownBy(() -> helperService.validateNoStudyTimeOverlap(
        TEST_USER_EMAIL,
        testData.studyTime().getStartTime().plusMinutes(15),
        testData.studyTime().getEndTime().plusMinutes(15))).isInstanceOf(
                                                               BusinessRuleException.class)
                                                           .hasMessage(
                                                               "Study time overlaps an existing study time");

    helperService.validateNoStudyTimeOverlap(
        TEST_USER_EMAIL,
        testData.studyTime().getEndTime(),
        testData.studyTime().getEndTime().plusHours(1));
  }

  @Test
  void calculateLearningGoalProgressShouldReturnZeroWithoutStudyTime() {
    User user = testDataFactory.createUser();
    StudyModule module = testDataFactory.createStudyModule(user);
    LearningGoal goal = testDataFactory.createLearningGoal(
        module,
        "No progress",
        BigDecimal.valueOf(10),
        CURRENT_DATE.plusDays(5),
        GoalStatus.PLANNED);

    flushAndClear();

    assertThat(helperService.calculateLearningGoalProgress(goal)).isEqualByComparingTo("0.00");
  }
}
