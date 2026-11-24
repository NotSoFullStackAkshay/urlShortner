package com.example.urlshortener.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class CodeGenerator {

    private static final char[] ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    private final SecureRandom random = new SecureRandom();
    private final int length;

    public CodeGenerator(@Value("${app.code-length:7}") int length) {
        this.length = length;
    }

    public String generate() {
        char[] out = new char[length];
        for (int i = 0; i < length; i++) {
            out[i] = ALPHABET[random.nextInt(ALPHABET.length)];
        }
        return new String(out);
    }
}
