package com.learnmanager.dto;

import com.learnmanager.entity.enums.GoalStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateLearningGoalRequest(

    @NotBlank(message = "Title is required") @Size(max = 150, message = "Title must not exceed 150 characters") String title,

    @NotNull(message = "Workload hours are required") @DecimalMin(value = "0.01", message = "Workload hours must be greater than zero") @Digits(integer = 6, fraction = 2, message = "Workload hours must contain at most 6 integer and 2 decimal digits") BigDecimal workloadHours,

    LocalDate deadline,

    @NotNull(message = "Status is required") GoalStatus status) {

}