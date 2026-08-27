package com.sujal.itsm.core.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sujal.itsm.core.user.model.AppUser;

/**
 * Repository for application users.
 */
@Repository
public interface AppUserRepository
        extends JpaRepository<AppUser, Long>, JpaSpecificationExecutor<AppUser> {

  // ==========================================================
  // Authentication
  // ==========================================================

  // ✅ ADDED "roles" to EntityGraph to prevent LazyInitializationException during login
  @EntityGraph(attributePaths = {"department", "roles"})
  Optional<AppUser> findByUsername(String username);

  @EntityGraph(attributePaths = {"department", "roles"})
  Optional<AppUser> findByEmail(String email);

  Optional<AppUser> findByEmployeeId(String employeeId);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  // ==========================================================
  // Administration
  // ==========================================================

  Page<AppUser> findAllByOrderByUsernameAsc(Pageable pageable);

  List<AppUser> findAllByOrderByFullNameAsc();

  @Query("SELECT u FROM AppUser u JOIN u.roles r WHERE r.name = :roleName")
  List<AppUser> findUsersByRoleName(@Param("roleName") String roleName);

  List<AppUser> findByDepartment_Id(Long departmentId);

  List<AppUser> findByIsActiveTrue();

  List<AppUser> findByIsActiveFalse();

  // ==========================================================
  // Search
  // ==========================================================

  @Query(
          """
                SELECT u
                FROM AppUser u
                WHERE
                    LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                """)
  Page<AppUser> search(@Param("keyword") String keyword, Pageable pageable);

  // ==========================================================
  // Dashboard Metrics
  // ==========================================================

  @Query("SELECT COUNT(u) FROM AppUser u")
  long countAllUsers();

  long countByIsActiveTrue();

  long countByIsActiveFalse();

  // ✅ FIXED: Replaced countByRole with custom JOIN query
  @Query("SELECT COUNT(u) FROM AppUser u JOIN u.roles r WHERE r.name = :roleName")
  long countByRoleName(@Param("roleName") String roleName);

  // ==========================================================
  // Assignment Helpers
  // ==========================================================

  // ✅ FIXED: Replaced u.role = 'ROLE_AGENT' with JOIN on roles
  @Query(
          """
                SELECT u
                FROM AppUser u
                JOIN u.roles r
                WHERE r.name = 'AGENT'
                  AND u.isActive = true
                ORDER BY u.fullName
                """)
  List<AppUser> findActiveAgents();

  @Query(
          """
                SELECT u
                FROM AppUser u
                WHERE u.department.id = :departmentId
                  AND u.isActive = true
                ORDER BY u.fullName
                """)
  List<AppUser> findActiveUsersByDepartment(@Param("departmentId") Long departmentId);
}