package com.example.elaine.service;

import com.example.elaine.dao.AccountRepository;
import com.example.elaine.dao.TransactionRepository;
import com.example.elaine.dto.AccountDTO;
import com.example.elaine.dto.TransactionDTO;
import com.example.elaine.entity.Account;
import com.example.elaine.entity.AccountStatus;
import com.example.elaine.entity.Transaction;
import com.example.elaine.entity.TransactionType;
import com.example.elaine.exception.NotFoundException;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private static final AtomicLong SEQUENCE = new AtomicLong(1000000L); // Start with a long prefix
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyDDD");

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    //1.create Account
    @Transactional
    public Account createAccount(String firstName, String lastName, BigDecimal balance) {
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Initial balance must be non-negative.");
        }
        String accountNumber = generateAccountNumber();
        Account account = new Account(accountNumber, firstName, lastName, balance);
        accountRepository.save(account);

        if (balance.compareTo(BigDecimal.ZERO) > 0) {
            Transaction transaction = new Transaction(account, "Initial deposit", balance, TransactionType.DEPOSIT);
            transactionRepository.save(transaction);
        }
        return account;
    }

    private synchronized String generateAccountNumber() {
        return DATE_FORMAT.format(new Date()) + SEQUENCE.getAndIncrement();
    }

    //2. get all accounts
    public List<AccountDTO> getAccounts(){
        return accountRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    //3. find account by accountNumber
    public AccountDTO getAccountByAccountNumber(String accountNumber) {
        Account account = getAccount(accountNumber);
        return convertToDTO(account);
    }

    //4.update the account
    @Transactional
    public AccountDTO updateAccount(String accountNumber, AccountDTO accountDTO){
        Account account = getAccount(accountNumber);

        boolean isUpdated = false;

        if (!Objects.equals(account.getFirstName(), accountDTO.getFirstName())) {
            account.setFirstName(accountDTO.getFirstName());
            isUpdated = true;
        }
        if (!Objects.equals(account.getLastName(), accountDTO.getLastName())) {
            account.setLastName(accountDTO.getLastName());
            isUpdated = true;
        }

        if (!isUpdated) {
            return null; // Indicate no changes were made
        }

        accountRepository.save(account);
        return convertToDTO(account);
    }


    //5.delete the account - soft delete
    public void updateAccountStatus(String accountNumber, AccountStatus newStatus) {
        Account account = getAccount(accountNumber);

        if (newStatus == AccountStatus.CLOSED && account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("Cannot close account with non-zero balance");
        }

        account.setStatus(newStatus);
        accountRepository.save(account);
    }


    private Account getAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account Number " + accountNumber + " not found"));
    }

    //Mapping entities to DTOs
    public AccountDTO convertToDTO(Account account) {
        List<TransactionDTO> transactionDTOS = account.getTransactions().stream()
                .map(this::mapTransaction)
                .collect(Collectors.toList());

        return new AccountDTO(
                account.getId(),
                account.getAccountNumber(),
                account.getFirstName(),
                account.getLastName(),
                account.getBalance(),
                account.getStatus(),
                transactionDTOS
        );
    }

    private TransactionDTO mapTransaction(Transaction transaction) {
        return new TransactionDTO(
                transaction.getId(),
                transaction.getDate(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getType()
        );
    }


}
