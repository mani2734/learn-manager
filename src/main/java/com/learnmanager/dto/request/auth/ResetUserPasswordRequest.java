package com.learnmanager.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetUserPasswordRequest(

    @NotBlank(message = "Password is required") @Size(min = 8, max = 100, message = "Password must contain between 8 and 100 characters") String password

) {

}