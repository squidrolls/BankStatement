package com.example.elaine.service;

import com.example.elaine.dao.AccountRepository;
import com.example.elaine.dao.TransactionRepository;
import com.example.elaine.dao.UserRepository;
import com.example.elaine.entity.*;
import com.example.elaine.exception.NotFoundException;
import com.example.elaine.payload.AccountDTO;
import com.example.elaine.payload.AccountRegistrationRequest;
import com.example.elaine.payload.TransactionDTO;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock private AccountRepository accountRepository;

    @Mock private TransactionRepository transactionRepository;

    @Mock private UserRepository userRepository;

    private AccountService underTest;

    @BeforeEach
    void setUp() {
        underTest = new AccountService(accountRepository, transactionRepository, userRepository);
    }

    //1.create account
    @Test
    void canCreateAccountWithValidRequest() {
        // Given
        Long userId = 1L;
        User user = new User("Elaine", "Yang", "elaine@gmail.com", "password", "123 Main St");
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        BigDecimal initialBalance = BigDecimal.valueOf(1000);
        AccountRegistrationRequest request = new AccountRegistrationRequest(userId, initialBalance);

        when(accountRepository.existsByAccountNumber(any())).thenReturn(false);

        // When
        AccountDTO accountDTO = underTest.createAccount(request);

        // Then
        ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountArgumentCaptor.capture());
        Account capturedAccount = accountArgumentCaptor.getValue();

        assertThat(capturedAccount.getUser()).isEqualTo(user);
        assertThat(capturedAccount.getBalance()).isEqualTo(initialBalance);
        assertThat(capturedAccount.getAccountNumber()).isNotNull();

        ArgumentCaptor<Transaction> transactionArgumentCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionArgumentCaptor.capture());
        Transaction capturedTransaction = transactionArgumentCaptor.getValue();

        assertThat(capturedTransaction.getAccount()).isEqualTo(capturedAccount);
        assertThat(capturedTransaction.getAmount()).isEqualTo(initialBalance);
        assertThat(capturedTransaction.getType()).isEqualTo(TransactionType.DEPOSIT);
    }

    @Test
    void willThrowWhenUserNotFound() {
        // Given
        Long userId = 1L;
        AccountRegistrationRequest request = new AccountRegistrationRequest(userId, BigDecimal.valueOf(1000));

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> underTest.createAccount(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found with id: " + userId);

        // Then
        verify(accountRepository, never()).save(any());
    }

    @Test
    void willThrowWhenInitialBalanceIsNegative() {
        // Given
        Long userId = 1L;
        User user = new User("Elaine", "Yang", "elaine@gmail.com", "password", "123 Main St");
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        BigDecimal negativeBalance = BigDecimal.valueOf(-100);
        AccountRegistrationRequest request = new AccountRegistrationRequest(userId, negativeBalance);

        // When
        assertThatThrownBy(() -> underTest.createAccount(request))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Initial balance must be non-negative.");

        // Then
        verify(accountRepository, never()).save(any());
    }

    @Test
    void generateUniqueAccountNumber() {
        // Given
        Long userId = 1L;
        User user = new User("Elaine", "Yang", "elaine@gmail.com", "password", "123 Main St");
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        BigDecimal initialBalance = BigDecimal.valueOf(1000);
        AccountRegistrationRequest request = new AccountRegistrationRequest(userId, initialBalance);

        // Simulate account number conflict
        when(accountRepository.existsByAccountNumber(any()))
                .thenReturn(true)
                .thenReturn(false);

        // When
        AccountDTO accountDTO = underTest.createAccount(request);

        // Then
        ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountArgumentCaptor.capture());
        Account capturedAccount = accountArgumentCaptor.getValue();

        assertThat(capturedAccount.getAccountNumber()).isNotNull();

        // Verify that generateAccountNumber() was called at least twice
        verify(accountRepository, times(2)).existsByAccountNumber(any());
    }

    //2.get all accounts
    @Test
    void canFindAllAccountsForUser() {
        // Given
        Long userId = 1L;
        User user = new User("Elaine", "Yang", "elaine@gmail.com", "password", "123 Main St");
        user.setId(userId);

        Account account1 = new Account("12345", BigDecimal.valueOf(1000), user);
        account1.setId(1L);
        account1.setStatus(AccountStatus.ACTIVE);

        Account account2 = new Account("67890", BigDecimal.valueOf(2000), user);
        account2.setId(2L);
        account2.setStatus(AccountStatus.ACTIVE);

        List<Account> accounts = List.of(account1, account2);
        when(accountRepository.findByUserId(userId)).thenReturn(accounts);

        // When
        List<AccountDTO> accountDTOs = underTest.findAllAccountsForUser(userId);

        // Then
        assertThat(accountDTOs).hasSize(2);
        assertThat(accountDTOs.get(0).getId()).isEqualTo(account1.getId());
        assertThat(accountDTOs.get(0).getAccountNumber()).isEqualTo(account1.getAccountNumber());
        assertThat(accountDTOs.get(0).getBalance()).isEqualTo(account1.getBalance());
        assertThat(accountDTOs.get(0).getStatus()).isEqualTo(account1.getStatus());

        assertThat(accountDTOs.get(1).getId()).isEqualTo(account2.getId());
        assertThat(accountDTOs.get(1).getAccountNumber()).isEqualTo(account2.getAccountNumber());
        assertThat(accountDTOs.get(1).getBalance()).isEqualTo(account2.getBalance());
        assertThat(accountDTOs.get(1).getStatus()).isEqualTo(account2.getStatus());
    }

    @Test
    void canHandleNoAccountsForUser() {
        // Given
        Long userId = 1L;
        when(accountRepository.findByUserId(userId)).thenReturn(List.of());

        // When
        List<AccountDTO> accountDTOs = underTest.findAllAccountsForUser(userId);

        // Then
        assertThat(accountDTOs).isEmpty();
    }

    //3. delete the account
    @Test
    void canCloseAccount() {
        // Given
        String accountNumber = "12345";
        User user = new User("Elaine", "Yang", "elaine@gmail.com", "password", "123 Main St");
        Account account = new Account(accountNumber, BigDecimal.ZERO, user);
        account.setStatus(AccountStatus.ACTIVE);
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));

        // When
        underTest.closeAccount(accountNumber);

        // Then
        ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountArgumentCaptor.capture());
        Account capturedAccount = accountArgumentCaptor.getValue();

        assertThat(capturedAccount.getStatus()).isEqualTo(AccountStatus.CLOSED);
    }

    @Test
    void willThrowWhenAccountNotFound() {
        // Given
        String accountNumber = "12345";
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> underTest.closeAccount(accountNumber))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Account Number " + accountNumber + " not found");

        // Then
        verify(accountRepository, never()).save(any());
    }

    @Test
    void willThrowWhenAccountAlreadyClosed() {
        // Given
        String accountNumber = "12345";
        User user = new User("Elaine", "Yang", "elaine@gmail.com", "password", "123 Main St");
        Account account = new Account(accountNumber, BigDecimal.ZERO, user);
        account.setStatus(AccountStatus.CLOSED);
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));

        // When
        assertThatThrownBy(() -> underTest.closeAccount(accountNumber))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Account is already closed.");

        // Then
        verify(accountRepository, never()).save(any());
    }

    @Test
    void willThrowWhenAccountBalanceIsNotZero() {
        // Given
        String accountNumber = "12345";
        User user = new User("Elaine", "Yang", "elaine@gmail.com", "password", "123 Main St");
        Account account = new Account(accountNumber, BigDecimal.valueOf(100), user);
        account.setStatus(AccountStatus.ACTIVE);
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));

        // When
        assertThatThrownBy(() -> underTest.closeAccount(accountNumber))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Account cannot be closed because the balance is not zero.");

        // Then
        verify(accountRepository, never()).save(any());
    }

    @Test
    void canMapTransactionToTransactionDTO() {
        // Given
        User user = new User("Elaine", "Yang", "elaine@gmail.com", "password", "123 Main St");
        Account account = new Account("12345", BigDecimal.valueOf(1000), user);
        account.setId(1L);

        Transaction transaction = new Transaction(LocalDateTime.now(), "Deposit", BigDecimal.valueOf(500), TransactionType.DEPOSIT, account);
        transaction.setId(1L);

        // When
        TransactionDTO transactionDTO = underTest.mapTransaction(transaction);

        // Then
        assertThat(transactionDTO.getId()).isEqualTo(transaction.getId());
        assertThat(transactionDTO.getDate()).isEqualTo(transaction.getDate());
        assertThat(transactionDTO.getDescription()).isEqualTo(transaction.getDescription());
        assertThat(transactionDTO.getAmount()).isEqualTo(transaction.getAmount());
        assertThat(transactionDTO.getType()).isEqualTo(transaction.getType());
    }

}
