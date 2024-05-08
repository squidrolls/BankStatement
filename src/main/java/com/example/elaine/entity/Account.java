package com.example.elaine.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.GenerationType.SEQUENCE;

@Entity(name = "Account")
@Table(name = "account", uniqueConstraints = {@UniqueConstraint(name = "account_number_unique", columnNames = "account_number")})
@Getter
@Setter
@NoArgsConstructor
public class Account {

    @Id
    @SequenceGenerator(name = "account_id_sequence", sequenceName = "account_id_sequence", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "account_id_sequence")
    @Column(name = "id", updatable = false)
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

    public void addTransaction(Transaction transaction){
        if(!transactions.contains(transaction)){
            transactions.add(transaction);
            transaction.setAccount(this);
        }
    }

    public void removeTransaction(Transaction transaction){
        if(transactions.contains(transaction)){
            transactions.remove(transaction);
            transaction.setAccount(null);
        }
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
