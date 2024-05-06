package com.example.elaine.dao;

import com.example.elaine.entity.Account;
import com.example.elaine.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccount(Account account);

    //todo: select time frame
//    @Query("SELECT t FROM Transaction t WHERE t.date >= ?1 and t.date <= ?2")
//    List<Transaction> findTransactionsByDateBetween(LocalDate startDate, LocalDate endDate);
}
