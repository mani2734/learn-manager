package com.learnmanager.dto;

import java.util.List;

public record ReportingDashboardResponse(int year, int month, TodayStudySummaryResponse today, MonthlyStudySummaryResponse currentMonth,
                                         List<ModuleMonthlySummaryResponse> modules, List<RecentStudyTimeResponse> recentSessions) {

}