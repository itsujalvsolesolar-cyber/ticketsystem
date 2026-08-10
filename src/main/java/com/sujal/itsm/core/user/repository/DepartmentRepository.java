package com.sujal.itsm.core.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sujal.itsm.core.user.model.Department;

/**
 * Repository for Department entities.
 *
 * <p>Enterprise Features:
 *
 * <ul>
 *   <li>Specification support for dynamic filtering
 *   <li>Optimized lookup methods
 *   <li>Alphabetical sorting for UI dropdowns
 *   <li>Existence validation
 * </ul>
 */
@Repository
public interface DepartmentRepository
    extends JpaRepository<Department, Long>, JpaSpecificationExecutor<Department> {

  /**
   * Find department by its unique name.
   *
   * @param name department name
   * @return matching department
   */
  @EntityGraph(attributePaths = {})
  Optional<Department> findByName(String name);

  /**
   * Case-insensitive department lookup.
   *
   * @param name department name
   * @return matching department
   */
  @Query(
      """
            SELECT d
            FROM Department d
            WHERE LOWER(d.name) = LOWER(:name)
            """)
  Optional<Department> findByNameIgnoreCase(@Param("name") String name);

  /**
   * Checks whether a department already exists.
   *
   * @param name department name
   * @return true if department exists
   */
  boolean existsByName(String name);

  /**
   * Returns all departments ordered alphabetically. Useful for dropdowns and administration
   * screens.
   */
  List<Department> findAllByOrderByNameAsc();

  /** Returns total configured departments. */
  @Query("SELECT COUNT(d) FROM Department d")
  long countAllDepartments();
}
