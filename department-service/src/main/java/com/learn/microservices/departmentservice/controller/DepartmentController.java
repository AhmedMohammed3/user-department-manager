package com.learn.microservices.departmentservice.controller;

import com.learn.microservices.departmentservice.dto.DepartmentDto;
import com.learn.microservices.departmentservice.exception.InvalidPaginationException;
import com.learn.microservices.departmentservice.service.DepartmentService;
import com.learn.microservices.departmentservice.util.PaginationOptions;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
@Slf4j
public class DepartmentController {

    private final DepartmentService departmentservice;

    @GetMapping("/")
    public ResponseEntity<Page<DepartmentDto>> getDepartments(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "sortOrder", required = false) String sortOrder) {

        PaginationOptions paginationOptions = new PaginationOptions(page, size, sortBy, sortOrder);

        if (paginationOptions.getPage() < 0 || paginationOptions.getSize() <= 0) {
            log.warn("Invalid pagination parameters: page={}, size={}", paginationOptions.getPage(), paginationOptions.getSize());
            throw new InvalidPaginationException("Page number cannot be negative and size must be greater than 0.");
        }
        log.info("Fetching departments with pagination: page={}, size={}", paginationOptions.getPage(), paginationOptions.getSize());

        Page<DepartmentDto> departmentsPage = departmentservice.getDepartmentsByFilter(paginationOptions);

        log.info("Successfully fetched {} departments.", departmentsPage.getTotalElements());
        return ResponseEntity.ok(departmentsPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDto> getDepartmentById(@PathVariable Long id) {
        log.info("Fetching department by id: id={}", id);
        DepartmentDto department = departmentservice.getDepartmentById(id);
        log.info("Successfully fetched department by id: {}", id);
        return ResponseEntity.ok(department);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<DepartmentDto> getDepartmentByName(@PathVariable String name) {
        log.info("Fetching department by name: name={}", name);
        DepartmentDto department = departmentservice.getDepartmentByName(name);
        log.info("Successfully fetched department by name: {}", name);
        return ResponseEntity.ok(department);
    }

    @PostMapping("/")
    public ResponseEntity<DepartmentDto> saveDepartment(@Valid @RequestBody DepartmentDto departmentDto) {
        log.info("Saving department: {}", departmentDto);
        DepartmentDto savedDepartment = departmentservice.saveDepartment(departmentDto);
        log.info("Successfully saved department: {}", savedDepartment);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDepartment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentDto> updateDepartment(@PathVariable Long id, @Valid @RequestBody DepartmentDto departmentDto) {
        log.info("Updating department with id: {}, {}", id, departmentDto);
        DepartmentDto updatedDepartment = departmentservice.updateDepartment(id, departmentDto);
        log.info("Successfully updated department: {}", updatedDepartment);
        return ResponseEntity.ok(updatedDepartment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        log.info("Deleting department with id: {}", id);
        departmentservice.deleteDepartment(id);
        log.info("Successfully deleted department with id: {}", id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/exists/{departmentId}")
    public boolean isDepartmentExists(@PathVariable Long departmentId) {
        return departmentservice.isDepartmentExists(departmentId);
    }

    @GetMapping("/exists/name/{name}")
    public ResponseEntity<Boolean> checkDepartmentExistsByName(@PathVariable String name) {
        log.info("Checking if department exists by name: {}", name);
        boolean exists = departmentservice.checkDepartmentExistsByName(name);
        if (exists) {
            log.info("Department exists by name: {}", name);
            return ResponseEntity.ok(exists);
        }
        log.info("Department does not exist by name: {}", name);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exists);
    }

}
