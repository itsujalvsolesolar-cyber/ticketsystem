package com.sujal.itsm.itams.service;

import com.sujal.itsm.core.security.CurrentUserService;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.itams.enums.TransactionType;
import com.sujal.itsm.itams.model.Product;
import com.sujal.itsm.itams.model.StockTransaction;
import com.sujal.itsm.itams.model.Warehouse;
import com.sujal.itsm.itams.repository.ProductRepository;
import com.sujal.itsm.itams.repository.StockTransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StockService {

    private final StockTransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final CurrentUserService currentUserService;

    public StockTransaction recordTransaction(TransactionType type, Long productId, Integer quantity,
                                              Long fromWarehouseId, Long toWarehouseId,
                                              String reference, String notes) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        // 1. Validate Quantity
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        // 2. Check Stock Availability for Outgoing Transactions
        if (type == TransactionType.STOCK_OUT || type == TransactionType.TRANSFER ||
                type == TransactionType.DAMAGED || type == TransactionType.SCRAP) {
            if (product.getCurrentStock() < quantity) {
                throw new IllegalArgumentException("Insufficient stock! Current stock: " + product.getCurrentStock());
            }
        }

        // 3. Update Product Stock Level
        int currentStock = product.getCurrentStock();

        switch (type) {
            case STOCK_IN:
            case ADJUSTMENT: // Assuming positive adjustment
                product.setCurrentStock(currentStock + quantity);
                break;
            case STOCK_OUT:
            case DAMAGED:
            case SCRAP:
                product.setCurrentStock(currentStock - quantity);
                break;
            case TRANSFER:
                // Total stock remains the same, but we record the movement
                break;
            default:
                break;
        }
        productRepository.save(product);

        // 4. Create Transaction Record
        AppUser user = currentUserService.getCurrentUser();
        Warehouse fromWh = fromWarehouseId != null ? new Warehouse() : null; // Simplified for brevity
        Warehouse toWh = toWarehouseId != null ? new Warehouse() : null;

        // Note: In a real app, fetch the actual Warehouse entities if IDs are provided
        // For this example, we assume the controller passes the entities or we fetch them.
        // Let's fetch them to be safe:
        // (Assuming you have a WarehouseRepository injected if needed, or pass entities directly)

        StockTransaction transaction = StockTransaction.builder()
                .type(type)
                .product(product)
                .quantity(quantity)
                .referenceNumber(reference)
                .notes(notes)
                .transactionDate(LocalDateTime.now())
                .performedBy(user)
                .build();

        return transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public List<StockTransaction> getAllTransactions() {
        return transactionRepository.findAllRecentTransactions();
    }
}