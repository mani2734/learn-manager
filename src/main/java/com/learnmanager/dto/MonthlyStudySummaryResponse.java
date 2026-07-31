package com.learnmanager.dto;

import java.math.BigDecimal;

public record MonthlyStudySummaryResponse(long learnedMinutes, long plannedMinutes, BigDecimal progressPercentage) {

}