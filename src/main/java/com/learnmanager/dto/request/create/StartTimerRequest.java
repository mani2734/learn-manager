package com.learnmanager.dto.request.create;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StartTimerRequest(

    @NotNull(message = "Study module ID is required") @Positive(message = "Study module ID must be greater than zero") Long studyModuleId,

    @Positive(message = "Learning goal ID must be greater than zero") Long learningGoalId,

    @Positive(message = "Planned study session ID must be greater than zero") Long plannedStudySessionId

) {

}