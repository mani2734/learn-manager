package com.learnmanager.service;

import com.learnmanager.dto.response.AdminUserResponse;
import com.learnmanager.dto.response.TestDataGenerationResponse;
import com.learnmanager.entity.*;
import com.learnmanager.entity.enums.Role;
import com.learnmanager.exception.BusinessRuleException;
import com.learnmanager.exception.ResourceNotFoundException;
import com.learnmanager.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

  private static final ZoneId APPLICATION_TIME_ZONE = ZoneId.of("Europe/Vienna");

  private static final String TEST_PASSWORD = "Test2026";

  private static final List<String> TEST_USER_EMAILS = List.of(
      "testuser1@learnmanager.local",
      "testuser2@learnmanager.local",
      "testuser3@learnmanager.local");

  private final UserRepository userRepository;

  private final NotificationSettingsRepository notificationSettingsRepository;

  private final NotificationRepository notificationRepository;

  private final StudyModuleRepository studyModuleRepository;

  private final LearningGoalRepository learningGoalRepository;

  private final MilestoneRepository milestoneRepository;

  private final PlanningPeriodRepository planningPeriodRepository;

  private final ModulePlanRepository modulePlanRepository;

  private final PlannedStudySessionRepository plannedStudySessionRepository;

  private final StudyTimeRepository studyTimeRepository;

  private final TimerRepository timerRepository;

  private final PasswordEncoder passwordEncoder;

  @Transactional(readOnly = true)
  public List<AdminUserResponse> getAllUsers() {
    return userRepository.findAll().stream().map(AdminUserResponse::fromEntity).toList();
  }

  @Transactional(readOnly = true)
  public AdminUserResponse getUserById(Long userId) {
    return AdminUserResponse.fromEntity(userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found")));
  }

  @Transactional
  public TestDataGenerationResponse generateTestData() {
    TEST_USER_EMAILS.forEach(this::deleteTestUser);

    userRepository.flush();

    for (int userIndex = 0; userIndex < TEST_USER_EMAILS.size(); userIndex++) {
      createTestUserWithData(TEST_USER_EMAILS.get(userIndex), userIndex);
    }

    return new TestDataGenerationResponse(TEST_USER_EMAILS.size(), TEST_USER_EMAILS, TEST_PASSWORD);
  }

  @Transactional
  public AdminUserResponse deactivateUser(Long userId) {
    User user = findUserById(userId);

    validateUserCanBeManaged(user);

    user.deactivate();

    return AdminUserResponse.fromEntity(userRepository.save(user));
  }

  @Transactional
  public AdminUserResponse activateUser(Long userId) {
    User user = findUserById(userId);

    validateUserCanBeManaged(user);

    user.activate();

    return AdminUserResponse.fromEntity(userRepository.save(user));
  }

  private void deleteTestUser(String email) {
    userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
      timerRepository.findByUser_EmailIgnoreCase(email).ifPresent(timerRepository::delete);
      studyTimeRepository.findAllByUser_EmailIgnoreCaseOrderByStartTimeDesc(email).forEach(studyTimeRepository::delete);
      notificationRepository.findAllByUser_EmailIgnoreCaseOrderByCreatedAtDesc(email).forEach(notificationRepository::delete);
      milestoneRepository.findAllByLearningGoal_StudyModule_User_EmailIgnoreCase(email).forEach(milestoneRepository::delete);
      modulePlanRepository.findAllByStudyModule_User_EmailIgnoreCaseOrderByCreatedAtDesc(email).forEach(modulePlanRepository::delete);
      learningGoalRepository.findAllByStudyModule_User_EmailIgnoreCaseOrderByCreatedAtDesc(email).forEach(learningGoalRepository::delete);
      plannedStudySessionRepository.findAllByUser_EmailIgnoreCaseOrderByStartTimeAsc(email).forEach(plannedStudySessionRepository::delete);
      studyModuleRepository.findAllByUserEmailIgnoreCaseOrderByCreatedAtDesc(email).forEach(studyModuleRepository::delete);
      planningPeriodRepository.findAllByUser_EmailIgnoreCaseOrderByStartDateDesc(email).forEach(planningPeriodRepository::delete);
      notificationSettingsRepository.findByUser_EmailIgnoreCase(email).ifPresent(notificationSettingsRepository::delete);
      userRepository.delete(user);
    });
  }

  private void createTestUserWithData(String email, int userIndex) {
    User user = userRepository.save(new User(email, passwordEncoder.encode(TEST_PASSWORD), Role.USER));

    notificationSettingsRepository.save(new NotificationSettings(user));

    LocalDate today = LocalDate.now(APPLICATION_TIME_ZONE);
    LocalDateTime now = LocalDateTime.now(APPLICATION_TIME_ZONE).withSecond(0).withNano(0);

    PlanningPeriod planningPeriod = planningPeriodRepository.save(new PlanningPeriod(user, today.minusDays(30)));

    List<StudyModule> studyModules = studyModuleRepository.saveAll(createStudyModules(user));

    for (int moduleIndex = 0; moduleIndex < studyModules.size(); moduleIndex++) {
      StudyModule studyModule = studyModules.get(moduleIndex);

      List<LearningGoal> learningGoals = learningGoalRepository.saveAll(createLearningGoals(studyModule, today, moduleIndex));

      for (LearningGoal learningGoal : learningGoals) {
        milestoneRepository.saveAll(createMilestones(learningGoal, today));
      }

      modulePlanRepository.saveAll(createModulePlans(planningPeriod, studyModule, moduleIndex));

      List<PlannedStudySession> plannedStudySessions = plannedStudySessionRepository.saveAll(createPlannedStudySessions(
          user,
          studyModule,
          now,
          moduleIndex,
          userIndex));

      studyTimeRepository.saveAll(createStudyTimes(user, studyModule, learningGoals, plannedStudySessions));
    }
  }

  private List<StudyModule> createStudyModules(User user) {
    return List.of(
        new StudyModule(user, "Mathematics", "MATH", new BigDecimal("80.00")),
        new StudyModule(user, "Physics", "PHYS", new BigDecimal("70.00")),
        new StudyModule(user, "Computer Science", "CS", new BigDecimal("100.00")),
        new StudyModule(user, "History", "HIST", new BigDecimal("60.00")));
  }

  private List<LearningGoal> createLearningGoals(StudyModule studyModule, LocalDate today, int moduleIndex) {
    return List.of(
        new LearningGoal(studyModule, "Understand the core concepts", new BigDecimal("20.00"), today.plusDays(14L + moduleIndex)),
        new LearningGoal(studyModule, "Complete practical exercises", new BigDecimal("25.00"), today.plusDays(30L + moduleIndex)));
  }

  private List<Milestone> createMilestones(LearningGoal learningGoal, LocalDate today) {
    return List.of(
        new Milestone(learningGoal, "Complete the first learning unit", today.plusDays(7)),
        new Milestone(learningGoal, "Complete the practice unit", today.plusDays(14)));
  }

  private List<ModulePlan> createModulePlans(PlanningPeriod planningPeriod, StudyModule studyModule, int moduleIndex) {
    List<ModulePlan> modulePlans = new ArrayList<>();

    for (int periodNumber = 1; periodNumber <= 6; periodNumber++) {
      modulePlans.add(new ModulePlan(planningPeriod, studyModule, periodNumber, BigDecimal.valueOf(8L + moduleIndex + periodNumber)));
    }

    return modulePlans;
  }

  private List<PlannedStudySession> createPlannedStudySessions(
      User user,
      StudyModule studyModule,
      LocalDateTime now,
      int moduleIndex,
      int userIndex) {
    List<PlannedStudySession> plannedStudySessions = new ArrayList<>();

    for (int sessionIndex = 0; sessionIndex < 3; sessionIndex++) {
      int globalSessionIndex = moduleIndex * 3 + sessionIndex;

      LocalDateTime endTime = now.minusHours(2L + globalSessionIndex * 2L + userIndex);

      LocalDateTime startTime = endTime.minusMinutes(60L + moduleIndex * 15L);

      plannedStudySessions.add(new PlannedStudySession(user, studyModule, "Study session " + (sessionIndex + 1), startTime, endTime));
    }

    LocalDateTime futureStartTime = now.plusDays(2L + moduleIndex).withHour(17 + moduleIndex).withMinute(0);

    plannedStudySessions.add(new PlannedStudySession(
        user,
                                                     studyModule,
                                                     "Upcoming study session",
                                                     futureStartTime,
                                                     futureStartTime.plusMinutes(90)));

    return plannedStudySessions;
  }

  private List<StudyTime> createStudyTimes(
      User user,
      StudyModule studyModule,
      List<LearningGoal> learningGoals,
      List<PlannedStudySession> plannedStudySessions) {
    List<StudyTime> studyTimes = new ArrayList<>();

    for (int sessionIndex = 0; sessionIndex < 3; sessionIndex++) {
      PlannedStudySession plannedStudySession = plannedStudySessions.get(sessionIndex);

      studyTimes.add(new StudyTime(
          user,
          studyModule,
          learningGoals.get(sessionIndex % learningGoals.size()),
          plannedStudySession,
          plannedStudySession.getStartTime(),
          plannedStudySession.getEndTime()));
    }

    return studyTimes;
  }

  private void validateUserCanBeManaged(User user) {
    if (user.getRole() == Role.ADMIN) {
      throw new BusinessRuleException("Admin users cannot be managed through user administration");
    }
  }

  private User findUserById(Long userId) {
    return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
  }

}