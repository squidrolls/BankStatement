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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public UserService(UserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    //1. get all user
    public List<UserDTO> getAllUsersWithAccounts() {
        List<BankUser> users = userRepository.findAllUsersWithAccounts();
        return users.stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
    }

    private UserDTO convertToUserDTO(BankUser user) {
        List<AccountDTO> accountDTOs = user.getAccounts().stream()
                .map(this::convertToAccountDTO)
                .collect(Collectors.toList());
        return new UserDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getAddress(),
                accountDTOs);
    }

    private AccountDTO convertToAccountDTO(Account account) {
        return new AccountDTO(
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getStatus());
    }

    //2.create new user
    public UserDTO createUser(CreateUserDTO createUserDTO) {
        // Check if email is already in use
        userRepository.findByEmail(createUserDTO.getEmail()).ifPresent(existingUser -> {
            throw new ValidationException("Email already in use: " + createUserDTO.getEmail());
        });

        BankUser user = new BankUser();
        user.setFirstName(createUserDTO.getFirstName());
        user.setLastName(createUserDTO.getLastName());
        user.setEmail(createUserDTO.getEmail());
        user.setPassword(createUserDTO.getPassword());
        user.setAddress(createUserDTO.getAddress());

        user = userRepository.save(user);
        return convertToUserDTO(user);
    }

    //3. update a user
    @Transactional
    public UserDTO updateUser(Long userId, UpdateUserDTO updateUserDTO) {
        BankUser user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Account Number " + userId + " not found"));

        boolean isUpdated = false;

        if (updateUserDTO.getEmail() != null && !updateUserDTO.getEmail().equals(user.getEmail())) {
            userRepository.findByEmail(updateUserDTO.getEmail()).ifPresent(existingUser -> {
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
            return convertToUserDTO(user);
        } else {
            throw new IllegalStateException("No changes were detected for the account.");
        }
    }

    //4. delete a user
    //todo: handle security and authorization
    @Transactional
    public void deleteUser(Long userId) {
        BankUser user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        // Set the user reference in each account to null and close the accounts
        for (Account account : user.getAccounts()) {
            account.setUser(null); // Disassociate the account from the user
            account.setStatus(AccountStatus.CLOSED); // Optionally close the account
            accountRepository.save(account);
        }
        userRepository.delete(user);
    }


}
