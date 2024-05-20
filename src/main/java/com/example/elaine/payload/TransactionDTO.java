package com.example.elaine.payload;

import com.example.elaine.entity.TransactionType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private Long id;
    private LocalDateTime date;
    private String description;
    private BigDecimal amount;
    private TransactionType type;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal balance;

    public TransactionDTO(Long id, LocalDateTime date, String description, BigDecimal amount, TransactionType type) {
        this.id = id;
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.type = type;
    }
}

