package com.example.elaine.service;

import com.example.elaine.dao.AccountRepository;
import com.example.elaine.dao.TransactionRepository;
import com.example.elaine.entity.Account;
import com.example.elaine.entity.Transaction;
import com.example.elaine.entity.TransactionType;
import com.example.elaine.exception.NotFoundException;
import com.example.elaine.payload.TransactionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;

    @Mock private AccountRepository accountRepository;

    private TransactionService underTest;

    @BeforeEach
    void setUp() {
        underTest = new TransactionService(transactionRepository, accountRepository);
    }

    @Test
    void canGetTransactionByIdAndAccountNumber() {
        // Given
        String accountNumber = "12345";
        Long transactionId = 1L;
        Account account = new Account(accountNumber, BigDecimal.valueOf(1000), null);
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));

        Transaction transaction = new Transaction(transactionId, account, LocalDateTime.now(), "Test transaction", BigDecimal.valueOf(100), TransactionType.DEPOSIT);
        when(transactionRepository.findByIdAndAccount_AccountNumber(transactionId, accountNumber)).thenReturn(Optional.of(transaction));

        // When
        TransactionDTO transactionDTO = underTest.getTransactionByIdAndAccountNumber(transactionId, accountNumber);

        // Then
        assertThat(transactionDTO.getId()).isEqualTo(transaction.getId());
        assertThat(transactionDTO.getDate()).isEqualTo(transaction.getDate());
        assertThat(transactionDTO.getDescription()).isEqualTo(transaction.getDescription());
        assertThat(transactionDTO.getAmount()).isEqualTo(transaction.getAmount());
        assertThat(transactionDTO.getType()).isEqualTo(transaction.getType());
    }

    @Test
    void willThrowWhenAccountNotFound() {
        // Given
        String accountNumber = "12345";
        Long transactionId = 1L;
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> underTest.getTransactionByIdAndAccountNumber(transactionId, accountNumber))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Account not found with account number: " + accountNumber);

        // Then
        verify(transactionRepository, never()).findByIdAndAccount_AccountNumber(transactionId, accountNumber);
    }

    @Test
    void willThrowWhenTransactionNotFound() {
        // Given
        String accountNumber = "12345";
        Long transactionId = 1L;
        Account account = new Account(accountNumber, BigDecimal.valueOf(1000), null);
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));
        when(transactionRepository.findByIdAndAccount_AccountNumber(transactionId, accountNumber)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> underTest.getTransactionByIdAndAccountNumber(transactionId, accountNumber))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Transaction not found with id: " + transactionId + " for account number: " + accountNumber);

        // Then
        verify(transactionRepository).findByIdAndAccount_AccountNumber(transactionId, accountNumber);
    }

    @Test
    void canGetTransactions() {
        // Given
        String accountNumber = "12345";
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        TransactionType type = TransactionType.DEPOSIT;
        Pageable pageable = PageRequest.of(0, 10);

        Account account = new Account(accountNumber, BigDecimal.valueOf(1000), null);

        Transaction transaction1 = new Transaction(1L, account, LocalDateTime.of(2023, 1, 10, 10, 0), "Deposit 1", BigDecimal.valueOf(100), type);
        Transaction transaction2 = new Transaction(2L, account, LocalDateTime.of(2023, 2, 20, 15, 0), "Deposit 2", BigDecimal.valueOf(200), type);

        Page<Transaction> transactionsPage = new PageImpl<>(List.of(transaction1, transaction2), pageable, 2);

        // Only stub necessary interactions
        when(transactionRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(transactionsPage);

        // When
        Page<TransactionDTO> transactionDTOPage = underTest.getTransactions(accountNumber, startDate, endDate, type, pageable);

        // Then
        assertThat(transactionDTOPage.getContent()).hasSize(2);

        TransactionDTO transactionDTO1 = transactionDTOPage.getContent().get(0);
        assertThat(transactionDTO1.getId()).isEqualTo(transaction1.getId());
        assertThat(transactionDTO1.getDate()).isEqualTo(transaction1.getDate());
        assertThat(transactionDTO1.getDescription()).isEqualTo(transaction1.getDescription());
        assertThat(transactionDTO1.getAmount()).isEqualTo(transaction1.getAmount());
        assertThat(transactionDTO1.getType()).isEqualTo(transaction1.getType());

        TransactionDTO transactionDTO2 = transactionDTOPage.getContent().get(1);
        assertThat(transactionDTO2.getId()).isEqualTo(transaction2.getId());
        assertThat(transactionDTO2.getDate()).isEqualTo(transaction2.getDate());
        assertThat(transactionDTO2.getDescription()).isEqualTo(transaction2.getDescription());
        assertThat(transactionDTO2.getAmount()).isEqualTo(transaction2.getAmount());
        assertThat(transactionDTO2.getType()).isEqualTo(transaction2.getType());

        verify(transactionRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void canHandleNoTransactions() {
        // Given
        String accountNumber = "12345";
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        TransactionType type = TransactionType.DEPOSIT;
        Pageable pageable = PageRequest.of(0, 10);

        Page<Transaction> emptyPage = Page.empty(pageable);

        // Only stub necessary interactions
        when(transactionRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

        // When
        Page<TransactionDTO> transactionDTOPage = underTest.getTransactions(accountNumber, startDate, endDate, type, pageable);

        // Then
        assertThat(transactionDTOPage.getContent()).isEmpty();

        verify(transactionRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void canCreateTransaction() {
        // Given
        String accountNumber = "12345";
        TransactionDTO transactionDTO = new TransactionDTO(null, LocalDateTime.now(), "Deposit", BigDecimal.valueOf(100), TransactionType.DEPOSIT, null);

        Account account = new Account(accountNumber, BigDecimal.valueOf(1000), null);
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));

        Transaction transaction = new Transaction(LocalDateTime.now(), transactionDTO.getDescription(), transactionDTO.getAmount(), transactionDTO.getType(), account);
        transaction.setId(1L);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // When
        TransactionDTO createdTransactionDTO = underTest.createTransaction(accountNumber, transactionDTO);

        // Then
        ArgumentCaptor<Transaction> transactionArgumentCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionArgumentCaptor.capture());
        Transaction capturedTransaction = transactionArgumentCaptor.getValue();

        assertThat(capturedTransaction.getDescription()).isEqualTo(transactionDTO.getDescription());
        assertThat(capturedTransaction.getAmount()).isEqualTo(transactionDTO.getAmount());
        assertThat(capturedTransaction.getType()).isEqualTo(transactionDTO.getType());
        assertThat(capturedTransaction.getAccount()).isEqualTo(account);

        assertThat(createdTransactionDTO.getId()).isEqualTo(transaction.getId());
        assertThat(createdTransactionDTO.getDate()).isEqualTo(transaction.getDate());
        assertThat(createdTransactionDTO.getDescription()).isEqualTo(transaction.getDescription());
        assertThat(createdTransactionDTO.getAmount()).isEqualTo(transaction.getAmount());
        assertThat(createdTransactionDTO.getType()).isEqualTo(transaction.getType());
        assertThat(createdTransactionDTO.getBalance()).isEqualTo(account.getBalance());

        verify(accountRepository).save(account);
    }

    @Test
    void willThrowWhenAccountNotFoundWhenCreatingTransaction() {
        // Given
        String accountNumber = "12345";
        TransactionDTO transactionDTO = new TransactionDTO(null, LocalDateTime.now(), "Deposit", BigDecimal.valueOf(100), TransactionType.DEPOSIT, null);
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> underTest.createTransaction(accountNumber, transactionDTO))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Account not found");

        // Then
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

}