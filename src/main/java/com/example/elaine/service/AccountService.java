package com.example.elaine.service;

import com.example.elaine.dao.AccountRepository;
import com.example.elaine.dao.TransactionRepository;
import com.example.elaine.dao.UserRepository;
import com.example.elaine.payload.AccountDTO;
import com.example.elaine.payload.AccountRegistrationRequest;
import com.example.elaine.payload.TransactionDTO;
import com.example.elaine.entity.*;
import com.example.elaine.exception.NotFoundException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyMM");
    private final Random random = new Random();

    //1.create Account - generate random account number
    @Transactional
    public AccountDTO createAccount(AccountRegistrationRequest accountRegistrationRequest) {
        User user = userRepository.findById(accountRegistrationRequest.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found with id: " + accountRegistrationRequest.getUserId()));

        if (accountRegistrationRequest.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Initial balance must be non-negative.");
        }

        String accountNumber;
        boolean exists;

        //generate accountNumber
        do {
            accountNumber = generateAccountNumber();
            exists = accountRepository.existsByAccountNumber(accountNumber);
        } while (exists);

        Account account = new Account(accountNumber, accountRegistrationRequest.getBalance(), user);
        accountRepository.save(account);

        if (accountRegistrationRequest.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            Transaction transaction = new Transaction(LocalDateTime.now(), "Initial deposit", accountRegistrationRequest.getBalance(), TransactionType.DEPOSIT, account);
            transactionRepository.save(transaction);
        }

        return convertToDTO(account);
    }


    //no need to add synchronized
    private String generateAccountNumber() {
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
        List<Account> accounts = accountRepository.findByUserId(userId);
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

    public TransactionDTO mapTransaction(Transaction transaction) {
        return new TransactionDTO(transaction.getId(), transaction.getDate(), transaction.getDescription(), transaction.getAmount(), transaction.getType());
    }
}
