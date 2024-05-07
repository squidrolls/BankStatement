package com.example.elaine.dto;

import com.example.elaine.entity.AccountStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public class AccountDTO {
    private Long id;
    private String accountNumber;
    private BigDecimal balance;

    @NotNull(message = "Status cannot be null")
    private AccountStatus status;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)  // Only include balance if it is not null
    private List<TransactionDTO> transactions;

    public AccountDTO(Long id, String accountNumber, BigDecimal balance, AccountStatus status, List<TransactionDTO> transactions) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.status = status;
        this.transactions = transactions;
    }

    public AccountDTO(Long id, String accountNumber, BigDecimal balance, AccountStatus status) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.status = status;
    }

    public AccountDTO() {
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

//    public List<TransactionDTO> getTransactions() {
//        return transactions;
//    }
//
//    public void setTransactions(List<TransactionDTO> transactions) {
//        this.transactions = transactions;
//    }
}
