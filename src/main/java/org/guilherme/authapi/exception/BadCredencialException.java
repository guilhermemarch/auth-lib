package org.guilherme.authapi.exception;

public class BadCredencialException extends RuntimeException {
    public BadCredencialException(String message) {
        super(message);
    }
} 