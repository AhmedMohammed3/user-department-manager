package com.learn.microservices.departmentservice.repository;

import com.learn.microservices.departmentservice.entity.Department;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> getByName(@Size(max = 100) @NotNull String name);
}