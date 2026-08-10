package com.sujal.itsm.ticketing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.sujal.itsm.ticketing.model.DashboardWidget;

/**
 * Repository for Dashboard Widget configuration.
 *
 * <p>Enterprise Features:
 *
 * <ul>
 *   <li>Dynamic Specifications
 *   <li>Optimized widget loading
 *   <li>Dashboard configuration support
 *   <li>Ordering and filtering
 * </ul>
 */
@Repository
public interface DashboardWidgetRepository
    extends JpaRepository<DashboardWidget, Long>, JpaSpecificationExecutor<DashboardWidget> {

  // ==========================================================
  // Basic Lookup
  // ==========================================================

  @Override
  @EntityGraph(attributePaths = {})
  Optional<DashboardWidget> findById(Long id);

  // ==========================================================
  // Dashboard Layout
  // ==========================================================

  /** Returns all active widgets ordered by display position. */
  List<DashboardWidget> findByIsActiveTrueOrderBySortOrderAsc();

  /** Returns all widgets ordered by display position. Useful for the admin configuration screen. */
  List<DashboardWidget> findAllByOrderBySortOrderAsc();

  /** Returns widgets of a specific type. Example: STAT_CARD, CHART, TABLE. */
  List<DashboardWidget> findByWidgetTypeOrderBySortOrderAsc(String widgetType);

  /** Checks whether any widget exists with the given title. */
  boolean existsByTitle(String title);

  /** Returns the number of active dashboard widgets. */
  long countByIsActiveTrue();
}
