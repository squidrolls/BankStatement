package com.example.elaine.service;


import com.example.elaine.dao.AccountRepository;
import com.example.elaine.dao.TransactionRepository;
import com.example.elaine.dto.TransactionDTO;
import com.example.elaine.entity.Account;
import com.example.elaine.entity.Transaction;
import com.example.elaine.entity.TransactionType;
import com.example.elaine.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;

    private final AccountRepository accountRepository;

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    public List<TransactionDTO> getTransactionsByAccountNumber(String accountNumber) {
        Account account = getAccount(accountNumber);

        List<Transaction> transactions = transactionRepository.findByAccount(account);

        return transactions.stream().map(transaction -> new TransactionDTO(
                transaction.getId(),
                transaction.getDate(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getType(),
                account.getAccountNumber()
        )).collect(Collectors.toList());
    }


    private Account getAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account Number " + accountNumber + " not found"));
    }

    @Transactional
    public TransactionDTO createTransaction(TransactionDTO transactionDTO) {
        String accountNumber = transactionDTO.getAccountNumber();
        Account account = getAccount(accountNumber);

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setDescription(transactionDTO.getDescription());
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setDate(LocalDateTime.now());
        transaction.setType(transactionDTO.getType());

        updateAccountBalance(account, transaction);

        transaction = transactionRepository.save(transaction);
        accountRepository.save(account);

        return new TransactionDTO(
                transaction.getId(),
                transaction.getDate(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getType(),
                account.getAccountNumber(),
                account.getBalance()
        );
    }

    private void updateAccountBalance(Account account, Transaction transaction) {
        BigDecimal currentBalance = account.getBalance();
        if(transaction.getType() == TransactionType.DEPOSIT){
            account.setBalance(currentBalance.add(transaction.getAmount()));
        }else if(transaction.getType() == TransactionType.WITHDRAWAL){
            account.setBalance(currentBalance.subtract(transaction.getAmount()));
            if(account.getBalance().compareTo(BigDecimal.ZERO) < 0){
                throw new IllegalStateException("Insufficient funds for the withdrawal.");
            }
        }else{
            throw new IllegalStateException("Unsupported transaction type");
        }
    }

    //todo: if the type is not write, send the exception
}