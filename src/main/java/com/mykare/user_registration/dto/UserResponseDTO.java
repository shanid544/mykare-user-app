package com.mykare.user_registration.dto;

import com.mykare.user_registration.model.Gender;
import com.mykare.user_registration.model.Role;

public record UserResponseDTO(
        Long id,
        String name,
        String email,
        Gender gender,
        Role role
) {
}
