package com.myapp.backend.domain.exception;

public class UserNameAlreadyExistsException extends RuntimeException {
    public UserNameAlreadyExistsException(String userName) {
        super("Username already taken: " + userName);
    }
}
