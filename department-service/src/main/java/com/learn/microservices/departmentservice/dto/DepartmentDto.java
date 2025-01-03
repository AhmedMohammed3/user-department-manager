package com.learn.microservices.departmentservice.dto;

import com.learn.microservices.departmentservice.entity.Department;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDto {
    private Long id;

    @NotBlank(message = "Department name cannot be blank")
    @Size(max = 100, message = "Department name cannot exceed 100 characters")
    private String name;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    private String createdAt;
    private String updatedAt;

    public DepartmentDto(Department department) {
        this.id = department.getId();
        this.name = department.getName();
        this.description = department.getDescription();
        if (department.getCreatedAt() != null) {
            this.createdAt = department.getCreatedAt().toString();
        } else {
            this.createdAt = null;
        }
        if (department.getUpdatedAt() != null) {
            this.updatedAt = department.getUpdatedAt().toString();
        } else {
            this.updatedAt = null;
        }
    }
}
