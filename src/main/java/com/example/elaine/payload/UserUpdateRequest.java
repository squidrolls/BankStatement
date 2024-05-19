package com.example.elaine.payload;


public record UserUpdateRequest(
        String firstName,
        String lastName,
        String address,
        String email,
        String password // todo: This will be optional and must be hashed if provided
) {
}
