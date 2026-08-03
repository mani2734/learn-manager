package com.learnmanager.service;

import com.learnmanager.dto.response.ModuleMonthlySummaryResponse;
import com.learnmanager.dto.response.ReportingDashboardResponse;
import com.learnmanager.entity.StudyModule;
import com.learnmanager.entity.User;
import com.learnmanager.testsupport.AbstractIntegrationTest;
import com.learnmanager.testsupport.TestDataFactory.CompleteTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static com.learnmanager.testsupport.TestDataFactory.CURRENT_DATE_TIME;
import static com.learnmanager.testsupport.TestDataFactory.TEST_USER_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;

class ReportingServiceTest extends AbstractIntegrationTest {

  @Autowired private ReportingService reportingService;

  @Test
  void getDashboardShouldAggregateCurrentMonthStudyAndPlannedMinutes() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    testDataFactory.createStudyTime(
        testData.user(),
        testData.studyModule(),
        testData.learningGoal(),
        null,
        CURRENT_DATE_TIME.minusHours(5),
        CURRENT_DATE_TIME.minusHours(4));

    testDataFactory.createPlannedStudySession(
        testData.user(),
        testData.studyModule(),
        "Second planned session",
        CURRENT_DATE_TIME.plusDays(2),
        CURRENT_DATE_TIME.plusDays(2).plusHours(2));

    flushAndClear();

    ReportingDashboardResponse dashboard = reportingService.getDashboard(TEST_USER_EMAIL.toUpperCase());

    assertThat(dashboard.year()).isEqualTo(2026);
    assertThat(dashboard.month()).isEqualTo(8);
    assertThat(dashboard.today().learnedMinutes()).isEqualTo(120);
    assertThat(dashboard.today().sessionCount()).isEqualTo(2);
    assertThat(dashboard.currentMonth().learnedMinutes()).isEqualTo(120);
    assertThat(dashboard.currentMonth().plannedMinutes()).isEqualTo(180);
    assertThat(dashboard.currentMonth().progressPercentage()).isEqualByComparingTo("66.67");
    assertThat(dashboard.recentSessions()).hasSize(2);
  }

  @Test
  void getDashboardShouldReturnPerModuleProgressAndIgnoreForeignData() {
    CompleteTestData testData = testDataFactory.createCompleteTestData();

    StudyModule secondModule = testDataFactory.createStudyModule(testData.user(), "Databases", "DB01", BigDecimal.valueOf(100));

    testDataFactory.createPlannedStudySession(
        testData.user(),
        secondModule,
        "Database planned session",
        CURRENT_DATE_TIME.plusDays(2),
        CURRENT_DATE_TIME.plusDays(2).plusHours(2));

    User otherUser = testDataFactory.createUser("other-user@learnmanager.local");
    StudyModule otherModule = testDataFactory.createStudyModule(otherUser, "Other module", "OTHER01", BigDecimal.valueOf(100));
    testDataFactory.createStudyTime(otherUser, otherModule, null, null, CURRENT_DATE_TIME.minusHours(3), CURRENT_DATE_TIME.minusHours(2));

    flushAndClear();

    ReportingDashboardResponse dashboard = reportingService.getDashboard(TEST_USER_EMAIL);

    assertThat(dashboard.modules()).hasSize(2);

    ModuleMonthlySummaryResponse firstModuleSummary = dashboard.modules()
                                                               .stream()
                                                               .filter(module -> module.studyModuleId()
                                                                                       .equals(testData.studyModule().getId()))
                                                               .findFirst()
                                                               .orElseThrow();

    ModuleMonthlySummaryResponse secondModuleSummary = dashboard.modules()
                                                                .stream()
                                                                .filter(module -> module.studyModuleId().equals(secondModule.getId()))
                                                                .findFirst()
                                                                .orElseThrow();

    assertThat(firstModuleSummary.learnedMinutes()).isEqualTo(60);
    assertThat(firstModuleSummary.plannedMinutes()).isEqualTo(60);
    assertThat(firstModuleSummary.progressPercentage()).isEqualByComparingTo("100.00");
    assertThat(secondModuleSummary.learnedMinutes()).isZero();
    assertThat(secondModuleSummary.plannedMinutes()).isEqualTo(120);
    assertThat(secondModuleSummary.progressPercentage()).isEqualByComparingTo("0.00");
  }

}
