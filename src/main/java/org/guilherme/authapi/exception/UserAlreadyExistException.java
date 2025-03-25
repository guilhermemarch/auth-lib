package org.guilherme.authapi.exception;

public class UserAlreadyExistException extends RuntimeException {


    public UserAlreadyExistException(String message) {
        super(message);
    }

}
