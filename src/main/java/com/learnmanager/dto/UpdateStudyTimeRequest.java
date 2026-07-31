package com.learnmanager.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record UpdateStudyTimeRequest(

    @Positive(message = "Learning goal ID must be greater than zero") Long learningGoalId,

    @Positive(message = "Planned study session ID must be greater than zero") Long plannedStudySessionId,

    @NotNull(message = "Start time is required") LocalDateTime startTime,

    @NotNull(message = "End time is required") LocalDateTime endTime

) {

}