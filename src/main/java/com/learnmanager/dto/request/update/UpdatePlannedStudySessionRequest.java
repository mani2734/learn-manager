package com.learnmanager.dto.request.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdatePlannedStudySessionRequest(

    @NotBlank(message = "Title is required") @Size(max = 150, message = "Title must not exceed 150 characters") String title,

    @NotNull(message = "Start time is required") LocalDateTime startTime,

    @NotNull(message = "End time is required") LocalDateTime endTime

) {

}