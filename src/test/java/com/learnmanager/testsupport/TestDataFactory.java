package com.learnmanager.testsupport;

import com.learnmanager.entity.*;
import com.learnmanager.entity.enums.GoalStatus;
import com.learnmanager.entity.enums.NotificationType;
import com.learnmanager.entity.enums.Role;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;

@Component
@RequiredArgsConstructor
@Transactional
public class TestDataFactory {

  public static final ZoneId APPLICATION_TIME_ZONE = ZoneId.of("Europe/Vienna");

  public static final Instant CURRENT_INSTANT = Instant.parse("2026-08-03T12:00:00Z");

  public static final LocalDate CURRENT_DATE = LocalDate.of(2026, 8, 3);

  public static final LocalDateTime CURRENT_DATE_TIME = LocalDateTime.of(2026, 8, 3, 14, 0);

  public static final String TEST_USER_EMAIL = "user@learnmanager.local";

  public static final String TEST_PASSWORD_HASH = "encoded-test-password";

  public static final String TEST_MODULE_NAME = "Software Engineering";

  public static final String TEST_MODULE_CODE = "SWE01";

  public static final String TEST_GOAL_TITLE = "Complete backend implementation";

  public static final String TEST_MILESTONE_TITLE = "Complete notification scheduler";

  public static final String TEST_SESSION_TITLE = "Backend study session";

  private final EntityManager entityManager;

  public static Clock createFixedClock() {
    return Clock.fixed(CURRENT_INSTANT, APPLICATION_TIME_ZONE);
  }

  public User createUser() {
    return createUser(TEST_USER_EMAIL, Role.USER, true);
  }

  public User createUser(String email) {
    return createUser(email, Role.USER, true);
  }

  public User createUser(String email, Role role, boolean active) {
    User user = createEntity(User.class);

    user.setEmail(email);
    user.setPasswordHash(TEST_PASSWORD_HASH);
    user.setRole(role);
    user.setActive(active);

    return persist(user);
  }

  public NotificationSettings createNotificationSettings(User user) {
    NotificationSettings notificationSettings = createEntity(NotificationSettings.class);

    notificationSettings.setUser(user);
    notificationSettings.setPlannedSessionReminderEnabled(true);
    notificationSettings.setPlannedSessionReminderMinutes(30);
    notificationSettings.setInactivityReminderEnabled(true);
    notificationSettings.setInactivityThresholdDays(3);
    notificationSettings.setGoalDeadlineReminderEnabled(true);
    notificationSettings.setGoalDeadlineReminderDays(7);
    notificationSettings.setPlanDeviationReminderEnabled(true);
    notificationSettings.setPlanDeviationThresholdPercent(20);

    return persist(notificationSettings);
  }

  public StudyModule createStudyModule(User user) {
    return createStudyModule(user, TEST_MODULE_NAME, TEST_MODULE_CODE, BigDecimal.valueOf(180));
  }

  public StudyModule createStudyModule(User user, String name, String code, BigDecimal workloadHours) {

    StudyModule studyModule = createEntity(StudyModule.class);

    studyModule.setUser(user);
    studyModule.setName(name);
    studyModule.setCode(code);
    studyModule.setWorkloadHours(workloadHours);

    return persist(studyModule);
  }

  public LearningGoal createLearningGoal(StudyModule studyModule) {
    return createLearningGoal(studyModule, TEST_GOAL_TITLE, BigDecimal.valueOf(60), CURRENT_DATE.plusDays(30), GoalStatus.IN_PROGRESS);
  }

  public LearningGoal createLearningGoal(
      StudyModule studyModule,
      String title,
      BigDecimal workloadHours,
      LocalDate deadline,
      GoalStatus status) {

    LearningGoal learningGoal = createEntity(LearningGoal.class);

    learningGoal.setStudyModule(studyModule);
    learningGoal.setTitle(title);
    learningGoal.setWorkloadHours(workloadHours);
    learningGoal.setDeadline(deadline);
    learningGoal.setStatus(status);

    return persist(learningGoal);
  }

  public Milestone createMilestone(LearningGoal learningGoal) {
    return createMilestone(learningGoal, TEST_MILESTONE_TITLE, CURRENT_DATE.plusDays(14), GoalStatus.IN_PROGRESS);
  }

  public Milestone createMilestone(LearningGoal learningGoal, String title, LocalDate deadline, GoalStatus status) {

    Milestone milestone = createEntity(Milestone.class);

    milestone.setLearningGoal(learningGoal);
    milestone.setTitle(title);
    milestone.setDeadline(deadline);
    milestone.setStatus(status);

    return persist(milestone);
  }

  public PlanningPeriod createPlanningPeriod(User user) {
    LocalDate startDate = CURRENT_DATE.minusDays(30);

    return createPlanningPeriod(user, startDate, startDate.plusDays(179));
  }

  public PlanningPeriod createPlanningPeriod(User user, LocalDate startDate, LocalDate endDate) {

    PlanningPeriod planningPeriod = new PlanningPeriod(user, startDate);

    if (!planningPeriod.getEndDate().equals(endDate)) {
      throw new IllegalArgumentException("endDate must match startDate plus 179 days");
    }

    return persist(planningPeriod);
  }

  public ModulePlan createModulePlan(PlanningPeriod planningPeriod, StudyModule studyModule) {

    return createModulePlan(planningPeriod, studyModule, 1, BigDecimal.valueOf(30));
  }

  public ModulePlan createModulePlan(
      PlanningPeriod planningPeriod,
      StudyModule studyModule,
      Integer periodNumber,
      BigDecimal plannedHours) {

    ModulePlan modulePlan = createEntity(ModulePlan.class);

    modulePlan.setPlanningPeriod(planningPeriod);
    modulePlan.setStudyModule(studyModule);
    modulePlan.setPeriodNumber(periodNumber);
    modulePlan.setPlannedHours(plannedHours);

    return persist(modulePlan);
  }

  public PlannedStudySession createPlannedStudySession(User user, StudyModule studyModule) {

    return createPlannedStudySession(
        user,
        studyModule,
        TEST_SESSION_TITLE,
        CURRENT_DATE_TIME.plusMinutes(20),
        CURRENT_DATE_TIME.plusMinutes(80));
  }

  public PlannedStudySession createPlannedStudySession(
      User user,
      StudyModule studyModule,
      String title,
      LocalDateTime startTime,
      LocalDateTime endTime) {

    PlannedStudySession plannedStudySession = createEntity(PlannedStudySession.class);

    plannedStudySession.setUser(user);
    plannedStudySession.setStudyModule(studyModule);
    plannedStudySession.setTitle(title);
    plannedStudySession.setStartTime(startTime);
    plannedStudySession.setEndTime(endTime);

    return persist(plannedStudySession);
  }

  public StudyTime createStudyTime(User user, StudyModule studyModule, LearningGoal learningGoal, PlannedStudySession plannedStudySession) {

    return createStudyTime(
        user,
        studyModule,
        learningGoal,
        plannedStudySession,
        CURRENT_DATE_TIME.minusHours(2),
        CURRENT_DATE_TIME.minusHours(1));
  }

  public StudyTime createStudyTime(
      User user,
      StudyModule studyModule,
      LearningGoal learningGoal,
      PlannedStudySession plannedStudySession,
      LocalDateTime startTime,
      LocalDateTime endTime) {

    StudyTime studyTime = createEntity(StudyTime.class);

    studyTime.setUser(user);
    studyTime.setStudyModule(studyModule);
    studyTime.setLearningGoal(learningGoal);
    studyTime.setPlannedStudySession(plannedStudySession);
    studyTime.setStartTime(startTime);
    studyTime.setEndTime(endTime);

    return persist(studyTime);
  }

  public Notification createNotification(User user) {
    return createNotification(
        user,
        NotificationType.PLANNED_SESSION_REMINDER,
        "Test notification",
        "This is a test notification.",
        "TEST_NOTIFICATION:" + user.getId(),
        false);
  }

  public Notification createNotification(
      User user,
      NotificationType type,
      String title,
      String message,
      String referenceKey,
      boolean readStatus) {

    Notification notification = createEntity(Notification.class);

    notification.setUser(user);
    notification.setType(type);
    notification.setTitle(title);
    notification.setMessage(message);
    notification.setReferenceKey(referenceKey);
    notification.setNotificationRead(readStatus);

    return persist(notification);
  }

  public CompleteTestData createCompleteTestData() {
    User user = createUser();

    NotificationSettings notificationSettings = createNotificationSettings(user);

    StudyModule studyModule = createStudyModule(user);

    LearningGoal learningGoal = createLearningGoal(studyModule);

    Milestone milestone = createMilestone(learningGoal);

    PlanningPeriod planningPeriod = createPlanningPeriod(user);

    ModulePlan modulePlan = createModulePlan(planningPeriod, studyModule);

    PlannedStudySession plannedStudySession = createPlannedStudySession(user, studyModule);

    /*
     * The planned session starts in the future and is required by the
     * planned-session reminder tests. The already completed StudyTime
     * therefore must not reference this future session.
     */
    StudyTime studyTime = createStudyTime(user, studyModule, learningGoal, null);

    return new CompleteTestData(
        user,
                                notificationSettings,
                                studyModule,
                                learningGoal,
                                milestone,
                                planningPeriod,
                                modulePlan,
                                plannedStudySession,
                                studyTime);
  }

  public void flushAndClear() {
    entityManager.flush();
    entityManager.clear();
  }

  private <T> T createEntity(Class<T> entityType) {
    return BeanUtils.instantiateClass(entityType);
  }

  private <T> T persist(T entity) {
    entityManager.persist(entity);
    entityManager.flush();

    return entity;
  }

  public record CompleteTestData(User user, NotificationSettings notificationSettings, StudyModule studyModule, LearningGoal learningGoal,
                                 Milestone milestone, PlanningPeriod planningPeriod, ModulePlan modulePlan,
                                 PlannedStudySession plannedStudySession, StudyTime studyTime) {

  }
}