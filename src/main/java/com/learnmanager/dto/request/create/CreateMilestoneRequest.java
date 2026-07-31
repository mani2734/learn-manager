package com.learnmanager.dto.request.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateMilestoneRequest(

    @NotNull(message = "Learning goal ID is required") @Positive(message = "Learning goal ID must be greater than zero") Long learningGoalId,

    @NotBlank(message = "Title is required") @Size(max = 150, message = "Title must not exceed 150 characters") String title,

    LocalDate deadline) {

}