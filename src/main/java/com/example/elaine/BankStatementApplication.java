package com.example.elaine;

import com.example.elaine.dao.AccountRepository;
import com.example.elaine.entity.Account;
import com.example.elaine.entity.Transaction;
import com.example.elaine.entity.TransactionType;
import com.github.javafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

@SpringBootApplication
public class BankStatementApplication {
	public static void main(String[] args) {
		SpringApplication.run(BankStatementApplication.class, args);
	}

}
