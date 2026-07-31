package com.learnmanager.dto;

import java.math.BigDecimal;

public record ModuleMonthlySummaryResponse(Long studyModuleId, String studyModuleName, long learnedMinutes, long plannedMinutes,
                                           BigDecimal progressPercentage) {

}