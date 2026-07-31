package com.learnmanager.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateNotificationSettingsRequest(

    @NotNull(message = "Planned session reminder enabled status is required") Boolean plannedSessionReminderEnabled,

    @NotNull(message = "Planned session reminder minutes are required") @Min(value = 1, message = "Planned session reminder minutes must be at least 1") @Max(value = 1440, message = "Planned session reminder minutes must not exceed 1440") Integer plannedSessionReminderMinutes,

    @NotNull(message = "Inactivity reminder enabled status is required") Boolean inactivityReminderEnabled,

    @NotNull(message = "Inactivity threshold days are required") @Min(value = 1, message = "Inactivity threshold days must be at least 1") @Max(value = 365, message = "Inactivity threshold days must not exceed 365") Integer inactivityThresholdDays,

    @NotNull(message = "Goal deadline reminder enabled status is required") Boolean goalDeadlineReminderEnabled,

    @NotNull(message = "Goal deadline reminder days are required") @Min(value = 1, message = "Goal deadline reminder days must be at least 1") @Max(value = 365, message = "Goal deadline reminder days must not exceed 365") Integer goalDeadlineReminderDays,

    @NotNull(message = "Plan deviation reminder enabled status is required") Boolean planDeviationReminderEnabled,

    @NotNull(message = "Plan deviation threshold percent is required") @Min(value = 1, message = "Plan deviation threshold percent must be at least 1") @Max(value = 100, message = "Plan deviation threshold percent must not exceed 100") Integer planDeviationThresholdPercent

) {

}