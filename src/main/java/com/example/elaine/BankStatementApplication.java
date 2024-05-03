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

	@Bean
	CommandLineRunner commandLineRunner(AccountRepository accountRepository) {
		return args -> {
			//todo: use the page and sort when get all transaction  - sort by date and firstName

//			generatedRandomAccount(accountRepository);

			AtomicLong SEQUENCE = new AtomicLong(1000000L); // Start with a long prefix
			SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyDDD");

			Faker faker = new Faker();
			Date randomDate = faker.date().between(
					new Date(System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 365)), // 1 year ago
					new Date()
			);
			Account account = new Account(
					DATE_FORMAT.format(randomDate) + SEQUENCE.getAndIncrement(),
					faker.name().firstName(),
					faker.name().lastName(),
					new BigDecimal(faker.number().randomDouble(2, 100, 5000))
			);
			account.addTransaction(new Transaction(
					LocalDateTime.now().minusDays(4),
					"initial deposit",
					BigDecimal.TEN,
					TransactionType.DEPOSIT));
			account.addTransaction(new Transaction(
					LocalDateTime.now().minusDays(2),
					"withdraw",
					BigDecimal.ONE,
					TransactionType.WITHDRAWAL));
			accountRepository.save(account);

		};
	}

	private static void generatedRandomAccount(AccountRepository accountRepository) {
		AtomicLong SEQUENCE = new AtomicLong(1000000L); // Start with a long prefix
		SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyDDD");

		Faker faker = new Faker();
		for (int i = 0; i < 20; i++) {
			Date randomDate = faker.date().between(
					new Date(System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 365)), // 1 year ago
					new Date()
			);
			Account account = new Account(
					DATE_FORMAT.format(randomDate) + SEQUENCE.getAndIncrement(),
					faker.name().firstName(),
					faker.name().lastName(),
					new BigDecimal(faker.number().randomDouble(2, 100, 5000))
			);
			// todo: add fake transaction
			accountRepository.save(account);
		}
	}
}
