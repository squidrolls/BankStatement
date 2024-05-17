package com.example.elaine.payload;

public record UserRegistrationRequest(
        String firstName,
        String lastName,
        String email,
        String password,  // todo: This should be hashed before storage.
        String address
) {

}
