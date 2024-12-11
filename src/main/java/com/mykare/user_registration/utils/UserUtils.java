package com.mykare.user_registration.utils;

import com.mykare.user_registration.dto.UserRequestDTO;
import com.mykare.user_registration.dto.UserResponseDTO;
import com.mykare.user_registration.model.Gender;
import com.mykare.user_registration.model.Role;
import com.mykare.user_registration.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserUtils {


    public User dtoToUser(UserRequestDTO userRequestDTO, PasswordEncoder passwordEncoder) {
        User user = new User();
        user.setName(userRequestDTO.name());
        user.setEmail(userRequestDTO.email());
        Gender userGender = getGender(userRequestDTO.gender());
        user.setGender(userGender);
        user.setPassword(passwordEncoder.encode(userRequestDTO.password()));
        user.setRole(Role.USER);
        return user;
    }

    public UserResponseDTO userToDto(User user) {
        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getGender(), user.getRole());
    }

    public Gender getGender(String gender) {
        Gender userGender = Gender.NOT_MENTIONED;
        if(gender.equalsIgnoreCase("male"))
            userGender = Gender.MALE;
        else if(gender.equalsIgnoreCase("female"))
            userGender = Gender.FEMALE;
        else
            userGender = Gender.OTHER;
        return userGender;
    }
}