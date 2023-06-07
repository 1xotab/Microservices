package com.example.customer;

public record CustomerRegistrationRequest(
        String firstname,
        String lastname,
        String email
) {
}
