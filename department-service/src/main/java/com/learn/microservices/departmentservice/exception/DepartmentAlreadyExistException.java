package com.learn.microservices.departmentservice.exception;

public class DepartmentAlreadyExistException extends NotFoundException {
    public DepartmentAlreadyExistException(String message) {
        super(message);
    }
}
