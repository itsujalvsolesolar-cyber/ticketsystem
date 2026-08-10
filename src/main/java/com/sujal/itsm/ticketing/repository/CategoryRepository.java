package com.sujal.itsm.ticketing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sujal.itsm.ticketing.model.Category;

/**
 * Repository for Category entities.
 *
 * <p>Enterprise Features:
 *
 * <ul>
 *   <li>Dynamic query support via Specifications
 *   <li>Optimized lookup methods
 *   <li>Alphabetical and SLA-based sorting
 *   <li>Dashboard aggregation support
 * </ul>
 */
@Repository
public interface CategoryRepository
    extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

  /**
   * Finds a category by its unique name.
   *
   * @param name category name
   * @return matching category
   */
  @EntityGraph(attributePaths = {})
  Optional<Category> findByName(String name);

  /**
   * Finds a category ignoring character case.
   *
   * @param name category name
   * @return matching category
   */
  @Query(
      """
            SELECT c
            FROM Category c
            WHERE LOWER(c.name) = LOWER(:name)
            """)
  Optional<Category> findByNameIgnoreCase(@Param("name") String name);

  /**
   * Checks if a category already exists.
   *
   * @param name category name
   * @return true if found
   */
  boolean existsByName(String name);

  /** Returns all categories ordered alphabetically. */
  List<Category> findAllByOrderByNameAsc();

  /** Returns all categories ordered by SLA hours. Useful for administration and SLA management. */
  List<Category> findAllByOrderBySlaHoursAsc();

  /** Returns the total number of configured categories. */
  @Query("SELECT COUNT(c) FROM Category c")
  long countAllCategories();
}
