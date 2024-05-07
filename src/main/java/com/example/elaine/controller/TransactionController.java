package com.example.elaine.controller;

import com.example.elaine.dto.TransactionDTO;
import com.example.elaine.entity.TransactionType;
import com.example.elaine.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RequestMapping(path = "/api/v1/users/{userId}/accounts/{accountNumber}/transactions")
@RestController
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }


    //1. List Transactions for an Account - pagination
    // filtering by date range, type of transaction (deposit, withdrawal)
    @GetMapping
    public ResponseEntity<Page<TransactionDTO>> listTransactions(
            @PathVariable String accountNumber,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) TransactionType type,
            Pageable pageable) {
        Page<TransactionDTO> transactions = transactionService.getTransactions(accountNumber, startDate, endDate, type, pageable);
        return ResponseEntity.ok(transactions);
    }


    //2. View a Specific Transaction
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionDTO> getTransaction(@PathVariable Long userId, @PathVariable String accountNumber, @PathVariable Long transactionId) {
        TransactionDTO transaction = transactionService.getTransactionByIdAndAccountNumber(transactionId, accountNumber);
        return ResponseEntity.ok(transaction);
    }

    //todo:Security
    //    @PreAuthorize("isAuthenticated() and @securityService.canAccessAccount(principal, #accountNumber)")
    //3. Create a Transaction - change the balance
    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(@PathVariable String accountNumber, @RequestBody TransactionDTO transactionDTO) {
        TransactionDTO createdTransaction = transactionService.createTransaction(accountNumber, transactionDTO);
        return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
    }

}
