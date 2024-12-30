package com.learn.microservices.userservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.learn.microservices.userservice.dto.UserEmailDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.sql.Date;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 100)
    @NotNull
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Size(max = 100)
    @NotNull
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Size(max = 255)
    @Email
    @NotNull
    @Column(name = "email", nullable = false)
    private String email;

    @Size(max = 255)
    @NotNull
    @JsonIgnore
    @Column(name = "password", nullable = false)
    private String password;

    @Size(max = 50)
    @Column(name = "phone_number", length = 50)
    private String phoneNumber;

    @Size(max = 50)
    @NotNull
    @Column(name = "role", nullable = false, length = 50)
    private String role;

    @Column(name = "department_id")
    private Long departmentId;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    @JsonIgnore
    private Date createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at", nullable = false)
    @JsonIgnore
    private Date updatedAt;

    public User(UserEmailDto userEmailDto) {
        this(
                userEmailDto.getId(),
                userEmailDto.getFirstName(),
                userEmailDto.getLastName(),
                userEmailDto.getEmail(),
                userEmailDto.getPassword(),
                userEmailDto.getPhoneNumber(),
                userEmailDto.getRole(),
                userEmailDto.getDepartmentId(),
                new Date(System.currentTimeMillis()),
                new Date(System.currentTimeMillis())
        );
    }
}
