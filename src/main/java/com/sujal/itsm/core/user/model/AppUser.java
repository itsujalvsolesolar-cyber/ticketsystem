package com.sujal.itsm.core.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
    name = "app_users",
    indexes = {
      @Index(name = "idx_user_username", columnList = "username"),
      @Index(name = "idx_user_email", columnList = "email"),
      @Index(name = "idx_user_employee_id", columnList = "employeeId"),
      @Index(name = "idx_user_department", columnList = "department_id"),
      @Index(name = "idx_user_active", columnList = "isActive")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {

  // ============================================
  // IDENTITY & AUTHENTICATION
  // ============================================
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Username is required")
  @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
  @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain letters, numbers, dots, hyphens, and underscores")
  @Column(unique = true, nullable = false, length = 50)
  private String username;

  @NotBlank(message = "Password is required")
  @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
  @Column(nullable = false, length = 255)
  private String password;

  @Email(message = "Please provide a valid email address")
  @NotBlank(message = "Email is required")
  @Size(max = 100)
  @Column(unique = true, nullable = false, length = 100)
  private String email;

  // ============================================
  // PROFILE INFORMATION
  // ============================================
  @Size(max = 100, message = "Full name must be less than 100 characters")
  @Column(length = 100)
  private String fullName;

  @Size(max = 50)
  @Column(name = "first_name", length = 50)
  private String firstName;

  @Size(max = 50)
  @Column(name = "last_name", length = 50)
  private String lastName;

  @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
  @Size(max = 20)
  @Column(length = 20)
  private String phoneNumber;

  @Size(max = 500)
  @Column(length = 500)
  private String bio;

  @Size(max = 255)
  @Column(name = "profile_picture_url", length = 255)
  private String profilePictureUrl;

  // ============================================
  // ROLES & PERMISSIONS (DYNAMIC RBAC)
  // ============================================
  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id")
  )
  @Builder.Default
  private Set<Role> roles = new HashSet<>();

  // ============================================
  // ORGANIZATIONAL INFORMATION
  // ============================================
  @Size(max = 50)
  @Column(name = "employee_id", unique = true, length = 50)
  private String employeeId;

  @Size(max = 100)
  @Column(length = 100)
  private String designation;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "department_id")
  private Department department;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "manager_id")
  private AppUser manager;

  @Column(name = "date_of_joining")
  private LocalDateTime dateOfJoining;

  @Size(max = 50)
  @Column(name = "employee_type", length = 50)
  private String employeeType;

  // ============================================
  // SECURITY & ACCOUNT STATUS
  // ============================================
  @Builder.Default
  @Column(name = "is_active", nullable = false)
  private boolean isActive = true;

  @Builder.Default
  @Column(name = "is_account_locked", nullable = false)
  private boolean isAccountLocked = false;

  @Builder.Default
  @Column(name = "is_email_verified", nullable = false)
  private boolean isEmailVerified = false;

  @Column(name = "failed_login_attempts")
  @Builder.Default
  private Integer failedLoginAttempts = 0;

  @Column(name = "last_login_date")
  private LocalDateTime lastLoginDate;

  @Column(name = "last_password_change_date")
  private LocalDateTime lastPasswordChangeDate;

  @Column(name = "account_lockout_until")
  private LocalDateTime accountLockoutUntil;

  @Size(max = 255)
  @Column(name = "password_reset_token", length = 255)
  private String passwordResetToken;

  @Column(name = "password_reset_token_expiry")
  private LocalDateTime passwordResetTokenExpiry;

  @Builder.Default
  @Column(name = "two_factor_enabled", nullable = false)
  private boolean twoFactorEnabled = false;

  // ============================================
  // PREFERENCES & SETTINGS
  // ============================================
  @Size(max = 10)
  @Column(name = "preferred_language", length = 10)
  @Builder.Default
  private String preferredLanguage = "en";

  @Size(max = 50)
  @Column(name = "timezone", length = 50)
  @Builder.Default
  private String timezone = "Asia/Kolkata";

  @Size(max = 20)
  @Column(name = "theme", length = 20)
  @Builder.Default
  private String theme = "light";

  @Column(name = "email_notifications_enabled")
  @Builder.Default
  private boolean emailNotificationsEnabled = true;

  // ============================================
  // AUDIT FIELDS
  // ============================================
  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "created_by")
  private Long createdBy;

  @Column(name = "updated_by")
  private Long updatedBy;

  // ============================================
  // HELPER METHODS (REFACTORED FOR DYNAMIC RBAC)
  // ============================================
  public boolean hasRole(String roleName) {
      return roles.stream().anyMatch(r -> r.getName().equalsIgnoreCase(roleName));
  }

  public boolean hasAuthority(String permissionCode) {
      return roles.stream()
              .flatMap(r -> r.getPermissions().stream())
              .anyMatch(p -> p.getCode().equals(permissionCode));
  }

  public boolean isAdmin() {
      return hasRole("SUPER ADMIN") || hasRole("ADMIN");
  }

  // Refactored to use dynamic string matching instead of legacy Enum
  public boolean isManager() {
      return hasRole("MANAGER") || hasRole("DEPARTMENT HEAD");
  }

  public boolean isAgent() {
      return hasRole("AGENT") || hasRole("IT EXECUTIVE") || hasRole("HR EXECUTIVE");
  }

  public boolean isAccountCurrentlyLocked() {
    if (!isAccountLocked) return false;
    if (accountLockoutUntil == null) return true;
    return LocalDateTime.now().isBefore(accountLockoutUntil);
  }

  public void incrementFailedLoginAttempts() {
    this.failedLoginAttempts = (this.failedLoginAttempts == null) ? 1 : this.failedLoginAttempts + 1;
    if (this.failedLoginAttempts >= 5) {
      this.isAccountLocked = true;
      this.accountLockoutUntil = LocalDateTime.now().plusMinutes(30);
    }
  }

  public void resetFailedLoginAttempts() {
    this.failedLoginAttempts = 0;
    this.lastLoginDate = LocalDateTime.now();
    this.isAccountLocked = false;
    this.accountLockoutUntil = null;
  }

  public boolean isPasswordResetTokenValid() {
    return passwordResetToken != null && passwordResetTokenExpiry != null && LocalDateTime.now().isBefore(passwordResetTokenExpiry);
  }

  public String getDisplayName() {
    if (fullName != null && !fullName.trim().isEmpty()) return fullName;
    if (firstName != null && lastName != null) return firstName + " " + lastName;
    return username;
  }

  public String getInitials() {
    if (firstName != null && lastName != null) {
      return (firstName.charAt(0) + "" + lastName.charAt(0)).toUpperCase();
    }
    if (fullName != null && fullName.length() >= 2) {
      String[] parts = fullName.split(" ");
      if (parts.length >= 2) return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
      return fullName.substring(0, 2).toUpperCase();
    }
    return username.substring(0, Math.min(2, username.length())).toUpperCase();
  }
}