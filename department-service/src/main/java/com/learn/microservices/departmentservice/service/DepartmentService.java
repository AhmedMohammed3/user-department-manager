package com.learn.microservices.departmentservice.service;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.microservices.departmentservice.dto.DepartmentDto;
import com.learn.microservices.departmentservice.entity.Department;
import com.learn.microservices.departmentservice.exception.DepartmentNotFoundException;
import com.learn.microservices.departmentservice.exception.DepartmentAlreadyExistException;
import com.learn.microservices.departmentservice.repository.DepartmentRepository;
import com.learn.microservices.departmentservice.util.PaginationOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentService {
    private final DepartmentRepository departmentRepo;
    private final ObjectMapper objectMapper;

    public Page<DepartmentDto> getDepartmentsByFilter(PaginationOptions paginationOptions) {
        log.info("Fetching departments with pagination, page: {}, size: {}", paginationOptions.getPage(), paginationOptions.getSize());
        try {
            Page<Department> departments;

            Sort sort = Sort.unsorted();
            String sortBy = paginationOptions.getSortBy();
            String sortOrder = paginationOptions.getSortOrder();

            if (sortBy != null && !sortBy.isBlank()) {
                if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
                    sort = Sort.by(Sort.Order.desc(sortBy));
                } else {
                    sort = Sort.by(Sort.Order.asc(sortBy));
                }
            }

            Pageable sortedPageable = PageRequest.of(paginationOptions.getPage(), paginationOptions.getSize(), sort);

            log.info("Fetching all departments.");
            departments = departmentRepo.findAll(sortedPageable);

            Page<DepartmentDto> departmentsDtos = departments.map(DepartmentDto::new);
            log.info("Successfully fetched {} departments.", departmentsDtos.getTotalElements());
            return departmentsDtos;
        } catch (Exception e) {
            String msg = "Failed to fetch departments, " + e.getMessage();
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    public DepartmentDto getDepartmentById(Long id) {
        log.info("Fetching department by id: {}", id);
        Department department = getDepartmentIfExist(id);

        return new DepartmentDto(department);
    }

    public DepartmentDto getDepartmentByName(String name) {
        log.info("Fetching department by name: {}", name);

        Department department = departmentRepo.getByName(name).orElseThrow(() -> {
            String msg = "Department with name " + name + " not found.";
            log.warn(msg);
            return new DepartmentNotFoundException(msg);
        });

        return new DepartmentDto(department);
    }

    public DepartmentDto saveDepartment(DepartmentDto departmentDto) {
        if (checkDepartmentExistsByName(departmentDto.getName())) {
            String msg = "Department with name " + departmentDto.getName() + " already exists.";
            log.warn(msg);
            throw new DepartmentAlreadyExistException(msg);
        }

        log.info("Saving department with name: {}", departmentDto.getName());
        departmentDto.setId(null);
        Department department = new Department(departmentDto);

        department.setName(department.getName().toLowerCase());

        Department createdDepartment = departmentRepo.save(department);
        return new DepartmentDto(createdDepartment);
    }

    public void deleteDepartment(Long id) {
        log.info("Deleting department with id: {}", id);
        if (!departmentRepo.existsById(id)) {
            String msg = "Department with id " + id + " not found.";
            log.warn(msg);
            throw new DepartmentNotFoundException(msg);
        }
        departmentRepo.deleteById(id);
        log.info("Department with id {} deleted successfully.", id);
    }

    public DepartmentDto updateDepartment(Long id, DepartmentDto departmentDto) {
        Department existingDepartment = getDepartmentIfExist(id);

        log.info("Updating department with id: {}", id);
        if (!existingDepartment.getName().equalsIgnoreCase(departmentDto.getName())
                && checkDepartmentExistsByName(departmentDto.getName())) {
            String msg = "Department with name " + departmentDto.getName() + " already exists.";
            log.warn(msg);
            throw new DepartmentAlreadyExistException(msg);
        }

        try {
            departmentDto.setId(id);
            objectMapper.updateValue(existingDepartment, departmentDto);
        } catch (JsonMappingException e) {
            log.error("Error mapping department data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update department data", e);
        }

        existingDepartment.setName(existingDepartment.getName().toLowerCase());

        Department savedDepartment = departmentRepo.save(existingDepartment);
        log.info("Department with id {} updated successfully.", id);
        return new DepartmentDto(savedDepartment);
    }

    public boolean checkDepartmentExistsByName(String name) {
        return departmentRepo.getByName(name.toLowerCase()).isPresent();
    }

    private Department getDepartmentIfExist(Long id) {
        return departmentRepo.findById(id).orElseThrow(() -> {
            String msg = "Department with id " + id + " not found.";
            log.warn(msg);
            return new DepartmentNotFoundException(msg);
        });
    }

    public boolean isDepartmentExists(Long departmentId) {
        return departmentRepo.existsById(departmentId);
    }
}
