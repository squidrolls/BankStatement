package com.example.elaine.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateAccountDTO {

    private Long userId;

    @NotNull(message = "Balance must not be null")
    private BigDecimal balance;
}
