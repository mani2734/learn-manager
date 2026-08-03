package com.learnmanager.service;

import com.learnmanager.dto.request.auth.ResetUserPasswordRequest;
import com.learnmanager.entity.User;
import com.learnmanager.entity.enums.Role;
import com.learnmanager.exception.BusinessRuleException;
import com.learnmanager.exception.ResourceNotFoundException;
import com.learnmanager.repository.*;
import com.learnmanager.testsupport.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdminServiceTest extends AbstractIntegrationTest {

  private static final String MANAGED_USER_EMAIL = "managed-user@learnmanager.local";

  private static final String ADMIN_USER_EMAIL = "second-admin@learnmanager.local";

  private static final String NEW_PASSWORD = "NewPassword2026";

  private static final String TEST_PASSWORD = "Test2026";

  private static final List<String> GENERATED_USER_EMAILS = List.of(
      "testuser1@learnmanager.local",
      "testuser2@learnmanager.local",
      "testuser3@learnmanager.local");

  @Autowired private AdminService adminService;

  @Autowired private UserRepository userRepository;

  @Autowired private NotificationSettingsRepository notificationSettingsRepository;

  @Autowired private NotificationRepository notificationRepository;

  @Autowired private StudyModuleRepository studyModuleRepository;

  @Autowired private LearningGoalRepository learningGoalRepository;

  @Autowired private MilestoneRepository milestoneRepository;

  @Autowired private PlanningPeriodRepository planningPeriodRepository;

  @Autowired private ModulePlanRepository modulePlanRepository;

  @Autowired private PlannedStudySessionRepository plannedStudySessionRepository;

  @Autowired private StudyTimeRepository studyTimeRepository;

  @Autowired private TimerRepository timerRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Test
  void getAllUsersShouldReturnAllUsers() {
    int initialUserCount = adminService.getAllUsers().size();

    testDataFactory.createUser(MANAGED_USER_EMAIL);

    flushAndClear();

    assertThat(adminService.getAllUsers()).hasSize(initialUserCount + 1);
  }

  @Test
  void getUserByIdShouldReturnExistingUser() {
    User user = testDataFactory.createUser(MANAGED_USER_EMAIL);

    flushAndClear();

    assertThat(adminService.getUserById(user.getId())).isNotNull();
  }

  @Test
  void getUserByIdShouldRejectUnknownUser() {
    assertThatThrownBy(() -> adminService.getUserById(Long.MAX_VALUE)).isInstanceOf(ResourceNotFoundException.class)
                                                                      .hasMessage("User not found");
  }

  @Test
  void deactivateUserShouldDeactivateUser() {
    User user = testDataFactory.createUser(MANAGED_USER_EMAIL);

    Long userId = user.getId();

    flushAndClear();

    adminService.deactivateUser(userId);

    flushAndClear();

    assertThat(findActiveStatus(userId)).isFalse();
  }

  @Test
  void activateUserShouldActivateUser() {
    User user = testDataFactory.createUser(MANAGED_USER_EMAIL, Role.USER, false);

    Long userId = user.getId();

    flushAndClear();

    adminService.activateUser(userId);

    flushAndClear();

    assertThat(findActiveStatus(userId)).isTrue();
  }

  @Test
  void deactivateUserShouldRejectAdminUser() {
    User admin = testDataFactory.createUser(ADMIN_USER_EMAIL, Role.ADMIN, true);

    Long adminId = admin.getId();

    flushAndClear();

    assertThatThrownBy(() -> adminService.deactivateUser(adminId)).isInstanceOf(BusinessRuleException.class)
                                                                  .hasMessage("Admin users cannot be managed through user administration");

    assertThat(findActiveStatus(adminId)).isTrue();
  }

  @Test
  void activateUserShouldRejectAdminUser() {
    User admin = testDataFactory.createUser(ADMIN_USER_EMAIL, Role.ADMIN, false);

    Long adminId = admin.getId();

    flushAndClear();

    assertThatThrownBy(() -> adminService.activateUser(adminId)).isInstanceOf(BusinessRuleException.class)
                                                                .hasMessage("Admin users cannot be managed through user administration");

    assertThat(findActiveStatus(adminId)).isFalse();
  }

  @Test
  void resetUserPasswordShouldStoreEncodedPassword() {
    User user = testDataFactory.createUser(MANAGED_USER_EMAIL);

    Long userId = user.getId();

    ResetUserPasswordRequest request = new ResetUserPasswordRequest(NEW_PASSWORD);

    flushAndClear();

    adminService.resetUserPassword(userId, request);

    flushAndClear();

    String storedPasswordHash = findPasswordHash(userId);

    assertThat(storedPasswordHash).isNotEqualTo(NEW_PASSWORD);

    assertThat(passwordEncoder.matches(NEW_PASSWORD, storedPasswordHash)).isTrue();
  }

  @Test
  void resetUserPasswordShouldRejectAdminUser() {
    User admin = testDataFactory.createUser(ADMIN_USER_EMAIL, Role.ADMIN, true);

    Long adminId = admin.getId();
    String originalPasswordHash = findPasswordHash(adminId);

    ResetUserPasswordRequest request = new ResetUserPasswordRequest(NEW_PASSWORD);

    flushAndClear();

    assertThatThrownBy(() -> adminService.resetUserPassword(adminId, request)).isInstanceOf(BusinessRuleException.class)
                                                                              .hasMessage(
                                                                                  "Admin users cannot be managed through user administration");

    assertThat(findPasswordHash(adminId)).isEqualTo(originalPasswordHash);
  }

  @Test
  void userManagementShouldRejectUnknownUser() {
    ResetUserPasswordRequest request = new ResetUserPasswordRequest(NEW_PASSWORD);

    assertThatThrownBy(() -> adminService.deactivateUser(Long.MAX_VALUE)).isInstanceOf(ResourceNotFoundException.class)
                                                                         .hasMessage("User not found");

    assertThatThrownBy(() -> adminService.activateUser(Long.MAX_VALUE)).isInstanceOf(ResourceNotFoundException.class)
                                                                       .hasMessage("User not found");

    assertThatThrownBy(() -> adminService.resetUserPassword(Long.MAX_VALUE, request)).isInstanceOf(ResourceNotFoundException.class)
                                                                                     .hasMessage("User not found");
  }

  @Test
  void generateTestDataShouldCreateCompleteTestData() {
    adminService.generateTestData();

    flushAndClear();

    GENERATED_USER_EMAILS.forEach(this::assertGeneratedTestData);
  }

  @Test
  void generateTestDataShouldReplaceExistingTestData() {
    adminService.generateTestData();

    flushAndClear();

    adminService.generateTestData();

    flushAndClear();

    GENERATED_USER_EMAILS.forEach(this::assertGeneratedTestData);

    assertThat(GENERATED_USER_EMAILS.stream().filter(email -> userRepository.findByEmailIgnoreCase(email).isPresent())).hasSize(3);
  }

  private void assertGeneratedTestData(String email) {
    User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();

    assertThat(user.getRole()).isEqualTo(Role.USER);

    assertThat(passwordEncoder.matches(TEST_PASSWORD, findPasswordHash(user.getId()))).isTrue();

    assertThat(notificationSettingsRepository.findByUser_EmailIgnoreCase(email)).isPresent();

    assertThat(planningPeriodRepository.findAllByUser_EmailIgnoreCaseOrderByStartDateDesc(email)).hasSize(1);

    assertThat(studyModuleRepository.findAllByUserEmailIgnoreCaseOrderByCreatedAtDesc(email)).hasSize(4);

    assertThat(learningGoalRepository.findAllByStudyModule_User_EmailIgnoreCaseOrderByCreatedAtDesc(email)).hasSize(8);

    assertThat(milestoneRepository.findAllByLearningGoal_StudyModule_User_EmailIgnoreCase(email)).hasSize(16);

    assertThat(modulePlanRepository.findAllByStudyModule_User_EmailIgnoreCaseOrderByCreatedAtDesc(email)).hasSize(24);

    assertThat(plannedStudySessionRepository.findAllByUser_EmailIgnoreCaseOrderByStartTimeAsc(email)).hasSize(16);

    assertThat(studyTimeRepository.findAllByUser_EmailIgnoreCaseOrderByStartTimeDesc(email)).hasSize(12);

    assertThat(notificationRepository.findAllByUser_EmailIgnoreCaseOrderByCreatedAtDesc(email)).isEmpty();

    assertThat(timerRepository.findByUser_EmailIgnoreCase(email)).isEmpty();
  }

  private Boolean findActiveStatus(Long userId) {
    return entityManager.createQuery("select u.active from User u where u.id = :userId", Boolean.class)
                        .setParameter("userId", userId)
                        .getSingleResult();
  }

  private String findPasswordHash(Long userId) {
    return entityManager.createQuery("select u.passwordHash from User u where u.id = :userId", String.class)
                        .setParameter("userId", userId)
                        .getSingleResult();
  }
}