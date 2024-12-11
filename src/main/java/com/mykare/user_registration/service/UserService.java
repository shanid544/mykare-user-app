package com.mykare.user_registration.service;

import com.mykare.user_registration.config.JwtService;
import com.mykare.user_registration.dto.*;
import com.mykare.user_registration.exception.AccessDeniedException;
import com.mykare.user_registration.exception.InvalidCredentialsException;
import com.mykare.user_registration.exception.UserAlreadyExistsException;
import com.mykare.user_registration.exception.UserNotFoundException;
import com.mykare.user_registration.model.Role;
import com.mykare.user_registration.model.User;
import com.mykare.user_registration.repo.UserRepository;
import com.mykare.user_registration.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserUtils userUtils;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${admin.email}")
    private String adminEmail;

    public UserResponseDTO registerUser(UserRequestDTO userRequestDTO) {
        if (userRepository.existsByEmail(userRequestDTO.email())) {
            log.error("User with email " + userRequestDTO.email() + " already exists");
            throw new UserAlreadyExistsException("User with email " + userRequestDTO.email() + " already exists");
        }
        User user = userUtils.dtoToUser(userRequestDTO, passwordEncoder);
        User savedUser = userRepository.save(user);
        return userUtils.userToDto(savedUser);
    }

    public ValidationResponseDTO validateUser(ValidationRequestDTO validationRequestDTO) {

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            validationRequestDTO.email(),
                            validationRequestDTO.password()
                    )
            );
        } catch (Exception e) {
            log.error("Invalid email or password");
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = adminEmail.equalsIgnoreCase(validationRequestDTO.email())
                ? jwtService.generateToken(authentication, Role.ADMIN.toString())
                : jwtService.generateToken(authentication, Role.USER.toString());

        return new ValidationResponseDTO("User validated successfully",
                token, String.valueOf(jwtService.getExpirationTime() / (1000 * 60 * 60)) + " hour");

    }

    public List<UserResponseDTO> getAllUsers(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<User> users = userRepository.findAll(pageRequest);
        if (users.isEmpty()) {
            return Collections.emptyList();
        }
        return users.stream()
                .map(userUtils::userToDto)
                .collect(Collectors.toList());
    }

    public UserDeleteResponseDto deleteUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (user.getEmail().equalsIgnoreCase(adminEmail)) {
            log.error("An admin is not allowed to delete admin.");
            throw new AccessDeniedException("An admin is not allowed to delete admin.");
        }
        userRepository.delete(user);
        return new UserDeleteResponseDto(user.getEmail(), "User deleted successfully");
    }
}