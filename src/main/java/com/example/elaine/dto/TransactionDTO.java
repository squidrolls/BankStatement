package com.example.elaine.dto;

import com.example.elaine.entity.TransactionType;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDTO {
    private Long id;
    private LocalDateTime date;
    private String description;
    private BigDecimal amount;
    private TransactionType type;
    private String accountNumber;  // Added to identify the account

    @JsonInclude(JsonInclude.Include.NON_NULL)  // Only include balance if it is not null
    private BigDecimal balance;

    public TransactionDTO() {
    }

    public TransactionDTO(Long id, LocalDateTime date, String description, BigDecimal amount, TransactionType type) {
        this.id = id;
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.type = type;
    }

    public TransactionDTO(Long id, LocalDateTime date, String description, BigDecimal amount, TransactionType type, String accountNumber) {
        this(id, date, description, amount, type);
        this.accountNumber = accountNumber;
    }

    //for creating a transaction
    public TransactionDTO(Long id, LocalDateTime date, String description, BigDecimal amount, TransactionType type, String accountNumber, BigDecimal balance) {
        this(id, date, description, amount, type, accountNumber);
        this.balance = balance;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}
