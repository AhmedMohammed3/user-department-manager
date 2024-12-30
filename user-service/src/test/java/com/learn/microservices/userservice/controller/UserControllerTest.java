package com.learn.microservices.userservice.controller;

import com.learn.microservices.userservice.dto.UserDto;
import com.learn.microservices.userservice.dto.UserEmailDto;
import com.learn.microservices.userservice.exception.DepartmentNotFoundException;
import com.learn.microservices.userservice.exception.EmailAlreadyExistException;
import com.learn.microservices.userservice.exception.InvalidPaginationException;
import com.learn.microservices.userservice.exception.UserNotFoundException;
import com.learn.microservices.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    private UserEmailDto createUserEmailDto() {
        return new UserEmailDto(1L, "Ahmed", "Hassan", "test@mail.com", "1234567890", "USER", 1L, "2024-12-26", "2024-12-27", "password");
    }

    @Test
    public void testGetUsers_success() {
        List<UserDto> userDtos = List.of(new UserDto(0L, "Ahmed", "Hassan", "test@mail.com", "1234567890", "ADMIN", 1L, "2024-12-26", "2024-12-27"), new UserDto(1L, "Ahmed", "Hassan2", "test2@mail.com", "1234567890", "USER", 1L, "2024-12-26", "2024-12-27"));
        Page<UserDto> userPage = new PageImpl<>(userDtos);
        when(userService.getUsersByFilter(any(), any(), any())).thenReturn(userPage);

        ResponseEntity<Page<UserDto>> response = userController.getUsers(0, 10, "name", "asc", null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getTotalElements());
    }

    @Test
    public void testGetUserByEmail_success() {
        UserEmailDto userEmailDto = createUserEmailDto();
        when(userService.getUserByEmail(any())).thenReturn(userEmailDto);
        ResponseEntity<UserEmailDto> response = userController.getUserByEmail("test2@mail.com");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testGetUserById_success() {
        UserDto userDto = new UserDto(1L, "Ahmed", "Hassan2", "test2@mail.com", "1234567890", "USER", 1L, "2024-12-26", "2024-12-27");
        when(userService.getUserById(any())).thenReturn(userDto);

        ResponseEntity<UserDto> response = userController.getUserById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testSaveUser_success() {
        UserEmailDto userEmailDto = createUserEmailDto();
        when(userService.saveUser(any())).thenReturn(userEmailDto);

        ResponseEntity<UserDto> response = userController.saveUser(userEmailDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testUpdateUser_success() {
        UserEmailDto userEmailDto = createUserEmailDto();
        when(userService.updateUser(any(), any())).thenReturn(userEmailDto);

        ResponseEntity<UserDto> response = userController.updateUser(1L, userEmailDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testDeleteUser_success() {
        ResponseEntity<Void> response = userController.deleteUser(1L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void testCheckUserExistsByEmail_success() {
        when(userService.checkUserExistsByEmail(any())).thenReturn(true);
        ResponseEntity<Boolean> response = userController.checkUserExistsByEmail("test2@mail.com");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @ParameterizedTest
    @CsvSource({
            "-1, 10",
            "0, -1"
    })
    public void testGetUsers_invalidPagination(int page, int size) {
        assertThrows(InvalidPaginationException.class,
                () -> userController.getUsers(page, size, "name", "asc", null, null));
    }

    @Test
    public void testSaveUser_emailAlreadyExists() {
        UserEmailDto userEmailDto = createUserEmailDto();
        when(userService.saveUser(any())).thenThrow(EmailAlreadyExistException.class);
        assertThrows(EmailAlreadyExistException.class,
                () -> userController.saveUser(userEmailDto));
    }

    @Test
    public void testSaveUser_departmentNotFound() {
        UserEmailDto userEmailDto = createUserEmailDto();
        when(userService.saveUser(any())).thenThrow(DepartmentNotFoundException.class);
        assertThrows(DepartmentNotFoundException.class,
                () -> userController.saveUser(userEmailDto));
    }

    @Test
    public void testUpdateUser_emailAlreadyExists() {
        UserEmailDto userEmailDto = createUserEmailDto();
        when(userService.updateUser(any(), any())).thenThrow(EmailAlreadyExistException.class);
        assertThrows(EmailAlreadyExistException.class,
                () -> userController.updateUser(1L, userEmailDto));
    }

    @Test
    public void testUpdateUser_departmentNotFound() {
        UserEmailDto userEmailDto = createUserEmailDto();
        when(userService.updateUser(any(), any())).thenThrow(DepartmentNotFoundException.class);
        assertThrows(DepartmentNotFoundException.class,
                () -> userController.updateUser(1L, userEmailDto));
    }

    @Test
    public void testUpdateUser_userNotFound() {
        UserEmailDto userEmailDto = createUserEmailDto();
        when(userService.updateUser(any(), any())).thenThrow(UserNotFoundException.class);
        assertThrows(UserNotFoundException.class,
                () -> userController.updateUser(1L, userEmailDto));
    }

    @Test
    public void testDeleteUser_userNotFound() {
        doThrow(UserNotFoundException.class).when(userService).deleteUser(any());
        assertThrows(UserNotFoundException.class,
                () -> userController.deleteUser(1L));
    }

    @Test
    public void testGetUserById_userNotFound() {
        when(userService.getUserById(any())).thenThrow(UserNotFoundException.class);
        assertThrows(UserNotFoundException.class,
                () -> userController.getUserById(1L));
    }

    @Test
    public void testGetUserByEmail_userNotFound() {
        when(userService.getUserByEmail(any())).thenThrow(UserNotFoundException.class);
        assertThrows(UserNotFoundException.class,
                () -> userController.getUserByEmail("test2@mail.com"));
    }

    @Test
    public void testCheckUserExistsByEmail_userNotFound() {
        when(userService.checkUserExistsByEmail(any())).thenReturn(false);
        ResponseEntity<Boolean> response = userController.checkUserExistsByEmail("test2@mail.com");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

}