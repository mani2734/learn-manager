package com.learnmanager.dto.request.create;

import com.learnmanager.entity.enums.RecurrenceFrequency;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record CreatePlannedStudySessionSeriesRequest(

    @NotNull(message = "Study module ID is required") @Positive(message = "Study module ID must be greater than zero") Long studyModuleId,

    @NotBlank(message = "Title is required") @Size(max = 150, message = "Title must not exceed 150 characters") String title,

    @NotNull(message = "Start time is required") LocalDateTime startTime,

    @NotNull(message = "End time is required") LocalDateTime endTime,

    @NotNull(message = "Recurrence frequency is required") RecurrenceFrequency recurrenceFrequency,

    @NotNull(message = "Occurrence count is required") @Min(value = 2, message = "Occurrence count must be at least 2") @Max(value = 100, message = "Occurrence count must not exceed 100") Integer occurrenceCount

) {

}