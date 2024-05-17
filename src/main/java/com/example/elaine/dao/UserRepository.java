package com.example.elaine.dao;

import com.example.elaine.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("select u from User u left join fetch u.accounts")
    List<User> findAllUsersWithAccounts();
    Optional<User> findByEmail(String email);
}
