package com.ariv.photoshare.common.exception;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String username) {
        super("Username already exists: " + username);
    }
}