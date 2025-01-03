package com.learn.microservices.departmentservice.controller;

import com.learn.microservices.departmentservice.dto.DepartmentDto;
import com.learn.microservices.departmentservice.exception.DepartmentAlreadyExistException;
import com.learn.microservices.departmentservice.exception.DepartmentNotFoundException;
import com.learn.microservices.departmentservice.exception.InvalidPaginationException;
import com.learn.microservices.departmentservice.service.DepartmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentControllerTest {
    @InjectMocks
    private DepartmentController departmentController;

    @Mock
    private DepartmentService departmentService;

    private DepartmentDto createDepartmentDto() {
        return new DepartmentDto(1L, "Software Department", "Department of software and technology", "2024-12-26", "2024-12-27");
    }

    @Test
    public void testGetDepartments_success() {
        Page<DepartmentDto> departmentDtoPage = new PageImpl<>(List.of(createDepartmentDto()));
        when(departmentService.getDepartmentsByFilter(any())).thenReturn(departmentDtoPage);

        ResponseEntity<Page<DepartmentDto>> response = departmentController.getDepartments(0, 10, "name", "asc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    public void testGetDepartmentByName_success() {
        DepartmentDto departmentDto = createDepartmentDto();
        when(departmentService.getDepartmentByName(any())).thenReturn(departmentDto);
        ResponseEntity<DepartmentDto> response = departmentController.getDepartmentByName("Software Department");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testGetDepartmentById_success() {
        DepartmentDto departmentDto = createDepartmentDto();
        when(departmentService.getDepartmentById(any())).thenReturn(departmentDto);

        ResponseEntity<DepartmentDto> response = departmentController.getDepartmentById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testSaveDepartment_success() {
        DepartmentDto departmentDto = createDepartmentDto();
        when(departmentService.saveDepartment(any())).thenReturn(departmentDto);

        ResponseEntity<DepartmentDto> response = departmentController.saveDepartment(departmentDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testUpdateDepartment_success() {
        DepartmentDto departmentDto = createDepartmentDto();
        when(departmentService.updateDepartment(any(), any())).thenReturn(departmentDto);

        ResponseEntity<DepartmentDto> response = departmentController.updateDepartment(1L, departmentDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testDeleteDepartment_success() {
        ResponseEntity<Void> response = departmentController.deleteDepartment(1L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void testCheckDepartmentExistsByName_success() {
        when(departmentService.checkDepartmentExistsByName(any())).thenReturn(true);
        ResponseEntity<Boolean> response = departmentController.checkDepartmentExistsByName("Software Department");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @ParameterizedTest
    @CsvSource({
            "-1, 10",
            "0, -1"
    })
    public void testGetDepartments_invalidPagination(int page, int size) {
        assertThrows(InvalidPaginationException.class,
                () -> departmentController.getDepartments(page, size, "name", "asc"));
    }

    @Test
    public void testSaveDepartment_emailAlreadyExists() {
        DepartmentDto departmentDto = createDepartmentDto();
        when(departmentService.saveDepartment(any())).thenThrow(DepartmentAlreadyExistException.class);
        assertThrows(DepartmentAlreadyExistException.class,
                () -> departmentController.saveDepartment(departmentDto));
    }

    @Test
    public void testSaveDepartment_departmentNotFound() {
        DepartmentDto departmentDto = createDepartmentDto();
        when(departmentService.saveDepartment(any())).thenThrow(DepartmentNotFoundException.class);
        assertThrows(DepartmentNotFoundException.class,
                () -> departmentController.saveDepartment(departmentDto));
    }

    @Test
    public void testUpdateDepartment_emailAlreadyExists() {
        DepartmentDto departmentDto = createDepartmentDto();
        when(departmentService.updateDepartment(any(), any())).thenThrow(DepartmentAlreadyExistException.class);
        assertThrows(DepartmentAlreadyExistException.class,
                () -> departmentController.updateDepartment(1L, departmentDto));
    }

    @Test
    public void testUpdateDepartment_departmentNotFound() {
        DepartmentDto departmentDto = createDepartmentDto();
        when(departmentService.updateDepartment(any(), any())).thenThrow(DepartmentNotFoundException.class);
        assertThrows(DepartmentNotFoundException.class,
                () -> departmentController.updateDepartment(1L, departmentDto));
    }

    @Test
    public void testGetDepartmentByName_departmentNotFound() {
        when(departmentService.getDepartmentByName(any())).thenThrow(DepartmentNotFoundException.class);
        assertThrows(DepartmentNotFoundException.class,
                () -> departmentController.getDepartmentByName("Software Department"));
    }

    @Test
    public void testCheckDepartmentExistsByName_departmentNotFound() {
        when(departmentService.checkDepartmentExistsByName(any())).thenReturn(false);
        ResponseEntity<Boolean> response = departmentController.checkDepartmentExistsByName("Software Department");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

}