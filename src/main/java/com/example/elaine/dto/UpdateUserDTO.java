package com.example.elaine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateUserDTO {
    private String firstName;
    private String lastName;
    private String address;
    private String email;
    private String password; // todo: This will be optional and must be hashed if provided
}
