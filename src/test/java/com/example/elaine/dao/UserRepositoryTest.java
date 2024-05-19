package com.example.elaine.dao;

import com.example.elaine.AbstractTestcontainers;
import com.example.elaine.entity.Account;
import com.example.elaine.entity.AccountStatus;
import com.example.elaine.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@SpringBootTest
class UserRepositoryTest extends AbstractTestcontainers {

    @Autowired
    private UserRepository underTest;

    @Autowired
    private ApplicationContext applicationContext;

    @BeforeEach
    void setUp() {
        System.out.println(applicationContext.getBeanDefinitionCount());
    }

    @Test
    void findAllUsersWithAccounts() {
        // Given
        String email = FAKER.internet().safeEmailAddress() + "-" + UUID.randomUUID();
        User user = new User(
                FAKER.name().firstName(),
                FAKER.name().lastName(),
                email,
                FAKER.internet().password(),
                FAKER.address().fullAddress()
        );

        Account account = new Account(
                null,
                "1234567890",
                BigDecimal.valueOf(1000.0),
                AccountStatus.ACTIVE,
                user,
                new ArrayList<>()
        );

        user.setAccounts(List.of(account));
        underTest.save(user);

        // When
        List<User> usersWithAccounts = underTest.findAllUsersWithAccounts();

        // Then
        assertThat(usersWithAccounts).hasSize(1);
        assertThat(usersWithAccounts.get(0).getAccounts()).hasSize(1);
        assertThat(usersWithAccounts.get(0).getAccounts().get(0).getAccountNumber()).isEqualTo("1234567890");
    }

    @Test
    void findByEmail() {
        //Given
        String email = FAKER.internet().safeEmailAddress() + "-" + UUID.randomUUID();
        User user = new User(
                FAKER.name().firstName(),
                FAKER.name().lastName(),
                email,
                FAKER.internet().password(),
                FAKER.address().fullAddress()
        );

        underTest.save(user);

        Long id = underTest.findAll().stream()
                .filter(c -> c.getEmail().equals(email))
                .map(User::getId)
                .findFirst().orElseThrow();

        //When
        Optional<User> actual = underTest.findByEmail(email);

        //Then
        assertThat(actual).isPresent().hasValueSatisfying(c -> {
            assertThat(c.getId()).isEqualTo(id);
            assertThat(c.getFirstName()).isEqualTo(user.getFirstName());
            assertThat(c.getLastName()).isEqualTo(user.getLastName());
            assertThat(c.getPassword()).isEqualTo(user.getPassword());
            assertThat(c.getAddress()).isEqualTo(user.getAddress());
        } );

    }
}