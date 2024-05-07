package com.example.elaine.dao;

import com.example.elaine.entity.BankUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<BankUser, Long> {
    @Query("select u from BankUser u join fetch u.accounts")
    List<BankUser> findAllUsersWithAccounts();

    Optional<BankUser> findByEmail(String email);
}
