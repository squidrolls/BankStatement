package com.example.elaine.dto;

import java.math.BigDecimal;
import java.util.List;

public class AccountDTO {
    private Long id;
    private String accountNumber;
    private String firstName;
    private String lastName;
    private BigDecimal balance;
    private List<TransactionDTO> transactions;

    public AccountDTO(Long id, String accountNumber, String firstName, String lastName, BigDecimal balance, List<TransactionDTO> transactions) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.balance = balance;
        this.transactions = transactions;
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public List<TransactionDTO> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionDTO> transactions) {
        this.transactions = transactions;
    }
}
