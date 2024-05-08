package com.example.elaine.config;

import com.example.elaine.dao.AccountRepository;
import com.example.elaine.dao.UserRepository;
import com.example.elaine.entity.*;
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
public class FakeDataConfiguration {
    @Bean
    CommandLineRunner commandLineRunner(AccountRepository accountRepository, UserRepository userRepository) {
        return args -> {
            generateAccountsAndTransactions(userRepository, accountRepository);
        };
    }
    public void generateAccountsAndTransactions(UserRepository userRepository, AccountRepository accountRepository) {
        Faker faker = new Faker();
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyMM");
        AtomicLong SEQUENCE = new AtomicLong(1);  // Starting sequence

        for (int i = 0; i < 10; i++) {
            String accountNumber = generateAccountNumber(DATE_FORMAT, SEQUENCE);

            // Create a User
            BankUser bankUser = new BankUser(
                    faker.name().firstName(),
                    faker.name().lastName(),
                    faker.internet().emailAddress(),
                    faker.internet().password(),// todo: Remember to hash this in production!
                    faker.address().fullAddress()
            );

            userRepository.save(bankUser);

            // Create an Account
            Account account = new Account(accountNumber, BigDecimal.ZERO,bankUser);

            // Transactions with varying amounts and purposes
            LocalDateTime now = LocalDateTime.now();
            BigDecimal balance = BigDecimal.ZERO;
            BigDecimal[] amounts = new BigDecimal[]{
                    BigDecimal.valueOf(1000 + (1000 * i)),
                    BigDecimal.valueOf(2000 + (500 * i)),
                    BigDecimal.valueOf(500 + (100 * i)),
                    BigDecimal.valueOf(300 + (300 * i)),
                    BigDecimal.valueOf(450 + (450 * i)),
                    BigDecimal.valueOf(1500 + (100 * i))
            };

            for (int j = 0; j < 6; j++) {
                TransactionType type = (j % 3 == 0) ? TransactionType.WITHDRAWAL : TransactionType.DEPOSIT;
                // Simulate date variation for transactions
                LocalDateTime transactionDate = now.minusDays(faker.number().numberBetween(1, 60));

                addTransaction(account, transactionDate, "Transaction " + (j + 1), amounts[j], type);

                // Adjust balance based on the type
                if (type == TransactionType.DEPOSIT) {
                    balance = balance.add(amounts[j]);
                } else {
                    balance = balance.subtract(amounts[j]);
                }
            }

            account.setBalance(balance);
            accountRepository.save(account);
        }
    }

    private void addTransaction(Account account, LocalDateTime dateTime, String description, BigDecimal amount, TransactionType type) {
        Transaction transaction = new Transaction(dateTime, description, amount, type,account);
        account.getTransactions().add(transaction);
    }

    private static String generateAccountNumber(SimpleDateFormat DATE_FORMAT, AtomicLong SEQUENCE) {
        String datePart = DATE_FORMAT.format(new Date());
        long sequence = SEQUENCE.getAndIncrement();
        return String.format("%s-%04d-%04d", datePart, sequence / 10000, sequence % 10000);
    }
}
