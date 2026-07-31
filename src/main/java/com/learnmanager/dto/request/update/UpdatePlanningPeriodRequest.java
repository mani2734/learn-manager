package com.learnmanager.dto.request.update;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdatePlanningPeriodRequest(

    @NotNull(message = "Start date is required") LocalDate startDate

) {

}