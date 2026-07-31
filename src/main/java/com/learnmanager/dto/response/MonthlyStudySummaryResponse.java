package com.learnmanager.dto.response;

import java.math.BigDecimal;

public record MonthlyStudySummaryResponse(long learnedMinutes, long plannedMinutes, BigDecimal progressPercentage) {

}