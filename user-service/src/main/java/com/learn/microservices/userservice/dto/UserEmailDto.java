package com.learn.microservices.userservice.dto;

import com.learn.microservices.userservice.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserEmailDto extends UserDto {

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    public UserEmailDto(Long id, String firstName, String lastName, String email, String phoneNumber, String role, Long departmentId, String createdAt, String updatedAt, String password) {
        super(id, firstName, lastName, email, phoneNumber, role, departmentId, createdAt, updatedAt);
        this.password = password;
    }

    public UserEmailDto(User user) {
        super(user);
        this.password = user.getPassword();
    }

}
