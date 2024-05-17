package com.example.elaine.payload;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AccountRegistrationRequest {
    private Long userId;

    @NotNull(message = "Balance must not be null")
    private BigDecimal balance;
}
