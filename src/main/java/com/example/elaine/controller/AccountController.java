package com.example.elaine.controller;

import com.example.elaine.dto.AccountDTO;
import com.example.elaine.dto.CreateAccountDTO;
import com.example.elaine.service.AccountService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(path = "/api/v1/users/{userId}/accounts")
@RestController
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }


    //1. Get all accounts for a user, not include the transaction
    @GetMapping
    public ResponseEntity<List<AccountDTO>> getAllAccountsForUser(@PathVariable Long userId) {
        logger.debug("Request to get all accounts for user ID: {}", userId);
        List<AccountDTO> accounts = accountService.findAllAccountsForUser(userId);
        logger.debug("Retrieved {} accounts for user ID: {}", accounts.size(), userId);
        return ResponseEntity.ok(accounts);
    }

    //create account for user
    @PostMapping
    public ResponseEntity<AccountDTO> createAccount(@PathVariable Long userId, @RequestBody CreateAccountDTO createAccountDTO) {
        logger.debug("Request to create an account for user ID: {}", userId);
        createAccountDTO.setUserId(userId); // Set the user ID from the path variable
        AccountDTO accountDTO = accountService.createAccount(createAccountDTO);
        logger.debug("Account created for user ID: {}", userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountDTO);
    }

    //delete the account-soft delete
    //could not be reopened
    @DeleteMapping("/{accountNumber}")
    public ResponseEntity<?> closeAccount(@PathVariable Long userId, @PathVariable String accountNumber) {
        logger.debug("Request to close account with account number: {} for user ID: {}", accountNumber, userId);
        accountService.closeAccount(accountNumber);
        logger.debug("Account with account number: {} closed successfully for user ID: {}", accountNumber, userId);
        return ResponseEntity.ok().body("Account closed successfully.");
    }
}
