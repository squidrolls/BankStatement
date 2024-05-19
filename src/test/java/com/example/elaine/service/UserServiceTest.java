package com.example.elaine.service;

import com.example.elaine.dao.AccountRepository;
import com.example.elaine.dao.UserRepository;
import com.example.elaine.entity.Account;
import com.example.elaine.entity.AccountStatus;
import com.example.elaine.entity.User;
import com.example.elaine.exception.DuplicateResourceException;
import com.example.elaine.exception.NotFoundException;
import com.example.elaine.payload.AccountDTO;
import com.example.elaine.payload.UserDTO;
import com.example.elaine.payload.UserRegistrationRequest;
import com.example.elaine.payload.UserUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;

    @Mock private AccountRepository accountRepository;

    private UserService underTest;

    @BeforeEach
    void setUp() {
        underTest = new UserService(userRepository, accountRepository);
    }

    //1. get all user
    @Test
    void canGetAllUsersWithAccounts() {
        // Given
        User user1 = new User("Elaine", "Yang", "elaine1@gmail.com", "password", "123 Main St");
        user1.setId(1L);
        Account account1 = new Account("12345", BigDecimal.valueOf(1000), user1);
        account1.setId(1L);
        account1.setStatus(AccountStatus.ACTIVE);
        user1.setAccounts(List.of(account1));

        User user2 = new User("John", "Doe", "john.doe@gmail.com", "password", "456 Elm St");
        user2.setId(2L);
        Account account2 = new Account("67890", BigDecimal.valueOf(2000), user2);
        account2.setId(2L);
        account2.setStatus(AccountStatus.ACTIVE);
        user2.setAccounts(List.of(account2));

        List<User> users = List.of(user1, user2);
        when(userRepository.findAllUsersWithAccounts()).thenReturn(users);

        // When
        List<UserDTO> userDTOs = underTest.getAllUsersWithAccounts();

        // Then
        assertThat(userDTOs).hasSize(2);

        UserDTO userDTO1 = userDTOs.get(0);
        assertThat(userDTO1.getId()).isEqualTo(user1.getId());
        assertThat(userDTO1.getFirstName()).isEqualTo(user1.getFirstName());
        assertThat(userDTO1.getLastName()).isEqualTo(user1.getLastName());
        assertThat(userDTO1.getEmail()).isEqualTo(user1.getEmail());
        assertThat(userDTO1.getAccounts()).hasSize(1);

        AccountDTO accountDTO1 = userDTO1.getAccounts().get(0);
        assertThat(accountDTO1.getId()).isEqualTo(account1.getId());
        assertThat(accountDTO1.getAccountNumber()).isEqualTo(account1.getAccountNumber());
        assertThat(accountDTO1.getBalance()).isEqualTo(account1.getBalance());
        assertThat(accountDTO1.getStatus()).isEqualTo(account1.getStatus());

        UserDTO userDTO2 = userDTOs.get(1);
        assertThat(userDTO2.getId()).isEqualTo(user2.getId());
        assertThat(userDTO2.getFirstName()).isEqualTo(user2.getFirstName());
        assertThat(userDTO2.getLastName()).isEqualTo(user2.getLastName());
        assertThat(userDTO2.getEmail()).isEqualTo(user2.getEmail());
        assertThat(userDTO2.getAccounts()).hasSize(1);

        AccountDTO accountDTO2 = userDTO2.getAccounts().get(0);
        assertThat(accountDTO2.getId()).isEqualTo(account2.getId());
        assertThat(accountDTO2.getAccountNumber()).isEqualTo(account2.getAccountNumber());
        assertThat(accountDTO2.getBalance()).isEqualTo(account2.getBalance());
        assertThat(accountDTO2.getStatus()).isEqualTo(account2.getStatus());
    }

    @Test
    void canHandleNoUsers() {
        // Given
        when(userRepository.findAllUsersWithAccounts()).thenReturn(List.of());

        // When
        List<UserDTO> userDTOs = underTest.getAllUsersWithAccounts();

        // Then
        assertThat(userDTOs).isEmpty();
    }

    //2.create new user
    @Test
    void canCreateUser() {
        // Given
        UserRegistrationRequest request = new UserRegistrationRequest("Elaine", "Yang", "elaine@gmail.com", "password", "123 Main St");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        User savedUser = new User(request.firstName(), request.lastName(), request.email(), request.password(), request.address());
        savedUser.setId(1L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        UserDTO userDTO = underTest.createUser(request);

        // Then
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();

        assertThat(capturedUser.getFirstName()).isEqualTo(request.firstName());
        assertThat(capturedUser.getLastName()).isEqualTo(request.lastName());
        assertThat(capturedUser.getEmail()).isEqualTo(request.email());
        assertThat(capturedUser.getPassword()).isEqualTo(request.password());
        assertThat(capturedUser.getAddress()).isEqualTo(request.address());

        assertThat(userDTO.getId()).isEqualTo(savedUser.getId());
        assertThat(userDTO.getFirstName()).isEqualTo(savedUser.getFirstName());
        assertThat(userDTO.getLastName()).isEqualTo(savedUser.getLastName());
        assertThat(userDTO.getEmail()).isEqualTo(savedUser.getEmail());
        assertThat(userDTO.getAddress()).isEqualTo(savedUser.getAddress());
        assertThat(userDTO.getAccounts()).isEmpty();
    }

    @Test
    void willThrowWhenEmailAlreadyExistsWhenCreatingUser() {
        // Given
        UserRegistrationRequest request = new UserRegistrationRequest("Elaine", "Yang", "elaine@gmail.com", "password", "123 Main St");

        User existingUser = new User("Elaine", "Yang", "elaine@gmail.com", "password", "123 Main St");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(existingUser));

        // When
        assertThatThrownBy(() -> underTest.createUser(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email already in use: " + request.email());

        // Then
        verify(userRepository, never()).save(any(User.class));
    }

    //3. update a user
    @Test
    void canUpdateUser() {
        // Given
        Long userId = 1L;
        User user = new User("Elaine", "Yang", "elaine@gmail.com", "password", "123 Main St");
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserUpdateRequest request = new UserUpdateRequest("ElaineNew", "YangNew", "elaine.new@gmail.com", "newpassword", "456 Elm St");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        // When
        underTest.updateUser(userId, request);

        // Then
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();

        assertThat(capturedUser.getFirstName()).isEqualTo(request.firstName());
        assertThat(capturedUser.getLastName()).isEqualTo(request.lastName());
        assertThat(capturedUser.getEmail()).isEqualTo(request.email());
        assertThat(capturedUser.getPassword()).isEqualTo(request.password());
        assertThat(capturedUser.getAddress()).isEqualTo(request.address());
    }

    @Test
    void willThrowWhenUserNotFound() {
        // Given
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest("ElaineNew", "YangNew", "elaine.new@gmail.com", "newpassword", "456 Elm St");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> underTest.updateUser(userId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Account Number " + userId + " not found");

        // Then
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
     void willThrowWhenEmailAlreadyExistsWhenUpdatingUser() {
        // Given
        Long userId = 1L;
        User user = new User("Elaine", "Yang", "elaine@gmail.com", "password", "123 Main St");
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserUpdateRequest request = new UserUpdateRequest("ElaineNew", "YangNew", "elaine.new@gmail.com", "newpassword", "456 Elm St");

        User existingUser = new User("Other", "User", "elaine.new@gmail.com", "password", "789 Pine St");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(existingUser));

        // When
        assertThatThrownBy(() -> underTest.updateUser(userId, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email already in use");

        // Then
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void willThrowWhenNoChangesDetected2() {
        // Given
        Long userId = 1L;
        User user = new User("Elaine", "Yang", "elaine@gmail.com", "password", "123 Main St");
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserUpdateRequest request = new UserUpdateRequest(user.getFirstName(), user.getLastName(), user.getAddress(), user.getEmail(), user.getPassword());

        // When
        assertThatThrownBy(() -> underTest.updateUser(userId, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No changes were detected for the account.");

        // Then
        verify(userRepository, never()).save(any(User.class));
    }


    //4.delete a user
    @Test
    void canDeleteUser() {
        // Given
        Long userId = 1L;
        User user = new User("Elaine", "Yang", "elaine@gmail.com", "password", "123 Main St");
        user.setId(userId);

        Account account1 = new Account("12345", BigDecimal.valueOf(1000), user);
        account1.setId(1L);
        Account account2 = new Account("67890", BigDecimal.valueOf(2000), user);
        account2.setId(2L);
        user.setAccounts(List.of(account1, account2));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        underTest.deleteUser(userId);

        // Then
        ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository, times(2)).save(accountArgumentCaptor.capture());

        List<Account> savedAccounts = accountArgumentCaptor.getAllValues();
        assertThat(savedAccounts.get(0).getUser()).isNull();
        assertThat(savedAccounts.get(0).getStatus()).isEqualTo(AccountStatus.CLOSED);

        assertThat(savedAccounts.get(1).getUser()).isNull();
        assertThat(savedAccounts.get(1).getStatus()).isEqualTo(AccountStatus.CLOSED);

        verify(userRepository).delete(user);
    }

    @Test
    void willThrowWhenUserNotFoundWhenDeletingUser() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> underTest.deleteUser(userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found with id: " + userId);

        // Then
        verify(userRepository, never()).delete(any(User.class));
        verify(accountRepository, never()).save(any(Account.class));
    }
}