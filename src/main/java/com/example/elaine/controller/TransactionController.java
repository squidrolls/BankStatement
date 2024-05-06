package com.example.elaine.controller;

import com.example.elaine.dto.TransactionDTO;
import com.example.elaine.entity.Transaction;
import com.example.elaine.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(path = "/api/v1/accounts/{accountNumber}/transactions")
@RestController
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }


    //todo:Security
//    @PreAuthorize("isAuthenticated() and @securityService.canAccessAccount(principal, #accountNumber)")
    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactionsByAccountNumber(@PathVariable String accountNumber) {
        List<TransactionDTO> transactions = transactionService.getTransactionsByAccountNumber(accountNumber);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(
            @PathVariable String accountNumber,
            @Valid @RequestBody TransactionDTO transactionDTO
    ){
        transactionDTO.setAccountNumber(accountNumber); // Ensure the DTO has the correct account number
        TransactionDTO createdTransaction = transactionService.createTransaction(transactionDTO);
        return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
    }

}
