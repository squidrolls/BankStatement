package com.example.elaine.service;

import com.example.elaine.dao.AccountRepository;
import com.example.elaine.dao.UserRepository;
import com.example.elaine.dto.AccountDTO;
import com.example.elaine.dto.CreateUserDTO;
import com.example.elaine.dto.UpdateUserDTO;
import com.example.elaine.dto.UserDTO;
import com.example.elaine.entity.Account;
import com.example.elaine.entity.AccountStatus;
import com.example.elaine.entity.BankUser;
import com.example.elaine.exception.NotFoundException;
import jakarta.validation.ValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    //1. get all user
    public List<UserDTO> getAllUsersWithAccounts() {
        List<BankUser> users = userRepository.findAllUsersWithAccounts();
        List<UserDTO> userDTOs = users.stream().map(this::convertToUserDTO).collect(Collectors.toList());

        log.info("Fetched {} users", userDTOs.size());

        return userDTOs;
    }

    private UserDTO convertToUserDTO(BankUser user) {
        List<AccountDTO> accountDTOs = user.getAccounts().stream()
                .map(this::convertToAccountDTO)
                .collect(Collectors.toList());
        return new UserDTO(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getAddress(), accountDTOs);
    }

    private AccountDTO convertToAccountDTO(Account account) {
        return new AccountDTO(account.getId(), account.getAccountNumber(), account.getBalance(), account.getStatus());
    }

    //2.create new user
    public UserDTO createUser(CreateUserDTO createUserDTO) {
        log.debug("Attempting to create a new user with email: {}", createUserDTO.getEmail());

        //Check if email is already in use
        userRepository.findByEmail(createUserDTO.getEmail()).ifPresent(existingUser -> {
            log.warn("Attempt to create a user with an existing email: {}", createUserDTO.getEmail());
            throw new ValidationException("Email already in use: " + createUserDTO.getEmail());
        });

        BankUser user = new BankUser(createUserDTO.getFirstName(), createUserDTO.getLastName(), createUserDTO.getEmail(), createUserDTO.getPassword(), createUserDTO.getAddress());
        user = userRepository.save(user);
        log.info("Created new user with ID: {}", user.getId());

        return convertToUserDTO(user);
    }

    //3. update a user
    @Transactional
    public UserDTO updateUser(Long userId, UpdateUserDTO updateUserDTO) {
        log.debug("Updating user with ID: {}", userId);
        BankUser user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Account Number " + userId + " not found"));

        boolean isUpdated = false;

        if (updateUserDTO.getEmail() != null && !updateUserDTO.getEmail().equals(user.getEmail())) {
            userRepository.findByEmail(updateUserDTO.getEmail()).ifPresent(existingUser -> {
                log.warn("Attempt to update user ID: {} with an existing email: {}", userId, updateUserDTO.getEmail());
                throw new ValidationException("Email already in use");
            });
            user.setEmail(updateUserDTO.getEmail());
            isUpdated = true;
        }

        if (updateUserDTO.getFirstName() != null && !updateUserDTO.getFirstName().equals(user.getFirstName())) {
            user.setFirstName(updateUserDTO.getFirstName());
            isUpdated = true;
        }

        if (updateUserDTO.getLastName() != null && !updateUserDTO.getLastName().equals(user.getLastName())) {
            user.setLastName(updateUserDTO.getLastName());
            isUpdated = true;
        }

        if (updateUserDTO.getAddress() != null && !updateUserDTO.getAddress().equals(user.getAddress())) {
            user.setAddress(updateUserDTO.getAddress());
            isUpdated = true;
        }

        // Check for password changes (ensure to encode the new password before comparison if encoded)
        if (updateUserDTO.getPassword() != null) { // todo :Add actual password comparison logic here if needed
            user.setPassword(updateUserDTO.getPassword()); //TODO: Add password encoder
            isUpdated = true;
        }

        if (isUpdated) {
            userRepository.save(user);
            log.info("Updated user with ID: {}", userId);

            return convertToUserDTO(user);
        } else {
            log.info("No changes detected for user with ID: {}", userId);

            throw new IllegalStateException("No changes were detected for the account.");
        }
    }

    //4. delete a user
    //todo: handle security and authorization
    @Transactional
    public void deleteUser(Long userId) {
        log.debug("Deleting user with ID: {}", userId);
        BankUser user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        // Set the user reference in each account to null and close the accounts
        for (Account account : user.getAccounts()) {
            account.setBankUser(null);// Disassociate the account from the user
            account.setStatus(AccountStatus.CLOSED);
            accountRepository.save(account);
        }
        userRepository.delete(user);

        log.info("Deleted user with ID: {} and closed all associated accounts", userId);
    }
}
