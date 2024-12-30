package com.learn.microservices.departmentservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.learn.microservices.departmentservice.dto.DepartmentDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
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
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 100)
    @NotNull
    @NotEmpty
    @Column(name = "name", nullable = false, length = 100, unique = true)
    private String name;

    @Size(max = 100)
    @Column(name = "description", nullable = true, length = 100)
    private String description;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    @JsonIgnore
    private Date createdAt;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at", nullable = false)
    @JsonIgnore
    private Date updatedAt;

    public Department(DepartmentDto departmentDto) {
        this(
                departmentDto.getId(),
                departmentDto.getName(),
                departmentDto.getDescription(),
                new Date(System.currentTimeMillis()),
                new Date(System.currentTimeMillis())
        );
    }
}
