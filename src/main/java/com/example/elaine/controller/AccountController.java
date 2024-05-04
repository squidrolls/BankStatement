package com.example.elaine.controller;

import com.example.elaine.dto.AccountDTO;
import com.example.elaine.entity.Account;
import com.example.elaine.service.AccountService;
import jakarta.validation.Valid;
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

    @GetMapping(path = "{accountNumber}")
    AccountDTO getAccountByAccountNumber(@PathVariable("accountNumber") String accountNumber){
        return accountService.getAccountByAccountNumber(accountNumber);
    }


    //todo: @PostMapping
    @PostMapping
    void createNewAccount(@Valid @RequestBody Account account){
        System.out.println("POST REQUEST..");
        System.out.println(account);
    }
    //todo:@DeleteMapping
    //todo:@PutMapping

}
