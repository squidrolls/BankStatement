package com.example.elaine.service;

import com.example.elaine.dao.AccountRepository;
import com.example.elaine.dao.TransactionRepository;
import com.example.elaine.dao.UserRepository;
import com.example.elaine.dto.AccountDTO;
import com.example.elaine.dto.CreateAccountDTO;
import com.example.elaine.dto.TransactionDTO;
import com.example.elaine.entity.*;
import com.example.elaine.exception.NotFoundException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyMM");
    private final Random random = new Random();

    //1.create Account - generate random account number
    @Transactional
    public AccountDTO createAccount(CreateAccountDTO createAccountDTO) {
        BankUser bankUser = userRepository.findById(createAccountDTO.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found with id: " + createAccountDTO.getUserId()));

        if (createAccountDTO.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Initial balance must be non-negative.");
        }

        Account account = null;
        final int maxAttempts = 5;
        int attempt = 0;
        boolean isAccountCreated = false;

        while (!isAccountCreated && attempt < maxAttempts) {
            try {
                String accountNumber = generateAccountNumber();
                account = new Account(accountNumber, createAccountDTO.getBalance(), bankUser);
                accountRepository.save(account);
                isAccountCreated = true;
            } catch (ConstraintViolationException e) {
                attempt++;
                // todo: Log the exception and retry
            }
        }

        if (!isAccountCreated) {
            throw new IllegalStateException("Failed to generate a unique account number after " + maxAttempts + " attempts.");
        }

        if (createAccountDTO.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            Transaction transaction = new Transaction(LocalDateTime.now(), "Initial deposit", createAccountDTO.getBalance(), TransactionType.DEPOSIT, account);
            transactionRepository.save(transaction);
        }
        return convertToDTO(account);
    }

    //no need to add synchronized
    public String generateAccountNumber() {
        // Format the current date
        String datePart = DATE_FORMAT.format(new Date());

        // Generate two four-digit random numbers
        int randomPart1 = 1000 + random.nextInt(9000); // ensures the number is always four digits
        int randomPart2 = 1000 + random.nextInt(9000); // ensures the number is always four digits

        // Construct the account number
        return String.format("%s-%04d-%04d", datePart, randomPart1, randomPart2);
    }

    //2. get all accounts
    public List<AccountDTO> findAllAccountsForUser(Long userId) {
        List<Account> accounts = accountRepository.findByBankUserId(userId);
        return accounts.stream()
                .map(account -> new AccountDTO(account.getId(), account.getAccountNumber(), account.getBalance(), account.getStatus()))
                .collect(Collectors.toList());
    }

    //3.delete the account - soft delete
    @Transactional
    public void closeAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account Number " + accountNumber + " not found"));

        if(account.getStatus().equals(AccountStatus.CLOSED)){
            throw new IllegalStateException("Account is already closed.");
        }

        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("Account cannot be closed because the balance is not zero.");
        }

        account.setStatus(AccountStatus.CLOSED);
        accountRepository.save(account);
    }

    //Mapping entities to DTOs
    public AccountDTO convertToDTO(Account account) {
        List<TransactionDTO> transactionDTOS = account.getTransactions().stream()
                .map(this::mapTransaction)
                .collect(Collectors.toList());

        return new AccountDTO(account.getId(), account.getAccountNumber(), account.getBalance(), account.getStatus(), transactionDTOS);
    }

    private TransactionDTO mapTransaction(Transaction transaction) {
        return new TransactionDTO(transaction.getId(), transaction.getDate(), transaction.getDescription(), transaction.getAmount(), transaction.getType());
    }
}
