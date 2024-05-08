package com.example.elaine.dto;

import com.example.elaine.entity.AccountStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {

    private Long id;

    private String accountNumber;

    private BigDecimal balance;

    @NotNull(message = "Status cannot be null")
    private AccountStatus status;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)  // Only include balance if it is not null
    private List<TransactionDTO> transactions;

    public AccountDTO(Long id, String accountNumber, BigDecimal balance, AccountStatus status) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.status = status;
    }
}
