package com.learn.microservices.userservice.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "DEPARTMENT-SERVICE",
        path = "/departments",
        fallbackFactory = DepartmentServiceFallback.class)
public interface DepartmentServiceProxy {
    @GetMapping("/exists/{departmentId}")
    boolean isDepartmentExists(@PathVariable Long departmentId);
}
