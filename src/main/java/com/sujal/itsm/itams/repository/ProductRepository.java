package com.sujal.itsm.itams.repository;

import com.sujal.itsm.itams.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ✅ FIX: Use JOIN FETCH to eagerly load relationships and prevent LazyInitializationException
    @Query("SELECT p FROM Product p " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.brand " +
            "LEFT JOIN FETCH p.supplier " +
            "WHERE p.isActive = true " +
            "ORDER BY p.name ASC")
    List<Product> findByIsActiveTrueOrderByNameAsc();

    // For Phase 4 Reports: Find products below minimum stock level
    @Query("SELECT p FROM Product p WHERE p.currentStock <= p.minStockLevel AND p.isActive = true")
    List<Product> findLowStockProducts();
}