package com.example.elaine.controller;

import com.example.elaine.dto.AccountDTO;
import com.example.elaine.dto.AccountStatusUpdateDTO;
import com.example.elaine.dto.AccountUpdateDTO;
import com.example.elaine.dto.CreateAccountDTO;
import com.example.elaine.entity.Account;
import com.example.elaine.service.AccountService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<?> updateAccount(@PathVariable String accountNumber, @RequestBody AccountUpdateDTO accountUpdateDTO){
        AccountDTO updatedAccountDTO = accountService.updateAccount(accountNumber, accountUpdateDTO);
        if (updatedAccountDTO== null) {
            return ResponseEntity.ok("No changes were detected for the account.");
        }
        return ResponseEntity.ok(updatedAccountDTO);
    }

    //delete the account
    @PutMapping("/{accountNumber}/status")
    public ResponseEntity<?> updateAccountStatus(@PathVariable String accountNumber, @Valid @RequestBody AccountStatusUpdateDTO statusUpdateDTO) {
        try {
            accountService.updateAccountStatus(accountNumber, statusUpdateDTO.getStatus());
            return ResponseEntity.ok("Account status updated successfully");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
