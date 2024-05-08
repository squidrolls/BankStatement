package com.example.elaine.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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

    @NotBlank(message = "account number must be not empty")
    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @NotNull(message = "balance must be not null")
    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING) // This annotation ensures the enum values are stored as string
    @Column(name = "status", nullable = false)
    private AccountStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private BankUser bankUser;

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

//    @Version
//    private Long version;  // This field is used for optimistic locking

    public Account(String accountNumber, BigDecimal balance, BankUser bankUser) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.bankUser = bankUser;
        this.status = AccountStatus.ACTIVE;
    }

    public Account() {

    }

    public BankUser getUser() {
        return bankUser;
    }

    public void setUser(BankUser bankUser) {
        this.bankUser = bankUser;
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

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
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

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", accountNumber='" + accountNumber + '\'' +
                ", balance=" + balance +
                ", status=" + status +
                ", user=" + bankUser +
                ", transactions=" + transactions +
                '}';
    }
}
