package com.learn.microservices.userservice.exception;

public class DepartmentNotFoundException extends NotFoundException {

    public DepartmentNotFoundException(String message) {
        super(message);
    }
}
