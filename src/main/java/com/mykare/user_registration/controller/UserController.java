package com.mykare.user_registration.controller;

import com.mykare.user_registration.dto.*;
import com.mykare.user_registration.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    @Operation(summary = "To register the user")
    @PostMapping("/v1/register")
    public ResponseEntity<UserResponseDTO> registerUser(@RequestBody @Valid UserRequestDTO userRequestDTO) {
        UserResponseDTO response = userService.registerUser(userRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "To validate the registered user by mail and password")
    @PostMapping("/v1/validate")
    public ResponseEntity<ValidationResponseDTO> loginUser(@RequestBody ValidationRequestDTO validationRequestDTO) {
        ValidationResponseDTO response = userService.validateUser(validationRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @Operation(summary = "To get all users by admin or user by passing their token")
    @GetMapping("/v1/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers( @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        List<UserResponseDTO> users = userService.getAllUsers(page, size);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }
    @Operation(summary = "To delete the user by admin passing admin's token")
    @DeleteMapping("/v1/users/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDeleteResponseDto> deleteUser(@PathVariable String email) {
        UserDeleteResponseDto response = userService.deleteUserByEmail(email);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}