package com.example.elaine.service;

import com.example.elaine.dao.AccountRepository;
import com.example.elaine.dao.TransactionRepository;
import com.example.elaine.payload.TransactionDTO;
import com.example.elaine.entity.Account;
import com.example.elaine.entity.Transaction;
import com.example.elaine.entity.TransactionType;
import com.example.elaine.exception.NotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public TransactionDTO getTransactionByIdAndAccountNumber(Long transactionId, String accountNumber) {
        accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account not found with account number: " + accountNumber));

        Transaction transaction = transactionRepository.findByIdAndAccount_AccountNumber(transactionId, accountNumber)
                .orElseThrow(() -> new NotFoundException("Transaction not found with id: " + transactionId + " for account number: " + accountNumber));

        return convertToTransactionDTO(transaction);
    }

    public Page<TransactionDTO> getTransactions(String accountNumber, LocalDate startDate, LocalDate endDate, TransactionType type, Pageable pageable) {
        return transactionRepository.findAll((Specification<Transaction>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Join with Account to filter by accountNumber
            predicates.add(criteriaBuilder.equal(root.get("account").get("accountNumber"), accountNumber));

            // Date range filter
            if (startDate != null && endDate != null) {
                predicates.add(criteriaBuilder.between(root.get("date"), startDate.atStartOfDay(), endDate.atTime(23, 59, 59)));
            }

            // Transaction type filter
            if (type != null) predicates.add(criteriaBuilder.equal(root.get("type"), type));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageable).map(this::convertToTransactionDTO);
    }


    private TransactionDTO convertToTransactionDTO(Transaction transaction) {
        return new TransactionDTO(transaction.getId(), transaction.getDate(), transaction.getDescription(), transaction.getAmount(), transaction.getType()
        );
    }

    @Transactional
    public TransactionDTO createTransaction(String accountNumber, TransactionDTO transactionDTO) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        Transaction transaction = new Transaction(LocalDateTime.now(), transactionDTO.getDescription(), transactionDTO.getAmount(), transactionDTO.getType(), account);

        updateAccountBalance(account, transaction);

        transaction = transactionRepository.save(transaction);
        accountRepository.save(account);

        return new TransactionDTO(transaction.getId(), transaction.getDate(), transaction.getDescription(), transaction.getAmount(), transaction.getType(), account.getBalance());
    }

    private void updateAccountBalance(Account account, Transaction transaction) {
        BigDecimal currentBalance = account.getBalance();
        if(transaction.getType() == TransactionType.DEPOSIT){
            account.setBalance(currentBalance.add(transaction.getAmount()));
        }else{
            account.setBalance(currentBalance.subtract(transaction.getAmount()));
            if(account.getBalance().compareTo(BigDecimal.ZERO) < 0){
                throw new IllegalStateException("Insufficient funds for the withdrawal.");
            }
        }
    }
}