package com.example.elaine.journey;

import com.example.elaine.entity.AccountStatus;
import com.example.elaine.payload.AccountDTO;
import com.example.elaine.payload.AccountRegistrationRequest;
import com.example.elaine.payload.UserDTO;
import com.example.elaine.payload.UserRegistrationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.BodyInserters;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("dev")
public class AccountIT {

    @Autowired
    private WebTestClient webTestClient;

    private static final Random RANDOM = new Random();
    private static final String USER_PATH = "/api/v1/users";
    private static final String ACCOUNT_PATH = "/api/v1/users/{userId}/accounts";

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
    void canCreateAccountForUser() {
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

        // Create account registration request with zero balance
        BigDecimal balance = BigDecimal.ZERO.setScale(2);
        AccountRegistrationRequest accountRequest = new AccountRegistrationRequest(
                userId,
                balance
        );

        // Send POST request to create account for the user
        webTestClient.post()
                .uri(ACCOUNT_PATH, userId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(accountRequest))
                .exchange()
                .expectStatus().isCreated();

        // Get all accounts for the user
        List<AccountDTO> allAccounts = webTestClient.get()
                .uri(ACCOUNT_PATH, userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AccountDTO.class)
                .returnResult()
                .getResponseBody();

        // Ensure the account is present by checking if the balance matches
        assertThat(allAccounts)
                .isNotEmpty()
                .extracting(AccountDTO::getBalance)
                .contains(balance);
    }

    @Test
    void canGetAllAccountsForUser() {
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

        // Create account registration request
        BigDecimal balance = BigDecimal.valueOf(RANDOM.nextInt(10000)).setScale(2);
        AccountRegistrationRequest accountRequest = new AccountRegistrationRequest(
                userId,
                balance
        );

        // Send POST request to create account for the user
        webTestClient.post()
                .uri(ACCOUNT_PATH, userId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(accountRequest))
                .exchange()
                .expectStatus().isCreated();

        // Get all accounts for the user
        List<AccountDTO> allAccounts = webTestClient.get()
                .uri(ACCOUNT_PATH, userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AccountDTO.class)
                .returnResult()
                .getResponseBody();

        // Ensure the account is present by checking if the balance matches
        assertThat(allAccounts)
                .isNotEmpty()
                .extracting(AccountDTO::getBalance)
                .contains(balance);
    }

    @Test
    void canDeleteAccountForUser() {
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

        // Create account registration request with zero balance
        BigDecimal balance = BigDecimal.ZERO.setScale(2);
        AccountRegistrationRequest accountRequest = new AccountRegistrationRequest(
                userId,
                balance
        );

        // Send POST request to create account for the user
        webTestClient.post()
                .uri(ACCOUNT_PATH, userId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(accountRequest))
                .exchange()
                .expectStatus().isCreated();

        // Get all accounts for the user
        List<AccountDTO> allAccounts = webTestClient.get()
                .uri(ACCOUNT_PATH, userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AccountDTO.class)
                .returnResult()
                .getResponseBody();

        // Ensure the account is present by checking if the balance matches
        assertThat(allAccounts)
                .isNotEmpty()
                .extracting(AccountDTO::getBalance)
                .contains(balance);

        String accountNumber = allAccounts.stream()
                .map(AccountDTO::getAccountNumber)
                .findFirst()
                .orElseThrow();

        // Send DELETE request to close the account
        webTestClient.delete()
                .uri(ACCOUNT_PATH + "/{accountNumber}", userId, accountNumber)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        // Verify account status is CLOSED
        AccountDTO closedAccount = webTestClient.get()
                .uri(ACCOUNT_PATH + "/{accountNumber}", userId, accountNumber)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AccountDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(closedAccount.getStatus()).isEqualTo(AccountStatus.CLOSED);
    }


}
