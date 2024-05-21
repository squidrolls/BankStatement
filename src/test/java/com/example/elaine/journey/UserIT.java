package com.example.elaine.journey;

import com.example.elaine.payload.UserDTO;
import com.example.elaine.payload.UserRegistrationRequest;
import com.example.elaine.payload.UserUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("dev")
public class UserIT {

    @Autowired
    private WebTestClient webTestClient;

    private static final String USER_PATH = "/api/v1/users";

    private UserDTO expectedUser;

    @BeforeEach
    void setUp() {
        // Initialize expected user data
        expectedUser = new UserDTO();
        expectedUser.setFirstName("Elaine");
        expectedUser.setLastName("Yang");
        expectedUser.setEmail(UUID.randomUUID().toString() + "@gmail.com");
        expectedUser.setAddress("123 Main St");
    }

    @Test
    void canCreateUser() {
        // Create user registration request
        UserRegistrationRequest userRequest = new UserRegistrationRequest(
                expectedUser.getFirstName(),
                expectedUser.getLastName(),
                expectedUser.getEmail(),
                "password",
                expectedUser.getAddress()
        );

        // Send POST request to create user
        webTestClient.post()
                .uri(USER_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(userRequest))
                .exchange()
                .expectStatus().isCreated();

        // Get all users
        List<UserDTO> allUsers = webTestClient.get()
                .uri(USER_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserDTO.class)
                .returnResult()
                .getResponseBody();

        // Ensure the user is present
        assertThat(allUsers)
                .isNotEmpty()
                .extracting(UserDTO::getEmail)
                .contains(expectedUser.getEmail());
    }

    @Test
    void canGetAllUsersWithAccounts() {
        // Create user registration request
        UserRegistrationRequest userRequest = new UserRegistrationRequest(
                expectedUser.getFirstName(),
                expectedUser.getLastName(),
                expectedUser.getEmail(),
                "password",
                expectedUser.getAddress()
        );

        // Send POST request to create user
        webTestClient.post()
                .uri(USER_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(userRequest))
                .exchange()
                .expectStatus().isCreated();

        // Get all users with accounts
        List<UserDTO> allUsers = webTestClient.get()
                .uri(USER_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserDTO.class)
                .returnResult()
                .getResponseBody();

        // Ensure the user is present
        assertThat(allUsers)
                .isNotEmpty()
                .extracting(UserDTO::getEmail)
                .contains(expectedUser.getEmail());
    }

    @Test
    void canUpdateUser() {
        // Create user registration request
        UserRegistrationRequest userRequest = new UserRegistrationRequest(
                expectedUser.getFirstName(),
                expectedUser.getLastName(),
                expectedUser.getEmail(),
                "password",
                expectedUser.getAddress()
        );

        // Send POST request to create user
        webTestClient.post()
                .uri(USER_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(userRequest))
                .exchange()
                .expectStatus().isCreated();

        // Get all users
        List<UserDTO> allUsers = webTestClient.get()
                .uri(USER_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserDTO.class)
                .returnResult()
                .getResponseBody();

        Long userId = allUsers.stream()
                .filter(user -> user.getEmail().equals(expectedUser.getEmail()))
                .map(UserDTO::getId)
                .findFirst()
                .orElseThrow();

        // Create user update request
        String newFirstName = "Jane";
        UserUpdateRequest updateRequest = new UserUpdateRequest(
                newFirstName,
                expectedUser.getLastName(),
                expectedUser.getAddress(),
                expectedUser.getEmail(),
                "newpassword"
        );

        // Send PUT request to update user
        webTestClient.put()
                .uri(USER_PATH + "/{userId}", userId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updateRequest))
                .exchange()
                .expectStatus().isOk();

        // Get updated user by ID
        UserDTO updatedUser = webTestClient.get()
                .uri(USER_PATH + "/{userId}", userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDTO.class)
                .returnResult()
                .getResponseBody();

        // Verify the user is updated
        assertThat(updatedUser.getFirstName()).isEqualTo(newFirstName);
    }

    @Test
    void canDeleteUser() {
        // Create user registration request
        UserRegistrationRequest userRequest = new UserRegistrationRequest(
                expectedUser.getFirstName(),
                expectedUser.getLastName(),
                expectedUser.getEmail(),
                "password",
                expectedUser.getAddress()
        );

        // Send POST request to create user
        webTestClient.post()
                .uri(USER_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(userRequest))
                .exchange()
                .expectStatus().isCreated();

        // Get all users
        List<UserDTO> allUsers = webTestClient.get()
                .uri(USER_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserDTO.class)
                .returnResult()
                .getResponseBody();

        Long userId = allUsers.stream()
                .filter(user -> user.getEmail().equals(expectedUser.getEmail()))
                .map(UserDTO::getId)
                .findFirst()
                .orElseThrow();

        // Send DELETE request to delete user
        webTestClient.delete()
                .uri(USER_PATH + "/{userId}", userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        // Verify user deletion
        webTestClient.get()
                .uri(USER_PATH + "/{userId}", userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }
}