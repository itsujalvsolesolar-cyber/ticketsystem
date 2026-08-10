package com.sujal.itsm.itams.repository;

import com.sujal.itsm.itams.model.Product;
import com.sujal.itsm.itams.model.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {

    // ✅ FIX: JOIN FETCH to load Product, Warehouses, and User in one query
    @Query("SELECT t FROM StockTransaction t " +
            "LEFT JOIN FETCH t.product " +
            "LEFT JOIN FETCH t.fromWarehouse " +
            "LEFT JOIN FETCH t.toWarehouse " +
            "LEFT JOIN FETCH t.performedBy " +
            "ORDER BY t.transactionDate DESC")
    List<StockTransaction> findAllRecentTransactions();

    @Query("SELECT t FROM StockTransaction t " +
            "LEFT JOIN FETCH t.product " +
            "LEFT JOIN FETCH t.fromWarehouse " +
            "LEFT JOIN FETCH t.toWarehouse " +
            "LEFT JOIN FETCH t.performedBy " +
            "WHERE t.product = :product ORDER BY t.transactionDate DESC")
    List<StockTransaction> findByProduct(@Param("product") Product product);
}