package com.example.elaine.controller;

import com.example.elaine.payload.TransactionDTO;
import com.example.elaine.entity.TransactionType;
import com.example.elaine.service.TransactionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RequestMapping(path = "/api/v1/users/{userId}/accounts/{accountNumber}/transactions")
@RestController
@Slf4j
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    //1. List Transactions for an Account - pagination
    // filtering by date range, type of transaction (deposit, withdrawal)
    @GetMapping
    public ResponseEntity<Page<TransactionDTO>> listTransactions(
            @PathVariable String accountNumber,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) TransactionType type,
            Pageable pageable) {
        log.debug("Request to list transactions for accountNumber: {}, from: {}, to: {}, type: {}, page details: {}",
                accountNumber, startDate, endDate, type, pageable);

        Page<TransactionDTO> transactions = transactionService.getTransactions(accountNumber, startDate, endDate, type, pageable);
        log.info("Retrieved {} transactions for accountNumber: {}", transactions.getNumberOfElements(), accountNumber);

        return ResponseEntity.ok(transactions);
    }

    //2. View a Specific Transaction
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionDTO> getTransaction(@PathVariable String accountNumber, @PathVariable Long transactionId) {
        TransactionDTO transaction = transactionService.getTransactionByIdAndAccountNumber(transactionId, accountNumber);
        log.info("Transaction with ID: {} retrieved successfully for accountNumber: {}", transactionId, accountNumber);
        return ResponseEntity.ok(transaction);
    }

    //todo:Security
    //    @PreAuthorize("isAuthenticated() and @securityService.canAccessAccount(principal, #accountNumber)")
    //3. Create a Transaction - change the balance
    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(@PathVariable String accountNumber,@Valid @RequestBody TransactionDTO transactionDTO) {
        log.debug("Request to create a new transaction for accountNumber: {}", accountNumber);
        TransactionDTO createdTransaction = transactionService.createTransaction(accountNumber, transactionDTO);
        return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
    }
}
