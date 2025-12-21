package com.unicorn.backend.exception;

import lombok.Getter;

@Getter
public class UserNotVerifiedException extends RuntimeException {
    private final String email;

    public UserNotVerifiedException(String email) {
        super("User email not verified: " + email);
        this.email = email;
    }
}
