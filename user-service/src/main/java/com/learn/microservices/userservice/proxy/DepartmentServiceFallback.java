package com.learn.microservices.userservice.proxy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

@Slf4j
public class DepartmentServiceFallback implements FallbackFactory<DepartmentServiceProxy> {

    @Override
    public DepartmentServiceProxy create(Throwable cause) {
        return departmentId -> {
            log.warn("Failed to check if department with id {} exists: {}", departmentId, cause.getMessage());
            return false;
        };
    }
}
