package com.example.elaine.dao;

import com.example.elaine.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    //todo: select time frame
//    @Query("SELECT t FROM Transaction t WHERE t.date >= ?1 and t.date <= ?2")
//    List<Transaction> findTransactionsByDateBetween(LocalDate startDate, LocalDate endDate);
}
