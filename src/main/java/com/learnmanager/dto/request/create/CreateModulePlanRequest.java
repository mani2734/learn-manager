package com.learnmanager.dto.request.create;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateModulePlanRequest(

    @NotNull(message = "Planning period ID is required") @Positive(message = "Planning period ID must be greater than zero") Long planningPeriodId,

    @NotNull(message = "Study module ID is required") @Positive(message = "Study module ID must be greater than zero") Long studyModuleId,

    @NotNull(message = "Period number is required") @Min(value = 1, message = "Period number must be between 1 and 6") @Max(value = 6, message = "Period number must be between 1 and 6") Integer periodNumber,

    @NotNull(message = "Planned hours are required") @DecimalMin(value = "0.01", message = "Planned hours must be greater than zero") @Digits(integer = 6, fraction = 2, message = "Planned hours must contain at most 6 integer and 2 decimal digits") BigDecimal plannedHours

) {

}