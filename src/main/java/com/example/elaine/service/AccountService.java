package com.example.elaine.service;

import com.example.elaine.dao.AccountRepository;
import com.example.elaine.dto.AccountDTO;
import com.example.elaine.dto.TransactionDTO;
import com.example.elaine.entity.Account;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }


    //TODO:write this by myself
    public List<AccountDTO> getAccounts() {
        return accountRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private AccountDTO convertToDTO(Account account) {
        List<TransactionDTO> transactionDTOs = account.getTransactions().stream()
                .map(transaction -> new TransactionDTO(transaction.getId(), account.getAccountNumber()))
                .collect(Collectors.toList());
        return new AccountDTO(
                account.getId(),
                account.getAccountNumber(),
                account.getFirstName(),
                account.getLastName(),
                account.getBalance(),
                transactionDTOs
        );
    }

    public AccountDTO getAccountByAccountNumber(String accountNumber) {
        return accountRepository.findAll().stream()
                .filter(account -> account.getAccountNumber().equals(accountNumber))
                .findFirst()
                .map(this::convertToDTO)
                .orElseThrow(() -> new IllegalStateException("account not found"));
        //todo: handle this exception


    }
}
