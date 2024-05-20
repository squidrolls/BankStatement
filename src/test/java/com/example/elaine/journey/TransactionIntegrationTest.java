package com.example.elaine.journey;

import com.example.elaine.entity.TransactionType;
import com.example.elaine.payload.AccountDTO;
import com.example.elaine.payload.AccountRegistrationRequest;
import com.example.elaine.payload.TransactionDTO;
import com.example.elaine.payload.UserDTO;
import com.example.elaine.payload.UserRegistrationRequest;
import com.example.elaine.util.PagedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class TransactionIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private static final String USER_PATH = "/api/v1/users";
    private static final String ACCOUNT_PATH = "/api/v1/users/{userId}/accounts";
    private static final String TRANSACTION_PATH = "/api/v1/users/{userId}/accounts/{accountNumber}/transactions";

    private UserDTO expectedUser;

    @BeforeEach
    void setUp() {
        expectedUser = new UserDTO();
        expectedUser.setFirstName("John");
        expectedUser.setLastName("Doe");
        expectedUser.setEmail(UUID.randomUUID().toString() + "@example.com");
        expectedUser.setAddress("123 Main St");
    }

    private Long createUser() {
        UserRegistrationRequest userRequest = new UserRegistrationRequest(
                expectedUser.getFirstName(),
                expectedUser.getLastName(),
                expectedUser.getEmail(),
                "password",
                expectedUser.getAddress()
        );

        webTestClient.post()
                .uri(USER_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(userRequest))
                .exchange()
                .expectStatus().isCreated();

        List<UserDTO> allUsers = webTestClient.get()
                .uri(USER_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserDTO.class)
                .returnResult()
                .getResponseBody();

        return allUsers.stream()
                .filter(user -> user.getEmail().equals(expectedUser.getEmail()))
                .map(UserDTO::getId)
                .findFirst()
                .orElseThrow();
    }

    private String createAccount(Long userId) {
        AccountRegistrationRequest accountRequest = new AccountRegistrationRequest(
                userId,
                BigDecimal.ZERO
        );

        webTestClient.post()
                .uri(ACCOUNT_PATH, userId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(accountRequest))
                .exchange()
                .expectStatus().isCreated();

        List<AccountDTO> allAccounts = webTestClient.get()
                .uri(ACCOUNT_PATH, userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AccountDTO.class)
                .returnResult()
                .getResponseBody();

        return allAccounts.stream()
                .map(AccountDTO::getAccountNumber)
                .findFirst()
                .orElseThrow();
    }

    @Test
    void canCreateTransaction() {
        Long userId = createUser();
        String accountNumber = createAccount(userId);

        TransactionDTO transactionRequest = new TransactionDTO(
                null,
                LocalDateTime.now(),
                "Test Deposit",
                BigDecimal.valueOf(1000).setScale(4),
                TransactionType.DEPOSIT
        );

        webTestClient.post()
                .uri(TRANSACTION_PATH, userId, accountNumber)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(transactionRequest))
                .exchange()
                .expectStatus().isCreated();

        Pageable pageable = PageRequest.of(0, 10);
        PagedResponse<TransactionDTO> transactionsPage = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(TRANSACTION_PATH)
                        .queryParam("page", pageable.getPageNumber())
                        .queryParam("size", pageable.getPageSize())
                        .build(userId, accountNumber))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<PagedResponse<TransactionDTO>>() {})
                .returnResult()
                .getResponseBody();

        List<TransactionDTO> transactions = transactionsPage.getContent();

        System.out.println("Transactions: " + transactions);

        assertThat(transactions)
                .isNotEmpty()
                .extracting(TransactionDTO::getDescription)
                .contains("Test Deposit");
    }

    @Test
    void canGetTransactionByIdAndAccountNumber() {
        Long userId = createUser();
        String accountNumber = createAccount(userId);

        TransactionDTO transactionRequest = new TransactionDTO(
                null,
                LocalDateTime.now(),
                "Test Deposit",
                BigDecimal.valueOf(1000).setScale(4),
                TransactionType.DEPOSIT
        );

        TransactionDTO createdTransaction = webTestClient.post()
                .uri(TRANSACTION_PATH, userId, accountNumber)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(transactionRequest))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TransactionDTO.class)
                .returnResult()
                .getResponseBody();

        TransactionDTO transaction = webTestClient.get()
                .uri(TRANSACTION_PATH + "/{transactionId}", userId, accountNumber, createdTransaction.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TransactionDTO.class)
                .returnResult()
                .getResponseBody();

        System.out.println("Transaction: " + transaction);

        assertThat(transaction.getDescription()).isEqualTo("Test Deposit");
        assertThat(transaction.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000).setScale(4));
    }

    @Test
    void canListTransactionsForAccount() {
        Long userId = createUser();
        String accountNumber = createAccount(userId);

        TransactionDTO transactionRequest1 = new TransactionDTO(
                null,
                LocalDateTime.now(),
                "Test Deposit 1",
                BigDecimal.valueOf(1000).setScale(4),
                TransactionType.DEPOSIT
        );

        webTestClient.post()
                .uri(TRANSACTION_PATH, userId, accountNumber)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(transactionRequest1))
                .exchange()
                .expectStatus().isCreated();

        TransactionDTO transactionRequest2 = new TransactionDTO(
                null,
                LocalDateTime.now(),
                "Test Deposit 2",
                BigDecimal.valueOf(2000).setScale(4),
                TransactionType.DEPOSIT
        );

        webTestClient.post()
                .uri(TRANSACTION_PATH, userId, accountNumber)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(transactionRequest2))
                .exchange()
                .expectStatus().isCreated();

        Pageable pageable = PageRequest.of(0, 10);
        PagedResponse<TransactionDTO> transactionsPage = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(TRANSACTION_PATH)
                        .queryParam("page", pageable.getPageNumber())
                        .queryParam("size", pageable.getPageSize())
                        .build(userId, accountNumber))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<PagedResponse<TransactionDTO>>() {})
                .returnResult()
                .getResponseBody();

        List<TransactionDTO> transactions = transactionsPage.getContent();

        System.out.println("Transactions: " + transactions);

        assertThat(transactions)
                .isNotEmpty()
                .extracting(TransactionDTO::getDescription)
                .contains("Test Deposit 1", "Test Deposit 2");
    }
}
