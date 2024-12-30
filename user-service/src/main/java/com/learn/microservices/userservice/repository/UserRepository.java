package com.learn.microservices.userservice.repository;

import com.learn.microservices.userservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> getByEmail(String email);
    Page<User> findByRole(String role, Pageable pageable);
    Page<User> findByDepartmentId(Long departmentId, Pageable pageable);
    Page<User> findByRoleAndDepartmentId(String role, Long departmentId, Pageable pageable);
}