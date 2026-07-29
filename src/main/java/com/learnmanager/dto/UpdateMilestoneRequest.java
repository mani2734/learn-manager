package com.learnmanager.dto;

import com.learnmanager.entity.enums.GoalStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateMilestoneRequest(

    @NotBlank(message = "Title is required") @Size(max = 150, message = "Title must not exceed 150 characters") String title,

    LocalDate deadline,

    @NotNull(message = "Status is required") GoalStatus status) {

}