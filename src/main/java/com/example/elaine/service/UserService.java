package com.example.elaine.service;

import com.example.elaine.dao.AccountRepository;
import com.example.elaine.dao.UserRepository;
import com.example.elaine.exception.DuplicateResourceException;
import com.example.elaine.payload.AccountDTO;
import com.example.elaine.payload.UserRegistrationRequest;
import com.example.elaine.payload.UserUpdateRequest;
import com.example.elaine.payload.UserDTO;
import com.example.elaine.entity.Account;
import com.example.elaine.entity.AccountStatus;
import com.example.elaine.entity.User;
import com.example.elaine.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    //1. get all user
    public List<UserDTO> getAllUsersWithAccounts() {
        List<User> users = userRepository.findAllUsersWithAccounts();
        List<UserDTO> userDTOs = users.stream().map(this::convertToUserDTO).collect(Collectors.toList());

        log.info("Fetched {} users", userDTOs.size());

        return userDTOs;
    }

    private UserDTO convertToUserDTO(User user) {
        List<AccountDTO> accountDTOs = user.getAccounts().stream()
                .map(this::convertToAccountDTO)
                .collect(Collectors.toList());
        return new UserDTO(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getAddress(), accountDTOs);
    }

    private AccountDTO convertToAccountDTO(Account account) {
        return new AccountDTO(account.getId(), account.getAccountNumber(), account.getBalance(), account.getStatus());
    }

    // Get user by ID
    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        return convertToUserDTO(user);
    }

    //2.create new user
    public UserDTO createUser(UserRegistrationRequest request) {
        log.debug("Attempting to create a new user with email: {}", request.email());

        //Check if email is already in use
        userRepository.findByEmail(request.email()).ifPresent(existingUser -> {
            log.warn("Attempt to create a user with an existing email: {}", request.email());
            throw new DuplicateResourceException("Email already in use: " + request.email());
        });

        User user = new User(request.firstName(), request.lastName(), request.email(), request.password(), request.address());
        user = userRepository.save(user);
        log.info("Created new user with ID: {}", user.getId());

        return convertToUserDTO(user);
    }

    //3. update a user
    @Transactional
    public void updateUser(Long userId, UserUpdateRequest userUpdateRequest) {
        log.debug("Updating user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Account Number " + userId + " not found"));

        boolean isUpdated = false;

        if (userUpdateRequest.email() != null && !userUpdateRequest.email().equals(user.getEmail())) {
            userRepository.findByEmail(userUpdateRequest.email()).ifPresent(existingUser -> {
                log.warn("Attempt to update user ID: {} with an existing email: {}", userId, userUpdateRequest.email());
                throw new DuplicateResourceException("Email already in use");
            });
            user.setEmail(userUpdateRequest.email());
            isUpdated = true;
        }

        if (userUpdateRequest.firstName() != null && !userUpdateRequest.firstName().equals(user.getFirstName())) {
            user.setFirstName(userUpdateRequest.firstName());
            isUpdated = true;
        }

        if (userUpdateRequest.lastName() != null && !userUpdateRequest.lastName().equals(user.getLastName())) {
            user.setLastName(userUpdateRequest.lastName());
            isUpdated = true;
        }

        if (userUpdateRequest.address() != null && !userUpdateRequest.address().equals(user.getAddress())) {
            user.setAddress(userUpdateRequest.address());
            isUpdated = true;
        }

        // Check for password changes (ensure to encode the new password before comparison if encoded)
        if (userUpdateRequest.password() != null && !userUpdateRequest.password().equals(user.getPassword())) { // todo :Add actual password comparison logic here if needed
            user.setPassword(userUpdateRequest.password()); //TODO: Add password encoder
            isUpdated = true;
        }

        if (isUpdated) {
            userRepository.save(user);
            log.info("Updated user with ID: {}", userId);
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        // Set the user reference in each account to null and close the accounts
        for (Account account : user.getAccounts()) {
            account.setUser(null);// Disassociate the account from the user
            account.setStatus(AccountStatus.CLOSED);
            accountRepository.save(account);
        }
        userRepository.delete(user);

        log.info("Deleted user with ID: {} and closed all associated accounts", userId);
    }
}
