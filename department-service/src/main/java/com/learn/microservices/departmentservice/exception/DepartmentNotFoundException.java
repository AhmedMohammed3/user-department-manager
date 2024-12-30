package com.learn.microservices.departmentservice.exception;

public class DepartmentNotFoundException extends NotFoundException {

    public DepartmentNotFoundException(String message) {
        super(message);
    }
}
