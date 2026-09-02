package com.sujal.itsm.itams.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class EmployeeOnboardingDTO {
    
    // --- HR / Identity Data ---
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String phone;
    private String jobTitle;
    
    @NotNull(message = "Department is required")
    private Long departmentId;
    
    private LocalDate dateOfJoining;

    // --- IT Provisioning Data ---
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Temporary password is required")
    private String temporaryPassword;

    private boolean sendActivationEmail = true;
}