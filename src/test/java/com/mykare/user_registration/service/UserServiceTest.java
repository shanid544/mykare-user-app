package com.mykare.user_registration.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.mykare.user_registration.config.JwtService;
import com.mykare.user_registration.dto.*;
import com.mykare.user_registration.exception.AccessDeniedException;
import com.mykare.user_registration.exception.InvalidCredentialsException;
import com.mykare.user_registration.exception.UserAlreadyExistsException;
import com.mykare.user_registration.exception.UserNotFoundException;
import com.mykare.user_registration.model.Gender;
import com.mykare.user_registration.model.Role;
import com.mykare.user_registration.model.User;
import com.mykare.user_registration.repo.UserRepository;
import com.mykare.user_registration.utils.UserUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private UserUtils userUtils;
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private JwtService jwtService;
    
    @InjectMocks
    private UserService userService; 

    private UserRequestDTO userRequestDTO;

    @Value("${admin.email}")
    private String adminEmail;

    @BeforeEach
    void setUp() {
       
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(userService, "adminEmail", "admin@example.com");
        userRequestDTO = 
                new UserRequestDTO("tesuser", "test@mykare.com", "MALE", "password123");
    }

    @Test
    void testRegisterUser_WhenEmailExists_ShouldThrowException() {
    
        when(userRepository.existsByEmail(userRequestDTO.email())).thenReturn(true);
        
        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> {
            userService.registerUser(userRequestDTO);
        });
        assertEquals("User with email " + userRequestDTO.email() + " already exists", exception.getMessage());
        
        verify(userRepository, times(1)).existsByEmail(userRequestDTO.email());
    }

    @Test
    void testRegisterUser_WhenEmailDoesNotExist_ShouldReturnUserResponseDTO() {
        when(userRepository.existsByEmail(userRequestDTO.email())).thenReturn(false);

        User user = new User(1L, "tesuser", "test@mykare.com", Gender.MALE, "password123", Role.ADMIN);
        when(userUtils.dtoToUser(userRequestDTO, passwordEncoder)).thenReturn(user);

        when(userRepository.save(user)).thenReturn(user);

        UserResponseDTO userResponseDTO = new UserResponseDTO(1L, "tesuser", "test@mykare.com", Gender.MALE, Role.ADMIN);
        when(userUtils.userToDto(user)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.registerUser(userRequestDTO);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("tesuser", result.name());
        assertEquals("test@mykare.com", result.email());
        assertEquals(Gender.MALE, result.gender());
        assertEquals(Role.ADMIN, result.role());

        verify(userRepository, times(1)).existsByEmail(userRequestDTO.email());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void validateUser_ShouldReturnValidationResponseDTO_WhenCredentialsAreValid() {

        String email = "test@mykare.com";
        String password = "password123";
        String token = "mockJwtToken";


        long expirationTime = 3600000;

        ValidationRequestDTO validationRequestDTO = new ValidationRequestDTO(email, password);

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtService.generateToken(authentication, Role.USER.toString())).thenReturn(token);
        when(jwtService.getExpirationTime()).thenReturn(expirationTime);


        ValidationResponseDTO response = userService.validateUser(validationRequestDTO);


        assertNotNull(response);
        assertEquals("User validated successfully", response.message());
        assertEquals(token, response.token());
        assertEquals("1 hour", response.expirationAfter());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateToken(authentication, Role.USER.toString());
    }

    @Test
    void validateUser_ShouldReturnAdminToken_WhenAdminCredentialsAreUsed() {

        String email = "admin@example.com";
        String password = "adminpassword";
        String token = "mockAdminJwtToken";

        ValidationRequestDTO validationRequestDTO = new ValidationRequestDTO(email, password);

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtService.generateToken(authentication, Role.ADMIN.toString())).thenReturn(token);

        ValidationResponseDTO response = userService.validateUser(validationRequestDTO);

        assertNotNull(response);
        assertEquals("User validated successfully", response.message());
        assertEquals(token, response.token());
        verify(jwtService, times(1)).generateToken(authentication, Role.ADMIN.toString());
    }

    @Test
    void validateUser_ShouldThrowInvalidCredentialsException_WhenAuthenticationFails() {

        String email = "invalid@mykare.com";
        String password = "wrongpassword";
        ValidationRequestDTO validationRequestDTO = new ValidationRequestDTO(email, password);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> userService.validateUser(validationRequestDTO)
        );
        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void testGetAllUsers_Success() {

        User user1 = new User(1L, "User One", "user1@example.com", Gender.MALE, "password1", Role.USER);
        User user2 = new User(2L, "User Two", "user2@example.com", Gender.FEMALE, "password2", Role.ADMIN);

        Page<User> userPage = new PageImpl<>(Arrays.asList(user1, user2));
        when(userRepository.findAll(PageRequest.of(0, 2))).thenReturn(userPage);

        UserResponseDTO responseDTO1 = new UserResponseDTO(1L, "User One", "user1@example.com", Gender.MALE, Role.USER);
        UserResponseDTO responseDTO2 = new UserResponseDTO(2L, "User Two", "user2@example.com", Gender.FEMALE, Role.ADMIN);

        when(userUtils.userToDto(user1)).thenReturn(responseDTO1);
        when(userUtils.userToDto(user2)).thenReturn(responseDTO2);


        List<UserResponseDTO> result = userService.getAllUsers(0, 2);


        assertEquals(2, result.size());
        assertEquals("User One", result.get(0).name());
        assertEquals("User Two", result.get(1).name());

        verify(userRepository, times(1)).findAll(PageRequest.of(0, 2));
        verify(userUtils, times(1)).userToDto(user1);
        verify(userUtils, times(1)).userToDto(user2);
    }

    @Test
    void testGetAllUsers_EmptyResult() {

        Page<User> emptyPage = new PageImpl<>(Collections.emptyList());
        when(userRepository.findAll(PageRequest.of(0, 2))).thenReturn(emptyPage);


        List<UserResponseDTO> result = userService.getAllUsers(0, 2);


        assertTrue(result.isEmpty());

        verify(userRepository, times(1)).findAll(PageRequest.of(0, 2));
        verifyNoInteractions(userUtils);
    }

    @Test
    void deleteUserByEmail_UserDeletedSuccessfully() {

        String userEmail = "user@mykare.com";
        User user = new User();
        user.setEmail(userEmail);

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        UserDeleteResponseDto response = userService.deleteUserByEmail(userEmail);

        assertNotNull(response);
        assertEquals(userEmail, response.email());
        assertEquals("User deleted successfully", response.message());
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void deleteUserByEmail_UserNotFound_ThrowsException() {

        String userEmail = "nonexistent@mykare.com";
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () ->
                userService.deleteUserByEmail(userEmail));
        assertEquals("User not found", exception.getMessage());
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteUserByEmail_AdminCannotBeDeleted_ThrowsException() {

        String adminEmail = "admin@example.com";
        User adminUser = new User();
        adminUser.setEmail(adminEmail);

        when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(adminUser));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () ->
                userService.deleteUserByEmail(adminEmail));
        assertEquals("An admin is not allowed to delete admin.", exception.getMessage());
        verify(userRepository, never()).delete(any(User.class));
    }


}
