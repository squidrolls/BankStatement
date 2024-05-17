package com.example.elaine.payload;

import com.fasterxml.jackson.annotation.JsonInclude;


public record UserUpdateRequest(
        String firstName,
        String lastName,
        String address,
        String email,
        String password // todo: This will be optional and must be hashed if provided
) {
}
