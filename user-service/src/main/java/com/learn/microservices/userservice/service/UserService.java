package com.learn.microservices.userservice.service;

import com.fasterxml.jackson.databind.JsonMappingException;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final DepartmentServiceProxy departmentServiceProxy;
    private final PasswordEncoder passwordEncoder;

    public Page<UserDto> getUsersByFilter(String role, Long departmentId,
                                          PaginationOptions paginationOptions) {
        log.info("Fetching users with pagination, page: {}, size: {}", paginationOptions.getPage(), paginationOptions.getSize());
        try {
            Page<User> users;

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

            if (role != null && departmentId != null) {
                log.info("Fetching users by role: {} and department ID: {}", role, departmentId);
                users = userRepository.findByRoleAndDepartmentId(role, departmentId, sortedPageable);
            } else if (role != null) {
                log.info("Fetching users by role: {}", role);
                users = userRepository.findByRole(role, sortedPageable);
            } else if (departmentId != null) {
                if (!departmentServiceProxy.isDepartmentExists(departmentId)) {
                    String departmentIdMsg = "Department with id " + departmentId + " not found.";
                    log.warn(departmentIdMsg);
                    throw new DepartmentNotFoundException(departmentIdMsg);
                }
                log.info("Fetching users by department ID: {}", departmentId);
                users = userRepository.findByDepartmentId(departmentId, sortedPageable);
            } else {
                log.info("Fetching all users.");
                users = userRepository.findAll(sortedPageable);
            }

            Page<UserDto> userDtos = users.map(UserDto::new);
            log.info("Successfully fetched {} users.", userDtos.getTotalElements());
            return userDtos;
        } catch (DepartmentNotFoundException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Failed to fetch users, " + e.getMessage();
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    public UserDto getUserById(Long id) {
        log.info("Fetching user by id: {}", id);
        User user = getUserIfExist(id);

        return new UserDto(user);
    }

    public UserEmailDto getUserByEmail(String email) {
        log.info("Fetching user by email: {}", email);
        User user = userRepository.getByEmail(email).orElseThrow(() -> {
            String msg = "User with email " + email + " not found.";
            log.warn(msg);
            return new UserNotFoundException(msg);
        });

        return new UserEmailDto(user);
    }

    public UserDto saveUser(UserEmailDto userEmailDto) {
        if (checkUserExistsByEmail(userEmailDto.getEmail())) {
            String msg = "User with email " + userEmailDto.getEmail() + " already exists.";
            log.warn(msg);
            throw new EmailAlreadyExistException(msg);
        }

        if (!departmentServiceProxy.isDepartmentExists(userEmailDto.getDepartmentId())) {
            String msg = "Department with id " + userEmailDto.getDepartmentId() + " not found.";
            log.warn(msg);
            throw new DepartmentNotFoundException(msg);
        }

        log.info("Saving user with email: {}", userEmailDto.getEmail());
        userEmailDto.setId(null);
        userEmailDto.setCreatedAt(LocalDateTime.now().toString());
        userEmailDto.setUpdatedAt(LocalDateTime.now().toString());
        userEmailDto.setPassword(passwordEncoder.encode(userEmailDto.getPassword()));
        User user = new User(userEmailDto);
        User createdUser = userRepository.save(user);
        return new UserDto(createdUser);
    }

    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);
        if (!userRepository.existsById(id)) {
            String msg = "User with id " + id + " not found.";
            log.warn(msg);
            throw new UserNotFoundException(msg);
        }
        userRepository.deleteById(id);
        log.info("User with id {} deleted successfully.", id);
    }

    public UserDto updateUser(Long id, UserEmailDto userDto) {
        User existingUser = getUserIfExist(id);

        log.info("Updating user with id: {}", id);
        if (!existingUser.getEmail().equals(userDto.getEmail())
                && checkUserExistsByEmail(userDto.getEmail())) {
            String msg = "User with email " + userDto.getEmail() + " already exists.";
            log.warn(msg);
            throw new EmailAlreadyExistException(msg);
        }

        Long departmentId = userDto.getDepartmentId();
        if (departmentId != null && !departmentId.equals(existingUser.getDepartmentId()) && !departmentServiceProxy.isDepartmentExists(departmentId)) {
            String msg = "Department with id " + departmentId + " not found.";
            log.warn(msg);
            throw new DepartmentNotFoundException(msg);
        }

        try {
            userDto.setId(id);
            objectMapper.updateValue(existingUser, userDto);
        } catch (JsonMappingException e) {
            log.error("Error mapping user data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update user data", e);
        }

        User updatedUser = userRepository.save(existingUser);
        log.info("User with id {} updated successfully.", id);
        return new UserDto(updatedUser);
    }

    public boolean checkUserExistsByEmail(String email) {
        return userRepository.getByEmail(email).isPresent();
    }

    private User getUserIfExist(Long id) {
        return userRepository.findById(id).orElseThrow(() -> {
            String msg = "User with id " + id + " not found.";
            log.warn(msg);
            return new UserNotFoundException(msg);
        });
    }
}
