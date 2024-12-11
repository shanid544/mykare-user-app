package com.mykare.user_registration.dto;

public record ValidationResponseDTO(
    String message,
    String token,

    String expirationAfter
) {

}
