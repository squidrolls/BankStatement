package com.example.elaine.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.*;

@Entity(name = "Transaction")
@Table(name = "transaction")
@Getter
@Setter
@NoArgsConstructor
public class Transaction {

    @Id
    @SequenceGenerator(name = "transaction_id_sequence", sequenceName = "transaction_id_sequence", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "transaction_id_sequence")
    @Column(name = "id", updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_number", nullable = false, referencedColumnName = "account_number", foreignKey = @ForeignKey(name = "account_id_fk"))
    private Account account;

    @NotNull(message = "date must be not null")
    @Column(name = "date", nullable = false, columnDefinition = "TIMESTAMP WITHOUT TIME ZONE")
    private LocalDateTime date;

    @NotBlank(message = "description must be not empty")
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;


    @NotNull(message = "amount must be not null")
    @Column(name = "amount", nullable = false, precision=19, scale=4)
    private BigDecimal amount;

    @NotNull(message = "transaction type must be not null")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;


    public Transaction(LocalDateTime date, String description, BigDecimal amount, TransactionType type, Account account) {
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.account = account;
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
