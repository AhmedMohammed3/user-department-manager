package com.learn.microservices.userservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.microservices.userservice.dto.UserDto;
import com.learn.microservices.userservice.dto.UserEmailDto;
import com.learn.microservices.userservice.entity.User;
import com.learn.microservices.userservice.exception.DepartmentNotFoundException;
import com.learn.microservices.userservice.exception.EmailAlreadyExistException;
import com.learn.microservices.userservice.exception.UserNotFoundException;
import com.learn.microservices.userservice.proxy.DepartmentServiceProxy;
import com.learn.microservices.userservice.repository.UserRepository;
import com.learn.microservices.userservice.util.PaginationOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private DepartmentServiceProxy departmentServiceProxy;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository, objectMapper, departmentServiceProxy, passwordEncoder);
    }

    @Test
    void testGetUsersByFilter_roleAndDepartmentId() {
        String sortBy = "firstName";
        PaginationOptions paginationOptions = new PaginationOptions(0, 10, sortBy, "asc");
        Pageable pageable = PageRequest.of(paginationOptions.getPage(), paginationOptions.getSize(), Sort.by(Sort.Order.asc(sortBy)));
        String role = "ADMIN";
        Long departmentId = 1L;

        Page<User> userPage = new PageImpl<>(List.of(new User()));
        when(userRepository.findByRoleAndDepartmentId(role, departmentId, pageable)).thenReturn(userPage);

        Page<UserDto> result = userService.getUsersByFilter(role, departmentId, paginationOptions);

        assertNotNull(result);
        verify(userRepository, times(1)).findByRoleAndDepartmentId(role, departmentId, pageable);
    }

    @Test
    void testGetUsersByFilter_roleOnly() {
        String sortBy = "firstName";
        PaginationOptions paginationOptions = new PaginationOptions(0, 10, sortBy, "asc");
        Pageable pageable = PageRequest.of(paginationOptions.getPage(), paginationOptions.getSize(), Sort.by(Sort.Order.asc(sortBy)));
        String role = "USER";

        Page<User> userPage = new PageImpl<>(List.of(new User()));
        when(userRepository.findByRole(role, pageable)).thenReturn(userPage);

        Page<UserDto> result = userService.getUsersByFilter(role, null, paginationOptions);

        assertNotNull(result);
        verify(userRepository, times(1)).findByRole(role, pageable);
    }

    @Test
    void testGetUsersByFilter_departmentIdOnly() {
        String sortBy = "firstName";
        PaginationOptions paginationOptions = new PaginationOptions(0, 10, sortBy, "asc");
        Pageable pageable = PageRequest.of(paginationOptions.getPage(), paginationOptions.getSize(), Sort.by(Sort.Order.asc(sortBy)));
        Long departmentId = 2L;

        Page<User> userPage = new PageImpl<>(List.of(new User()));
        when(departmentServiceProxy.isDepartmentExists(departmentId)).thenReturn(true);
        when(userRepository.findByDepartmentId(departmentId, pageable)).thenReturn(userPage);

        Page<UserDto> result = userService.getUsersByFilter(null, departmentId, paginationOptions);

        assertNotNull(result);
        verify(userRepository, times(1)).findByDepartmentId(departmentId, pageable);
    }

    @Test
    void testGetUsersByFilter_noFilters() {
        String sortBy = "firstName";
        PaginationOptions paginationOptions = new PaginationOptions(0, 10, sortBy, "asc");
        Pageable pageable = PageRequest.of(paginationOptions.getPage(), paginationOptions.getSize(), Sort.by(Sort.Order.asc(sortBy)));

        Page<User> userPage = new PageImpl<>(List.of(new User()));
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        Page<UserDto> result = userService.getUsersByFilter(null, null, paginationOptions);

        assertNotNull(result);
        verify(userRepository, times(1)).findAll(pageable);
    }

    @Test
    void testGetUsersByFilter_noFilters_emptySortBy() {
        String sortBy = " ";
        PaginationOptions paginationOptions = new PaginationOptions(0, 10, sortBy, "asc");
        Pageable pageable = PageRequest.of(paginationOptions.getPage(), paginationOptions.getSize());

        Page<User> userPage = new PageImpl<>(List.of(new User()));
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        Page<UserDto> result = userService.getUsersByFilter(null, null, paginationOptions);

        assertNotNull(result);
        verify(userRepository, times(1)).findAll(pageable);
    }

    @Test
    void testGetUsersByFilter_sortAscending() {
        PaginationOptions paginationOptions = new PaginationOptions(0, 10, "firstName", "asc");

        String role = "ADMIN";
        Long departmentId = 1L;

        Page<User> userPage = new PageImpl<>(List.of(new User()));
        when(userRepository.findByRoleAndDepartmentId(role, departmentId, PageRequest.of(0, 10, Sort.by(Sort.Order.asc("firstName"))))).thenReturn(userPage);

        Page<UserDto> result = userService.getUsersByFilter(role, departmentId, paginationOptions);

        assertNotNull(result);
        verify(userRepository, times(1)).findByRoleAndDepartmentId(role, departmentId, PageRequest.of(0, 10, Sort.by(Sort.Order.asc("firstName"))));
    }

    @Test
    void testGetUsersByFilter_sortDescending() {
        PaginationOptions paginationOptions = new PaginationOptions(0, 10, "firstName", "desc");

        String role = "USER";
        Long departmentId = 2L;

        Page<User> userPage = new PageImpl<>(List.of(new User()));
        when(userRepository.findByRoleAndDepartmentId(role, departmentId, PageRequest.of(0, 10, Sort.by(Sort.Order.desc("firstName"))))).thenReturn(userPage);

        Page<UserDto> result = userService.getUsersByFilter(role, departmentId, paginationOptions);

        assertNotNull(result);
        verify(userRepository, times(1)).findByRoleAndDepartmentId(role, departmentId, PageRequest.of(0, 10, Sort.by(Sort.Order.desc("firstName"))));
    }

    @Test
    void testGetUsersByFilter_exception() {
        PaginationOptions paginationOptions = new PaginationOptions(0, 10, "firstName", "asc");

        String role = "ADMIN";
        Long departmentId = 1L;

        when(userRepository.findByRoleAndDepartmentId(role, departmentId, Pageable.unpaged())).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> userService.getUsersByFilter(role, departmentId, paginationOptions));
    }

    @Test
    void testGetUsersByFilter_invalidRole() {
        String sortBy = "firstName";
        PaginationOptions paginationOptions = new PaginationOptions(0, 10, sortBy, "asc");
        Pageable pageable = PageRequest.of(paginationOptions.getPage(), paginationOptions.getSize(), Sort.by(Sort.Order.asc(sortBy)));

        Page<User> userPage = new PageImpl<>(List.of(new User()));
        when(userRepository.findByRole("", pageable)).thenReturn(userPage);

        Page<UserDto> result = userService.getUsersByFilter("", null, paginationOptions);

        assertNotNull(result);
        verify(userRepository, times(1)).findByRole("", pageable);
    }

    @Test
    void testGetUsersByFilter_invalidPagination() {
        String sortBy = "firstName";
        PaginationOptions paginationOptions = new PaginationOptions(0, 10, sortBy, "asc");
        Pageable pageable = PageRequest.of(paginationOptions.getPage(), paginationOptions.getSize(), Sort.by(Sort.Order.asc(sortBy)));

        String role = "USER";
        Long departmentId = 2L;

        Page<User> userPage = new PageImpl<>(List.of(new User()));
        when(userRepository.findByRoleAndDepartmentId(role, departmentId, pageable)).thenReturn(userPage);

        Page<UserDto> result = userService.getUsersByFilter(role, departmentId, paginationOptions);

        assertNotNull(result);
        verify(userRepository, times(1)).findByRoleAndDepartmentId(role, departmentId, pageable);
    }

    @Test
    void getUserById_ShouldReturnUserDto() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");

        UserDto userDto = new UserDto();
        userDto.setEmail("test@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(objectMapper.convertValue(user, UserDto.class)).thenReturn(userDto);

        UserDto result = userService.getUserById(userId);

        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getUserById_ShouldThrowUserNotFoundException_WhenUserNotFound() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(userId));
    }

    @Test
    void getUserByEmail_ShouldReturnUserEmailDto() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);

        UserEmailDto userEmailDto = new UserEmailDto();
        userEmailDto.setEmail(email);

        when(userRepository.getByEmail(email)).thenReturn(Optional.of(user));
        when(objectMapper.convertValue(user, UserEmailDto.class)).thenReturn(userEmailDto);

        UserEmailDto result = userService.getUserByEmail(email);

        assertEquals(email, result.getEmail());
        verify(userRepository, times(1)).getByEmail(email);
    }

    @Test
    void getUserByEmail_ShouldThrowUserNotFoundException_WhenUserNotFound() {
        String email = "test@example.com";
        when(userRepository.getByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail(email));
    }

    @Test
    void saveUser_ShouldSaveAndReturnUserDto() {
        UserEmailDto userEmailDto = new UserEmailDto();
        userEmailDto.setEmail("newuser@example.com");
        userEmailDto.setDepartmentId(1L);

        User user = new User();
        user.setEmail(userEmailDto.getEmail());

        UserDto userDto = new UserDto();
        userDto.setEmail(userEmailDto.getEmail());

        when(departmentServiceProxy.isDepartmentExists(userEmailDto.getDepartmentId())).thenReturn(true);
        when(objectMapper.convertValue(userEmailDto, User.class)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(objectMapper.convertValue(user, UserDto.class)).thenReturn(userDto);


        UserDto result = userService.saveUser(userEmailDto);


        assertEquals(userEmailDto.getEmail(), result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void saveUser_ShouldThrowEmailAlreadyExistException_WhenEmailExists() {
        UserEmailDto userEmailDto = new UserEmailDto();
        userEmailDto.setEmail("existinguser@example.com");

        when(userRepository.getByEmail(userEmailDto.getEmail())).thenReturn(Optional.of(new User()));

        assertThrows(EmailAlreadyExistException.class, () -> userService.saveUser(userEmailDto));
    }

    @Test
    void saveUser_ShouldThrowDepartmentNotFoundException_WhenDepartmentDoesNotExist() {
        UserEmailDto userEmailDto = new UserEmailDto();
        userEmailDto.setEmail("newuser@example.com");
        userEmailDto.setDepartmentId(1L);

        when(departmentServiceProxy.isDepartmentExists(userEmailDto.getDepartmentId())).thenReturn(false);

        assertThrows(DepartmentNotFoundException.class, () -> userService.saveUser(userEmailDto));
    }

    @Test
    void deleteUser_ShouldDeleteUser() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);

        userService.deleteUser(userId);

        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void deleteUser_ShouldThrowUserNotFoundException_WhenUserDoesNotExist() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(userId));
    }

    @Test
    void updateUser_ShouldUpdateAndReturnUserDto() {
        Long userId = 1L;
        UserEmailDto userEmailDto = new UserEmailDto();
        userEmailDto.setEmail("updated@example.com");
        userEmailDto.setDepartmentId(2L);

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEmail("old@example.com");

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setEmail(userEmailDto.getEmail());

        UserDto updatedUserDto = new UserDto();
        updatedUserDto.setEmail(userEmailDto.getEmail());

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(departmentServiceProxy.isDepartmentExists(userEmailDto.getDepartmentId())).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(objectMapper.convertValue(updatedUser, UserDto.class)).thenReturn(updatedUserDto);

        UserDto result = userService.updateUser(userId, userEmailDto);

        assertEquals(userEmailDto.getEmail(), result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_ShouldThrowEmailAlreadyExistException_WhenEmailAlreadyExists() {
        Long userId = 1L;
        UserEmailDto userEmailDto = new UserEmailDto();
        userEmailDto.setEmail("existing@example.com");

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEmail("old@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.getByEmail(userEmailDto.getEmail())).thenReturn(Optional.of(new User()));

        assertThrows(EmailAlreadyExistException.class, () -> userService.updateUser(userId, userEmailDto));
    }

    @Test
    void updateUser_ShouldThrowDepartmentNotFoundException_WhenDepartmentDoesNotExist() {
        Long userId = 1L;
        UserEmailDto userEmailDto = new UserEmailDto();
        userEmailDto.setEmail("updated@example.com");
        userEmailDto.setDepartmentId(999L);

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEmail("old@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(departmentServiceProxy.isDepartmentExists(userEmailDto.getDepartmentId())).thenReturn(false);

        assertThrows(DepartmentNotFoundException.class, () -> userService.updateUser(userId, userEmailDto));
    }
}
