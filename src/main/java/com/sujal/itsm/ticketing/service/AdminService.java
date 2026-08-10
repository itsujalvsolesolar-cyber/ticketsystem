package com.sujal.itsm.ticketing.service;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sujal.itsm.core.exception.DuplicateResourceException;
import com.sujal.itsm.core.exception.UserAlreadyExistsException;
import com.sujal.itsm.core.exception.UserNotFoundException;
import com.sujal.itsm.core.security.CurrentUserService;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.core.user.model.Department;
import com.sujal.itsm.core.user.repository.AppUserRepository;
import com.sujal.itsm.core.user.repository.DepartmentRepository;
import com.sujal.itsm.ticketing.dto.AdminSettingsView;
import com.sujal.itsm.ticketing.dto.UserCreateRequest;
import com.sujal.itsm.ticketing.dto.UserUpdateRequest;
import com.sujal.itsm.ticketing.model.Category;
import com.sujal.itsm.ticketing.repository.CategoryRepository;
import com.sujal.itsm.core.user.model.Role;
import com.sujal.itsm.core.user.repository.RoleRepository;
import java.util.Set;

/**
 * Enterprise Admin Service Handles all administrative operations: user management,
 * department/category CRUD.
 *
 * <p>Key improvements over controller-based logic: - Centralized validation (no duplicate checks
 * scattered across methods) - Transactional integrity (all-or-nothing operations) - Testable in
 * isolation (no web dependencies) - Clean separation of concerns
 *
 * @author Enterprise Architecture Team
 * @version 2.0.0
 */
@Service
@Transactional
public class AdminService {

  private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

  private final AppUserRepository userRepository;
  private final DepartmentRepository departmentRepository;
  private final CategoryRepository categoryRepository;
  private final PasswordEncoder passwordEncoder;
  private final CurrentUserService currentUserService;
  private final RoleRepository roleRepository;

  public AdminService(
          AppUserRepository userRepository,
          DepartmentRepository departmentRepository,
          CategoryRepository categoryRepository,
          PasswordEncoder passwordEncoder,
          CurrentUserService currentUserService, RoleRepository roleRepository) {
    this.userRepository = userRepository;
    this.departmentRepository = departmentRepository;
    this.categoryRepository = categoryRepository;
    this.passwordEncoder = passwordEncoder;
    this.currentUserService = currentUserService;
      this.roleRepository = roleRepository;
  }

  // ============================================
  // SETTINGS PAGE
  // ============================================

  /** Loads all data needed for the admin settings page. */
  @Transactional(readOnly = true)
  public AdminSettingsView loadSettingsPage() {
    AppUser currentUser = currentUserService.getCurrentUser();

    return AdminSettingsView.builder()
        .currentUser(currentUser)
        .departments(departmentRepository.findAll())
        .categories(categoryRepository.findAll())
        .users(userRepository.findAll())
        .availableRoles(roleRepository.findAll())
        .build();
  }

  // ============================================
  // DEPARTMENT MANAGEMENT
  // ============================================

  /**
   * Creates a new department with duplicate checking.
   *
   * @throws DuplicateResourceException if department name already exists
   */
  public Department createDepartment(String name) {
    if (departmentRepository.findByName(name).isPresent()) {
      throw new DuplicateResourceException("Department", name);
    }

    Department dept = Department.builder().name(name).build();

    Department saved = departmentRepository.save(dept);
    logger.info("Department created: {} (id={})", name, saved.getId());
    return saved;
  }

  // ============================================
  // CATEGORY MANAGEMENT
  // ============================================

  /**
   * Creates a new category with duplicate checking.
   *
   * @throws DuplicateResourceException if category name already exists
   */
  public Category createCategory(String name) {
    if (categoryRepository.findByName(name).isPresent()) {
      throw new DuplicateResourceException("Category", name);
    }

    Category cat = Category.builder().name(name).build();

    Category saved = categoryRepository.save(cat);
    logger.info("Category created: {} (id={})", name, saved.getId());
    return saved;
  }

  // ============================================
  // USER MANAGEMENT
  // ============================================

  /**
   * Creates a new user with comprehensive validation.
   *
   * @throws UserAlreadyExistsException if username or email is taken
   */
  public AppUser createUser(UserCreateRequest request) {
    // Validate uniqueness
    validateUsernameUniqueness(request.getUsername(), null);
    validateEmailUniqueness(request.getEmail(), null);

    // Build the user entity
    Role targetRole = roleRepository.findByName(request.getRole())
            .orElseThrow(() ->
                    new RuntimeException("Role not found: " + request.getRole()));

    AppUser user =
            AppUser.builder()
                    .username(request.getUsername())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .email(request.getEmail())
                    .fullName(request.getUsername())
                    .firstName(request.getUsername())
                    .lastName("")
                    .department(resolveDepartment(request.getDepartmentId()))
                    .isActive(true)
                    .isEmailVerified(false)
                    .roles(Set.of(targetRole))
                    .build();

    AppUser saved = userRepository.save(user);
    logger.info(
        "User created: {} with role {} (id={})",
        saved.getUsername(),
        saved.getRoles(),
        saved.getId());
    return saved;
  }

  /**
   * Updates an existing user with comprehensive validation.
   *
   * @throws UserNotFoundException if user doesn't exist
   * @throws UserAlreadyExistsException if new username/email is taken by another user
   */
  public AppUser updateUser(Long userId, UserUpdateRequest request) {
    AppUser user = requireUser(userId);

    // Validate uniqueness (excluding the current user)
    validateUsernameUniqueness(request.getUsername(), userId);
    validateEmailUniqueness(request.getEmail(), userId);

    // Update fields
    user.setUsername(request.getUsername());
    user.setEmail(request.getEmail());
    Role targetRole = roleRepository.findByName(request.getRole())
            .orElseThrow(() ->
                    new RuntimeException("Role not found: " + request.getRole()));

    user.setRoles(Set.of(targetRole));
    user.setDepartment(resolveDepartment(request.getDepartmentId()));

    // Only update password if provided
    if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
      user.setPassword(passwordEncoder.encode(request.getPassword()));
    }

    AppUser saved = userRepository.save(user);
    logger.info("User updated: {} (id={})", saved.getUsername(), saved.getId());
    return saved;
  }

  /**
   * Deletes a user by ID.
   *
   * @throws UserNotFoundException if user doesn't exist
   */
  public void deleteUser(Long userId) {
    if (!userRepository.existsById(userId)) {
      throw new UserNotFoundException(userId);
    }
    userRepository.deleteById(userId);
    logger.info("User deleted: id={}", userId);
  }

  // ============================================
  // PRIVATE HELPERS
  // ============================================

  /**
   * Retrieves a user or throws UserNotFoundException. Follows the "require" pattern for fail-fast
   * behavior.
   */
  private AppUser requireUser(Long userId) {
    return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
  }

  /**
   * Validates that a username is unique across all users. If excludeUserId is provided, that user
   * is excluded from the check (for updates).
   */
  private void validateUsernameUniqueness(String username, Long excludeUserId) {
    userRepository
        .findByUsername(username)
        .ifPresent(
            existing -> {
              if (excludeUserId == null || !existing.getId().equals(excludeUserId)) {
                throw new UserAlreadyExistsException("Username already exists: " + username);
              }
            });
  }

  /** Validates that an email is unique across all users. */
  private void validateEmailUniqueness(String email, Long excludeUserId) {
    if (email == null || email.isBlank()) return;

    userRepository
        .findByEmail(email)
        .ifPresent(
            existing -> {
              if (excludeUserId == null || !existing.getId().equals(excludeUserId)) {
                throw new UserAlreadyExistsException("Email already exists: " + email);
              }
            });
  }

  /** Resolves a department ID to a Department entity (or null if not provided). */
  private Department resolveDepartment(Long departmentId) {
    if (departmentId == null) return null;
    return departmentRepository.findById(departmentId).orElse(null);
  }
}
