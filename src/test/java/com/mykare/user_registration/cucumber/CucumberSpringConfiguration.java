package com.mykare.user_registration.cucumber;

import com.mykare.user_registration.UserRegistrationApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = UserRegistrationApplication.class)
public class CucumberSpringConfiguration {
}

