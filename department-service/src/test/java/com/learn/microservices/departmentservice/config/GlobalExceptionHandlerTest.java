package com.learn.microservices.departmentservice.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.microservices.departmentservice.controller.DepartmentController;
import com.learn.microservices.departmentservice.dto.DepartmentDto;
import com.learn.microservices.departmentservice.exception.DepartmentAlreadyExistException;
import com.learn.microservices.departmentservice.exception.DepartmentNotFoundException;
import com.learn.microservices.departmentservice.service.DepartmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DepartmentController.class)
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepartmentService departmentService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    private DepartmentDto createDepartmentDto() {
        return new DepartmentDto(1L, "Software Department", "Department of software and technology", "2024-12-26", "2024-12-27");
    }

    @Test
    public void testGetDepartments_success() throws Exception {
        List<DepartmentDto> departmentDtos = List.of(createDepartmentDto());
        Page<DepartmentDto> departmentDtoPage = new PageImpl<>(departmentDtos);
        when(departmentService.getDepartmentsByFilter(any())).thenReturn(departmentDtoPage);

        mockMvc.perform(get("/departments/")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "firstName")
                        .param("sortOrder", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    public void testGetDepartmentByName_success() throws Exception {
        DepartmentDto departmentDto = createDepartmentDto();
        when(departmentService.getDepartmentByName(any())).thenReturn(departmentDto);

        mockMvc.perform(get("/departments/name/{name}", "Software%20Department"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Software Department"))
                .andExpect(jsonPath("$.description").value("Department of software and technology"));
    }

    @Test
    public void testGetDepartmentById_success() throws Exception {
        DepartmentDto departmentDto = createDepartmentDto();
        when(departmentService.getDepartmentById(any())).thenReturn(departmentDto);

        mockMvc.perform(get("/departments/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Software Department"))
                .andExpect(jsonPath("$.description").value("Department of software and technology"));
    }

    @Test
    public void testSaveDepartment_success() throws Exception {
        DepartmentDto departmentDto = createDepartmentDto();
        when(departmentService.saveDepartment(any())).thenReturn(departmentDto);

        mockMvc.perform(post("/departments/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(departmentDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Software Department"))
                .andExpect(jsonPath("$.description").value("Department of software and technology"));
    }

    @Test
    public void testUpdateDepartment_success() throws Exception {
        DepartmentDto departmentDto = createDepartmentDto();
        when(departmentService.updateDepartment(any(), any())).thenReturn(departmentDto);

        mockMvc.perform(put("/departments/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(departmentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Software Department"))
                .andExpect(jsonPath("$.description").value("Department of software and technology"));
    }

    @Test
    public void testDeleteDepartment_success() throws Exception {
        mockMvc.perform(delete("/departments/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testCheckDepartmentExistsByName_success() throws Exception {
        when(departmentService.checkDepartmentExistsByName(any())).thenReturn(true);

        mockMvc.perform(get("/departments/exists/name/{name}", "Software Department"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    public void testGetDepartments_invalidPagination() throws Exception {
        mockMvc.perform(get("/departments/")
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testSaveDepartment_invalidName() throws Exception {
        DepartmentDto departmentDto = createDepartmentDto();
        departmentDto.setName("");  // Invalid name

        mockMvc.perform(post("/departments/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(departmentDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testSaveDepartment_DepartmentAlreadyExists() throws Exception {
        DepartmentDto departmentDto = createDepartmentDto();
        when(departmentService.saveDepartment(any())).thenThrow(DepartmentAlreadyExistException.class);

        mockMvc.perform(post("/departments/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(departmentDto)))
                .andExpect(status().isConflict());
    }

    @Test
    public void testDeleteDepartment_DepartmentNotFound() throws Exception {
        doThrow(DepartmentNotFoundException.class).when(departmentService).deleteDepartment(any());

        mockMvc.perform(delete("/departments/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUnhandledException() throws Exception {
        mockMvc.perform(get("/rrss", 1L))
                .andExpect(status().isInternalServerError());
    }
}
