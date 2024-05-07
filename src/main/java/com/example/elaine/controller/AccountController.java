package com.example.elaine.controller;

import com.example.elaine.dto.AccountDTO;
import com.example.elaine.dto.CreateAccountDTO;
import com.example.elaine.entity.Account;
import com.example.elaine.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(path = "/api/v1/users/{userId}/accounts")
@RestController
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }


    //1. Get all accounts for a user, not include the transaction
    @GetMapping
    public ResponseEntity<List<AccountDTO>> getAllAccountsForUser(@PathVariable Long userId) {
        List<AccountDTO> accounts = accountService.findAllAccountsForUser(userId);
        return ResponseEntity.ok(accounts);
    }

    //create account for user
    @PostMapping
    public ResponseEntity<AccountDTO> createAccount(@PathVariable Long userId, @RequestBody CreateAccountDTO createAccountDTO) {
        createAccountDTO.setUserId(userId); // Set the user ID from the path variable
        AccountDTO accountDTO = accountService.createAccount(createAccountDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountDTO);
    }

    //delete the account-soft delete
    //could not be reopened
    @DeleteMapping("/{accountNumber}")
    public ResponseEntity<?> closeAccount(@PathVariable Long userId, @PathVariable String accountNumber) {
        accountService.closeAccount(accountNumber);
        return ResponseEntity.ok().body("Account closed successfully.");
    }
}
