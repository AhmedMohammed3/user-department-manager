package com.learn.microservices.departmentservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.microservices.departmentservice.dto.DepartmentDto;
import com.learn.microservices.departmentservice.entity.Department;
import com.learn.microservices.departmentservice.exception.DepartmentAlreadyExistException;
import com.learn.microservices.departmentservice.exception.DepartmentNotFoundException;
import com.learn.microservices.departmentservice.repository.DepartmentRepository;
import com.learn.microservices.departmentservice.util.PaginationOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DepartmentServiceTest {

    private DepartmentService departmentService;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        departmentService = new DepartmentService(departmentRepository, objectMapper);
    }

    @Test
    void testGetDepartmentsByFilter_noFilters() {
        PaginationOptions paginationOptions = new PaginationOptions(0, 10, "firstName", "asc");
        Pageable pageable = PageRequest.of(paginationOptions.getPage(), paginationOptions.getSize(), Sort.by(Sort.Order.asc(paginationOptions.getSortBy())));

        Page<Department> departmentPage = new PageImpl<>(List.of(new Department()));
        when(departmentRepository.findAll(pageable)).thenReturn(departmentPage);

        Page<DepartmentDto> result = departmentService.getDepartmentsByFilter(paginationOptions);

        assertNotNull(result);
        verify(departmentRepository, times(1)).findAll(pageable);
    }

    @Test
    void testGetDepartmentsByFilter_noFilters_emptySortBy() {
        PaginationOptions paginationOptions = new PaginationOptions(0, 10, " ", "asc");
        Pageable pageable = PageRequest.of(paginationOptions.getPage(), paginationOptions.getSize());

        Page<Department> departmentPage = new PageImpl<>(List.of(new Department()));
        when(departmentRepository.findAll(pageable)).thenReturn(departmentPage);

        Page<DepartmentDto> result = departmentService.getDepartmentsByFilter(paginationOptions);

        assertNotNull(result);
        verify(departmentRepository, times(1)).findAll(pageable);
    }

    @Test
    void testGetDepartmentsByFilter_sortAscending() {
        PaginationOptions paginationOptions = new PaginationOptions(0, 10, "name", "asc");

        Page<Department> departmentPage = new PageImpl<>(List.of(new Department()));
        when(departmentRepository.findAll(PageRequest.of(0, 10, Sort.by(Sort.Order.asc("name"))))).thenReturn(departmentPage);

        Page<DepartmentDto> result = departmentService.getDepartmentsByFilter(paginationOptions);

        assertNotNull(result);
        verify(departmentRepository, times(1)).findAll(PageRequest.of(0, 10, Sort.by(Sort.Order.asc("name"))));
    }

    @Test
    void testGetDepartmentsByFilter_sortDescending() {
        PaginationOptions paginationOptions = new PaginationOptions(0, 10, "name", "desc");

        Page<Department> departmentPage = new PageImpl<>(List.of(new Department()));
        when(departmentRepository.findAll(PageRequest.of(0, 10, Sort.by(Sort.Order.desc("name"))))).thenReturn(departmentPage);

        Page<DepartmentDto> result = departmentService.getDepartmentsByFilter(paginationOptions);

        assertNotNull(result);
        verify(departmentRepository, times(1)).findAll(PageRequest.of(0, 10, Sort.by(Sort.Order.desc("name"))));
    }

    @Test
    void testGetDepartmentsByFilter_exception() {
        PaginationOptions paginationOptions = new PaginationOptions(0, 10, "name", "asc");

        when(departmentRepository.findAll(Pageable.unpaged())).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> departmentService.getDepartmentsByFilter(paginationOptions));
    }

    @Test
    void testGetDepartmentsByFilter_invalidRole() {
        PaginationOptions paginationOptions = new PaginationOptions(0, 10, "name", "asc");
        Pageable pageable = PageRequest.of(paginationOptions.getPage(), paginationOptions.getSize(), Sort.by(Sort.Order.asc(paginationOptions.getSortBy())));

        Page<Department> departmentPage = new PageImpl<>(List.of(new Department()));
        when(departmentRepository.findAll(pageable)).thenReturn(departmentPage);

        Page<DepartmentDto> result = departmentService.getDepartmentsByFilter(paginationOptions);

        assertNotNull(result);
        verify(departmentRepository, times(1)).findAll(pageable);
    }

    @Test
    void testGetDepartmentsByFilter_invalidPagination() {
        PaginationOptions paginationOptions = new PaginationOptions(0, 10, "name", "asc");
        Pageable pageable = PageRequest.of(paginationOptions.getPage(), paginationOptions.getSize(), Sort.by(Sort.Order.asc(paginationOptions.getSortBy())));

        Page<Department> departmentPage = new PageImpl<>(List.of(new Department()));
        when(departmentRepository.findAll(pageable)).thenReturn(departmentPage);

        Page<DepartmentDto> result = departmentService.getDepartmentsByFilter(paginationOptions);

        assertNotNull(result);
        verify(departmentRepository, times(1)).findAll(pageable);
    }

    @Test
    void getDepartmentsById_ShouldReturnDepartmentsDto() {
        Long departmentId = 1L;
        Department department = new Department();
        department.setId(departmentId);
        department.setName("Software Department");

        DepartmentDto departmentDto = new DepartmentDto();
        departmentDto.setName("Software Department");

        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
        when(objectMapper.convertValue(department, DepartmentDto.class)).thenReturn(departmentDto);

        DepartmentDto result = departmentService.getDepartmentById(departmentId);

        assertEquals("Software Department", result.getName());
        verify(departmentRepository, times(1)).findById(departmentId);
    }

    @Test
    void getDepartmentById_ShouldThrowDepartmentNotFoundException_WhenDepartmentNotFound() {
        Long departmentId = 1L;
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.empty());

        assertThrows(DepartmentNotFoundException.class, () -> departmentService.getDepartmentById(departmentId));
    }

    @Test
    void getDepartmentByName_ShouldReturnDepartmentDto() {
        String name = "Software Department";
        Department department = new Department();
        department.setName(name);

        DepartmentDto departmentDto = new DepartmentDto();
        departmentDto.setName(name);

        when(departmentRepository.getByName(name)).thenReturn(Optional.of(department));
        when(objectMapper.convertValue(department, DepartmentDto.class)).thenReturn(departmentDto);

        DepartmentDto result = departmentService.getDepartmentByName(name);

        assertEquals(name, result.getName());
        verify(departmentRepository, times(1)).getByName(name);
    }

    @Test
    void getDepartmentByName_ShouldThrowDepartmentNotFoundException_WhenDepartmentNotFound() {
        String name = "Software Department";
        when(departmentRepository.getByName(name)).thenReturn(Optional.empty());

        assertThrows(DepartmentNotFoundException.class, () -> departmentService.getDepartmentByName(name));
    }

    @Test
    void saveDepartment_ShouldSaveAndReturnDepartmentDto() {
        DepartmentDto departmentDto = new DepartmentDto();
        departmentDto.setName("Software Department");
        departmentDto.setDescription("Department of software and technology");

        Department department = new Department();
        department.setName(departmentDto.getName());
        department.setDescription(departmentDto.getDescription());

        when(objectMapper.convertValue(departmentDto, Department.class)).thenReturn(department);
        when(departmentRepository.save(any(Department.class))).thenReturn(department);
        when(objectMapper.convertValue(department, DepartmentDto.class)).thenReturn(departmentDto);


        DepartmentDto result = departmentService.saveDepartment(departmentDto);


        assertEquals(departmentDto.getName(), result.getName());
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    void saveDepartment_ShouldThrowDepartmentAlreadyExistException_WhenDepartmentExists() {
        DepartmentDto departmentDto = new DepartmentDto();
        departmentDto.setName("Software Department");

        Department existingDepartment = new Department();
        existingDepartment.setName(departmentDto.getName());

        when(departmentRepository.getByName(departmentDto.getName())).thenReturn(Optional.of(existingDepartment));

        assertThrows(DepartmentAlreadyExistException.class, () -> departmentService.saveDepartment(departmentDto));
    }

    @Test
    void deleteDepartment_ShouldDeleteDepartment() {
        Long departmentId = 1L;
        when(departmentRepository.existsById(departmentId)).thenReturn(true);

        departmentService.deleteDepartment(departmentId);

        verify(departmentRepository, times(1)).deleteById(departmentId);
    }

    @Test
    void deleteDepartment_ShouldThrowDepartmentNotFoundException_WhenDepartmentDoesNotExist() {
        Long departmentId = 1L;
        when(departmentRepository.existsById(departmentId)).thenReturn(false);

        assertThrows(DepartmentNotFoundException.class, () -> departmentService.deleteDepartment(departmentId));
    }

    @Test
    void updateDepartment_ShouldUpdateAndReturnDepartmentDto() {
        Long departmentId = 1L;
        DepartmentDto departmentDto = new DepartmentDto();
        departmentDto.setName("Updated Department Name");

        Department existingDepartment = new Department();
        existingDepartment.setId(departmentId);
        existingDepartment.setName("Old Department Name");

        Department updatedDepartment = new Department();
        updatedDepartment.setId(departmentId);
        updatedDepartment.setName(departmentDto.getName());

        DepartmentDto updatedDepartmentDto = new DepartmentDto();
        updatedDepartmentDto.setName(departmentDto.getName());

        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(existingDepartment));
        when(departmentRepository.save(any(Department.class))).thenReturn(updatedDepartment);
        when(objectMapper.convertValue(updatedDepartment, DepartmentDto.class)).thenReturn(updatedDepartmentDto);

        DepartmentDto result = departmentService.updateDepartment(departmentId, departmentDto);

        assertEquals(departmentDto.getName(), result.getName());
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    void updateDepartment_ShouldThrowDepartmentAlreadyExistException_WhenNameAlreadyExists() {
        Long departmentId = 1L;
        DepartmentDto departmentDto = new DepartmentDto();
        departmentDto.setName("Existing Department Name");

        Department existingDepartment = new Department();
        existingDepartment.setId(departmentId);
        existingDepartment.setName("New Department Name");

        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(existingDepartment));
        when(departmentRepository.getByName(departmentDto.getName())).thenReturn(Optional.of(existingDepartment));

        assertThrows(DepartmentAlreadyExistException.class, () -> departmentService.updateDepartment(departmentId, departmentDto));
    }

}
