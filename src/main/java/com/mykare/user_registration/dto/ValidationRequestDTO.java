package com.mykare.user_registration.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ValidationRequestDTO(
        @Email(message = "Email must be valid") @NotBlank(message = "Email is required") String email,

        @NotBlank(message = "Password is required") String password
) {
}
