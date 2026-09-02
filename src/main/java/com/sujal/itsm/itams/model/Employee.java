package com.sujal.itsm.itams.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.core.user.model.Department;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees", indexes = {
    @Index(name = "idx_emp_code", columnList = "employee_code", unique = true),
    @Index(name = "idx_emp_user_id", columnList = "user_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "department"})
@EqualsAndHashCode(of = "id")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_code", nullable = false, unique = true, length = 32)
    private String employeeCode;

    @Column(name = "first_name", nullable = false, length = 64)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 64)
    private String lastName;

    @Column(nullable = false, unique = true, length = 128)
    private String email;

    @Column(length = 32)
    private String phone;

    @Column(name = "job_title", length = 128)
    private String jobTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Department department;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "roles", "employee"})
    private AppUser user;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "date_of_joining")
    private LocalDate dateOfJoining;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public String getEmployeeId() {
        return this.employeeCode;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeCode = employeeId;
    }

    public String getFullName() {
        return ((this.firstName != null ? this.firstName : "") + " " + (this.lastName != null ? this.lastName : "")).trim();
    }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) return;
        String[] parts = fullName.trim().split("\\s+", 2);
        this.firstName = parts[0];
        this.lastName = parts.length > 1 ? parts[1] : "";
    }

    public String getDesignation() {
        return this.jobTitle;
    }

    public void setDesignation(String designation) {
        this.jobTitle = designation;
    }

    public boolean getIsActive() {
        return this.active;
    }

    public void setIsActive(boolean active) {
        this.active = active;
    }
}