package com.example.elaine.dto;

import com.example.elaine.entity.AccountStatus;
import jakarta.validation.constraints.NotNull;

public class AccountStatusUpdateDTO {
    @NotNull(message = "Status cannot be null")
    private AccountStatus status;

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }
}

