package com.mykare.user_registration.exception;

public record ErrorResponse(String message, int status, String details) {}
