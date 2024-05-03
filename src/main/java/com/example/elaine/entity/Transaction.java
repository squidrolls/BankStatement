package com.example.elaine.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.*;

@Entity(name = "Transaction")
@Table(name = "transaction")
public class Transaction {

    @Id
    @SequenceGenerator(
            name = "transaction_id_sequence",
            sequenceName = "transaction_id_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = SEQUENCE,
            generator = "transaction_id_sequence"
    )
    @Column(
            name = "id",
            updatable = false //could not be updated
    )
    private Long id;

    //todo: one to many
    // fetch type

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false, referencedColumnName = "id", foreignKey = @ForeignKey(name = "account_id_fk"))
    private Account account;


    @Column(name = "date", nullable = false, columnDefinition = "TIMESTAMP WITHOUT TIME ZONE")
    private LocalDateTime date;

    //todo: password
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    //todo: precision = 19, scale = 4
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "type", nullable = false)
    private TransactionType type;

    public Transaction() {

    }

    //todo:needs to be changed
    public Transaction(Account account, String description, BigDecimal amount, TransactionType type) {
        this.account = account;
        this.date = LocalDateTime.now();  // Set the current date and time
        this.description = description;
        this.amount = amount;
        this.type = type;
    }

    public Transaction(LocalDateTime date, String description, BigDecimal amount, TransactionType type) {
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
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

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", account=" + account +
                ", date=" + date +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", type=" + type +
                '}';
    }
}
