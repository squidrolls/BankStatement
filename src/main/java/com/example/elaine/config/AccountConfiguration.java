package com.example.elaine.config;

import com.example.elaine.dao.AccountRepository;
import com.example.elaine.entity.Account;
import com.example.elaine.entity.Transaction;
import com.example.elaine.entity.TransactionType;
import com.github.javafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

@Configuration
public class AccountConfiguration {
    @Bean
    CommandLineRunner commandLineRunner(AccountRepository accountRepository) {
        return args -> {
            //todo: use the page and sort when get all transaction  - sort by date and firstName

//			generatedRandomAccounts(accountRepository);

            generatedAccountsAndTransactions(accountRepository);

        };
    }

    private static void generatedAccountsAndTransactions(AccountRepository accountRepository) {
        Faker faker = new Faker();
        BigDecimal balance = new BigDecimal(faker.number().randomDouble(2, 2000, 5000));
        Account account = new Account(
                "240631000000",
                faker.name().firstName(),
                faker.name().lastName(),
                balance
        );

        //add two transactions
        account.addTransaction(new Transaction(
                LocalDateTime.now().minusDays(4),
                "Initial deposit",
                BigDecimal.valueOf(1000),
                TransactionType.DEPOSIT));
        account.addTransaction(new Transaction(
                LocalDateTime.now().minusDays(2),
                "Deposit",
                balance.subtract(BigDecimal.valueOf(990)),
                TransactionType.DEPOSIT));
        account.addTransaction(new Transaction(
                LocalDateTime.now().minusDays(2),
                "Withdrawal",
                BigDecimal.TEN,
                TransactionType.WITHDRAWAL));

        accountRepository.save(account);
    }

    private static void generatedRandomAccounts(AccountRepository accountRepository) {
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
