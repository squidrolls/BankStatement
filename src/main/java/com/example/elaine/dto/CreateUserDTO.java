package com.example.elaine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String password;  // Note: This should be hashed before storage.
    private String address;
}
