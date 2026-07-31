package com.learnmanager.dto.request.create;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreatePlanningPeriodRequest(

    @NotNull(message = "Start date is required") LocalDate startDate) {

}