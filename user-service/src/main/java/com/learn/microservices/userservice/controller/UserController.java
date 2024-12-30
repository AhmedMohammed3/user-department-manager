package com.learn.microservices.userservice.controller;

import com.learn.microservices.userservice.dto.UserDto;
import com.learn.microservices.userservice.dto.UserEmailDto;
import com.learn.microservices.userservice.exception.InvalidPaginationException;
import com.learn.microservices.userservice.service.UserService;
import com.learn.microservices.userservice.util.PaginationOptions;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/")
    public ResponseEntity<Page<UserDto>> getUsers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "sortOrder", required = false) String sortOrder,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "department", required = false) Long department) {

        PaginationOptions paginationOptions = new PaginationOptions(page, size, sortBy, sortOrder);

        if (paginationOptions.getPage() < 0 || paginationOptions.getSize() <= 0) {
            log.warn("Invalid pagination parameters: page={}, size={}", paginationOptions.getPage(), paginationOptions.getSize());
            throw new InvalidPaginationException("Page number cannot be negative and size must be greater than 0.");
        }
        log.info("Fetching users with pagination: page={}, size={}", paginationOptions.getPage(), paginationOptions.getSize());

        Page<UserDto> usersPage = userService.getUsersByFilter(role, department, paginationOptions);

        log.info("Successfully fetched {} users.", usersPage.getTotalElements());
        return ResponseEntity.ok(usersPage);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserEmailDto> getUserByEmail(@PathVariable @Email String email) {
        log.info("Fetching users by email: email={}", email);
        UserEmailDto userEmailDto = userService.getUserByEmail(email);
        log.info("Successfully fetched user by email: {}", email);
        return ResponseEntity.ok(userEmailDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        log.info("Fetching user by id: id={}", id);
        UserDto user = userService.getUserById(id);
        log.info("Successfully fetched user by id: {}", id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/")
    public ResponseEntity<UserDto> saveUser(@Valid @RequestBody UserEmailDto user) {
        log.info("Saving user: {}", user);
        UserDto savedUser = userService.saveUser(user);
        log.info("Successfully saved user: {}", savedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @Valid @RequestBody UserEmailDto user) {
        log.info("Updating user with id: {}, {}", id, user);
        UserDto updatedUser = userService.updateUser(id, user);
        log.info("Successfully updated user: {}", updatedUser);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with id: {}", id);
        userService.deleteUser(id);
        log.info("Successfully deleted user with id: {}", id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> checkUserExistsByEmail(@PathVariable String email) {
        log.info("Checking if user exists by email: {}", email);
        boolean exists = userService.checkUserExistsByEmail(email);
        if (exists) {
            log.info("User exists by email: {}", email);
            return ResponseEntity.ok(exists);
        }
        log.info("User does not exist by email: {}", email);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exists);
    }

}
