package com.learn.microservices.userservice.dto;

import com.learn.microservices.userservice.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;

    @NotBlank(message = "First name cannot be blank")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 50, message = "Phone number cannot exceed 50 characters")
    private String phoneNumber;

    @NotBlank(message = "Role cannot be blank")
    private String role;

    private Long departmentId;
    private String createdAt;
    private String updatedAt;

    public UserDto(User user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
        this.role = user.getRole();
        this.departmentId = user.getDepartmentId();
        if (user.getCreatedAt() != null) {
            this.createdAt = user.getCreatedAt().toString();
        } else {
            this.createdAt = null;
        }
        if (user.getUpdatedAt() != null) {
            this.updatedAt = user.getUpdatedAt().toString();
        } else {
            this.updatedAt = null;
        }
    }
}
