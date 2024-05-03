package com.example.elaine.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.GenerationType.SEQUENCE;

@Entity(name = "Account")
@Table(
        name = "account",
        uniqueConstraints = {@UniqueConstraint(name = "account_number_unique", columnNames = "account_number")}
)
public class Account {

    @Id
    @SequenceGenerator(
            name = "account_id_sequence",
            sequenceName = "account_id_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = SEQUENCE,
            generator = "account_id_sequence"
    )
    @Column(
            name = "id",
            updatable = false
    )
    private Long id;

    //TODO: now is max 15, add constrain to only 15
    //todo: orphanRemoval = true
    @Column(name = "account_number", nullable = false, length = 12)
    private String accountNumber;
    //todo: write exception if the accountNumber

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    //todo: need to change the balance!!!- service layer
    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    //bidirectional relationship
    //if a transaction is removed, it will not affect the Account
    //if an account is saved, all the transactions in the transactions collection will be saved
    //if an account is deleted, all the transactions associated with the account will be deleted
    @OneToMany(
            mappedBy = "account",
            orphanRemoval = false,
            cascade = {CascadeType.PERSIST, CascadeType.REMOVE},
            fetch= FetchType.LAZY
    )
    private List<Transaction> transactions = new ArrayList<>();

    public Account() {

    }

    public Account(String accountNumber, String firstName, String lastName, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.balance = balance;
    }

    public void addTransaction(Transaction transaction){
        if(!transactions.contains(transaction)){
            transactions.add(transaction);
            transaction.setAccount(this);
        }
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void removeTransaction(Transaction transaction){
        if(transactions.contains(transaction)){
            transactions.remove(transaction);
            transaction.setAccount(null);
        }
    }


    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", accountNumber='" + accountNumber + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", balance=" + balance +
                '}';
    }

}
