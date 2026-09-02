package com.sujal.itsm.core.user.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sujal.itsm.itams.model.Employee;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.sujal.itsm.core.enums.IdentityType;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "app_users", indexes = {
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_username", columnList = "username")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"roles", "employee", "password", "department"})
@EqualsAndHashCode(of = "id")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false, length = 255)
    @JsonIgnore
    private String password;

    @Column(nullable = false, unique = true, length = 128)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "identity_type", nullable = false, length = 32)
    @Builder.Default
    private IdentityType identityType = IdentityType.HUMAN;

    @Column(name = "first_name", length = 64)
    private String firstName;

    @Column(name = "last_name", length = 64)
    private String lastName;

    @Column(name = "full_name", length = 128)
    private String fullName;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "is_email_verified", nullable = false)
    @Builder.Default
    private boolean isEmailVerified = true;

    @Column(name = "account_non_locked", nullable = false)
    @Builder.Default
    private boolean accountNonLocked = true;

    @Column(name = "two_factor_enabled", nullable = false)
    @Builder.Default
    private boolean twoFactorEnabled = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private Employee employee;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public String getFullName() {
        if (this.fullName != null && !this.fullName.isBlank()) {
            return this.fullName;
        }
        if (this.firstName != null || this.lastName != null) {
            return ((this.firstName != null ? this.firstName : "") + " " + (this.lastName != null ? this.lastName : "")).trim();
        }
        return this.username;
    }

    public boolean isEnabled() {
        return this.isActive;
    }

    public void setEnabled(boolean enabled) {
        this.isActive = enabled;
    }

    public boolean isAdmin() {
        return hasRole("ADMIN") || hasRole("ROLE_ADMIN");
    }

    public boolean hasRole(String roleName) {
        if (roles == null) return false;
        // Normalize: uppercase, spaces -> underscores, strip ROLE_ prefix
        String normalized = roleName.toUpperCase().replace(" ", "_").replace("ROLE_", "");
        return roles.stream().anyMatch(r -> {
            String name = r.getName().toUpperCase().replace(" ", "_").replace("ROLE_", "");
            return name.equals(normalized);
        });
    }

    public boolean hasAnyRole(String... roleNames) {
        for (String roleName : roleNames) {
            if (hasRole(roleName)) return true;
        }
        return false;
    }

    /** IT staff = can see IT Assets, Workflow Builder, etc. */
    public boolean isItStaff() {
        return hasAnyRole("SUPER_ADMIN", "ADMIN", "IT_MANAGER", "IT_EXECUTIVE", "STAFF");
    }
    
    /** Admin level = can see Administration / Users & Roles */
    public boolean isAdminLevel() {
        return hasAnyRole("SUPER_ADMIN", "ADMIN", "IT_MANAGER");
    }

    public void linkEmployee(Employee emp) {
        this.employee = emp;
        if (emp != null && emp.getUser() != this) {
            emp.setUser(this);
        }
    }
}