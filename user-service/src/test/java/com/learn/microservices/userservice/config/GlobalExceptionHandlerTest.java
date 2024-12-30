package com.learn.microservices.userservice.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.microservices.userservice.controller.UserController;
import com.learn.microservices.userservice.dto.UserDto;
import com.learn.microservices.userservice.dto.UserEmailDto;
import com.learn.microservices.userservice.exception.EmailAlreadyExistException;
import com.learn.microservices.userservice.exception.UserNotFoundException;
import com.learn.microservices.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    private UserEmailDto createUserEmailDto() {
        return new UserEmailDto(1L, "Ahmed", "Hassan", "test@mail.com", "1234567890", "USER", 1L, "2024-12-26", "2024-12-27", "password");
    }

    @Test
    public void testGetUsers_success() throws Exception {
        List<UserDto> userDtos = List.of(new UserDto(0L, "Ahmed", "Hassan", "test@mail.com", "1234567890", "ADMIN", 1L, "2024-12-26", "2024-12-27"),
                new UserDto(1L, "Ahmed", "Hassan2", "test2@mail.com", "1234567890", "USER", 1L, "2024-12-26", "2024-12-27"));
        Page<UserDto> userPage = new PageImpl<>(userDtos);
        when(userService.getUsersByFilter(any(), any(), any())).thenReturn(userPage);

        mockMvc.perform(get("/users/")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "firstName")
                        .param("sortOrder", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    public void testGetUserByEmail_success() throws Exception {
        UserEmailDto userEmailDto = createUserEmailDto();
        when(userService.getUserByEmail(any())).thenReturn(userEmailDto);

        mockMvc.perform(get("/users/email/{email}", "test@mail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@mail.com"))
                .andExpect(jsonPath("$.firstName").value("Ahmed"));
    }

    @Test
    public void testGetUserById_success() throws Exception {
        UserDto userDto = new UserDto(1L, "Ahmed", "Hassan2", "test2@mail.com", "1234567890", "USER", 1L, "2024-12-26", "2024-12-27");
        when(userService.getUserById(any())).thenReturn(userDto);

        mockMvc.perform(get("/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ahmed"))
                .andExpect(jsonPath("$.lastName").value("Hassan2"));
    }

    @Test
    public void testSaveUser_success() throws Exception {
        UserEmailDto userEmailDto = createUserEmailDto();
        when(userService.saveUser(any())).thenReturn(userEmailDto);

        mockMvc.perform(post("/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userEmailDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Ahmed"))
                .andExpect(jsonPath("$.email").value("test@mail.com"));
    }

    @Test
    public void testUpdateUser_success() throws Exception {
        UserEmailDto userEmailDto = createUserEmailDto();
        when(userService.updateUser(any(), any())).thenReturn(userEmailDto);

        mockMvc.perform(put("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userEmailDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ahmed"))
                .andExpect(jsonPath("$.email").value("test@mail.com"));
    }

    @Test
    public void testDeleteUser_success() throws Exception {
        mockMvc.perform(delete("/users/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testCheckUserExistsByEmail_success() throws Exception {
        when(userService.checkUserExistsByEmail(any())).thenReturn(true);

        mockMvc.perform(get("/users/exists/email/{email}", "test@mail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    public void testGetUsers_invalidPagination() throws Exception {
        mockMvc.perform(get("/users/")
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testSaveUser_invalidEmail() throws Exception {
        UserEmailDto userEmailDto = createUserEmailDto();
        userEmailDto.setEmail("");  // Invalid email

        mockMvc.perform(post("/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userEmailDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testSaveUser_emailAlreadyExists() throws Exception {
        UserEmailDto userEmailDto = createUserEmailDto();
        when(userService.saveUser(any())).thenThrow(EmailAlreadyExistException.class);

        mockMvc.perform(post("/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userEmailDto)))
                .andExpect(status().isConflict());
    }

    @Test
    public void testDeleteUser_userNotFound() throws Exception {
        Mockito.doThrow(UserNotFoundException.class).when(userService).deleteUser(any());

        mockMvc.perform(delete("/users/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUnhandledException() throws Exception {
        mockMvc.perform(get("/rrss", 1L))
                .andExpect(status().isInternalServerError());
    }
}
