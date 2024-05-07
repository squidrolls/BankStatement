package com.example.elaine.controller;

import com.example.elaine.dto.TransactionDTO;
import com.example.elaine.entity.TransactionType;
import com.example.elaine.service.TransactionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RequestMapping(path = "/api/v1/users/{userId}/accounts/{accountNumber}/transactions")
@RestController
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
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
        logger.debug("Request to list transactions for accountNumber: {}, from: {}, to: {}, type: {}, page details: {}",
                accountNumber, startDate, endDate, type, pageable);
        Page<TransactionDTO> transactions = transactionService.getTransactions(accountNumber, startDate, endDate, type, pageable);
        logger.info("Retrieved {} transactions for accountNumber: {}", transactions.getNumberOfElements(), accountNumber);
        return ResponseEntity.ok(transactions);
    }


    //2. View a Specific Transaction
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionDTO> getTransaction(@PathVariable String accountNumber, @PathVariable Long transactionId) {
        logger.debug("Request to get transaction with ID: {} for accountNumber: {}", transactionId, accountNumber);
        TransactionDTO transaction = transactionService.getTransactionByIdAndAccountNumber(transactionId, accountNumber);
        if (transaction == null) {
            logger.warn("Transaction with ID: {} not found for accountNumber: {}", transactionId, accountNumber);
            return ResponseEntity.notFound().build();
        }
        logger.info("Transaction with ID: {} retrieved successfully for accountNumber: {}", transactionId, accountNumber);
        return ResponseEntity.ok(transaction);
    }


    //todo:Security
    //    @PreAuthorize("isAuthenticated() and @securityService.canAccessAccount(principal, #accountNumber)")
    //3. Create a Transaction - change the balance
    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(@PathVariable String accountNumber, @RequestBody TransactionDTO transactionDTO) {
        logger.debug("Request to create a new transaction for accountNumber: {}", accountNumber);
        TransactionDTO createdTransaction = transactionService.createTransaction(accountNumber, transactionDTO);
        return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
    }

}
