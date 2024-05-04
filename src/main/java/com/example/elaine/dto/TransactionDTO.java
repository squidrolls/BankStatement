package com.example.elaine.dto;

public class TransactionDTO {
    private Long id;
    private String accountNumber; // Only account number to identify the account, not the whole Account object

    public TransactionDTO(Long id, String accountNumber) {
        this.id = id;
        this.accountNumber = accountNumber;
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
}
