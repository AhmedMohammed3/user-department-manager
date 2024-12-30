package com.learn.microservices.userservice.exception;

public class EmailAlreadyExistException extends NotFoundException {
    public EmailAlreadyExistException(String message) {
        super(message);
    }
}
