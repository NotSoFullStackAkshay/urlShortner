package com.example.urlshortener.exception;

public class AliasAlreadyExistsException extends RuntimeException {
    public AliasAlreadyExistsException(String code) {
        super("Alias already in use: " + code);
    }
}
