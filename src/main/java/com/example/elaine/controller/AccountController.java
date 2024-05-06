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
import java.util.Objects;

@RequestMapping(path = "/api/v1/accounts")
@RestController
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public List<AccountDTO> getAccounts(){
        return accountService.getAccounts();
    }

    @GetMapping("{accountNumber}")
    public ResponseEntity<AccountDTO> getAccountByAccountNumber(@PathVariable String accountNumber) {
        AccountDTO accountDTO = accountService.getAccountByAccountNumber(accountNumber);
        return ResponseEntity.ok(accountDTO);
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@Valid @RequestBody CreateAccountDTO request) {
        Account account = accountService.createAccount(request.getFirstName(), request.getLastName(), request.getBalance());
        return new ResponseEntity<>(account, HttpStatus.CREATED);
    }

    @PutMapping("{accountNumber}")
    public ResponseEntity<?> updateAccount(@PathVariable String accountNumber, @RequestBody AccountDTO accountDTO){
        AccountDTO updatedAccountDTO = accountService.updateAccount(accountNumber, accountDTO);
        return ResponseEntity.ok(
                Objects.requireNonNullElse(updatedAccountDTO, "No changes were detected for the account."));
    }

    //delete the account
    @PutMapping("/{accountNumber}/status")
    public ResponseEntity<?> updateAccountStatus(@PathVariable String accountNumber, @Valid @RequestBody AccountDTO accountDTO) {
        accountService.updateAccountStatus(accountNumber, accountDTO.getStatus());
        return ResponseEntity.ok("Account status updated successfully");
    }
}
