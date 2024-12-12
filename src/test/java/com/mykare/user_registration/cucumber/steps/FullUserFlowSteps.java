package com.mykare.user_registration.cucumber.steps;

import com.mykare.user_registration.config.JwtAuthenticationFilter;
import com.mykare.user_registration.config.JwtService;
import com.mykare.user_registration.dto.UserRequestDTO;
import com.mykare.user_registration.dto.ValidationRequestDTO;
import com.mykare.user_registration.dto.ValidationResponseDTO;
import com.mykare.user_registration.repo.UserRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@CucumberContextConfiguration
public class FullUserFlowSteps {

    @LocalServerPort
    String port;

    private final RestTemplate restTemplate = new RestTemplate();

    private String userToken;
    private String adminToken;

    private String allUsers;

    @Value("${admin.email}")
    private String adminEmail;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;


    private String apiUrl = "http://localhost:";


    @Given("the user does not already exist with email {string}")
    public void userDoesNotExistWithEmail(String email) {
        boolean result = userRepository.existsByEmail(email);
        assertFalse(result);
    }

    @When("the user registers with the following details:")
    public void userRegistersWithDetails(DataTable userData) {

        String name = userData.cell(1, 0);
        String email = userData.cell(1, 1);
        String gender = userData.cell(1, 2);
        String password = userData.cell(1, 3);

        UserRequestDTO userRequestDTO = new UserRequestDTO(name, email, gender, password);


        String url = apiUrl + port + "/api/v1/register";
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST,
                new HttpEntity<>(userRequestDTO), Void.class);
        assertEquals(201, response.getStatusCodeValue());
    }

    @Then("the user is successfully registered with email {string}")
    public void theUserIsSuccessfullyRegisteredWithEmail(String email) {
        boolean result = userRepository.existsByEmail(email);
        assertTrue(result);
    }


    @When("the user logs in with email {string} and password {string}")
    public void userLogsInWithCredentials(String email, String password) {
        ValidationRequestDTO loginRequest = new ValidationRequestDTO(email, password);
        String url = apiUrl + port + "/api/v1/validate";
        ResponseEntity<ValidationResponseDTO> response = restTemplate.exchange(url, HttpMethod.POST,
                new HttpEntity<>(loginRequest), ValidationResponseDTO.class);
        assertEquals(200, response.getStatusCodeValue());

        userToken = response.getBody().token();
    }

    @Then("the user should receive a valid JWT token")
    public void userShouldReceiveJWTToken() {
        assertFalse(userToken.isEmpty());
    }


    @Given("the user has a valid JWT token")
    public void userHasValidJWTToken() {
        assertNotNull(userToken);
    }

    @When("the user requests to get all users using the token")
    public void userRequestsAllUsers() {
        String url = apiUrl + port + "/api/v1/users?page=0&size=10";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + userToken);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET,
                new HttpEntity<>(headers), String.class);

        assertEquals(200, response.getStatusCodeValue());

        allUsers = response.getBody();
    }

    @Then("the response should contain the user details")
    public void responseShouldContainUserDetails() {
        assertTrue(allUsers.contains("test@mykare.com"));
    }


    @Given("the admin user exists with email {string}")
    public void adminUserExistsWithEmail(String email) {
        assertTrue(userRepository.existsByEmail(email));
    }

    @Then("the admin gets the token with email {string} and password {string}")
    public void theAdminGetsTheTokenWithEmailAndPassword(String email, String password) {
        ValidationRequestDTO loginRequest = new ValidationRequestDTO(email, password);
        String url = apiUrl + port + "/api/v1/validate";
        ResponseEntity<ValidationResponseDTO> response = restTemplate.exchange(url, HttpMethod.POST,
                new HttpEntity<>(loginRequest), ValidationResponseDTO.class);
        assertEquals(200, response.getStatusCodeValue());

        adminToken = response.getBody().token();
    }


    @When("the admin deletes the user with email {string}")
    public void adminDeletesUserWithEmail(String email) {
        String url = apiUrl + port + "/api/v1/users/" + email;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + adminToken);

        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE,
                new HttpEntity<>(headers), Void.class);

        assertEquals(200, response.getStatusCodeValue());
    }

    @Then("the user with email {string} should be deleted successfully")
    public void userShouldBeDeletedSuccessfully(String email) {
        assertFalse(userRepository.existsByEmail(email));
    }

}
