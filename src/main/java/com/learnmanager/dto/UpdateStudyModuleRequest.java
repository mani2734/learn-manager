package com.learnmanager.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record UpdateStudyModuleRequest(

    @NotBlank(message = "Name is required") @Size(max = 150, message = "Name must not exceed 150 characters") String name,

    @Size(max = 50, message = "Code must not exceed 50 characters") String code,

    @Size(max = 2000, message = "Description must not exceed 2000 characters") String description,

    @NotNull(message = "ECTS are required") @Positive(message = "ECTS must be greater than zero") Integer ects,

    @DecimalMin(value = "0.01", message = "Workload hours must be greater than zero") BigDecimal workloadHours) {

}