package com.sujal.itsm.itams.service;

import com.sujal.itsm.itams.model.Product;
import com.sujal.itsm.itams.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> findAllActive() {
        return productRepository.findByIsActiveTrueOrderByNameAsc();
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
    }

    public Product create(Product product) {
        return productRepository.save(product);
    }

    public Product update(Long id, Product details) {
        Product product = findById(id);
        product.setName(details.getName());
        product.setSku(details.getSku());
        product.setBarcode(details.getBarcode());
        product.setModelNumber(details.getModelNumber());
        product.setCategory(details.getCategory());
        product.setBrand(details.getBrand());
        product.setSupplier(details.getSupplier());
        product.setUnit(details.getUnit());
        product.setCurrentStock(details.getCurrentStock());
        product.setMinStockLevel(details.getMinStockLevel());
        product.setUnitPrice(details.getUnitPrice());
        product.setDescription(details.getDescription());
        product.setIsActive(details.getIsActive());
        return productRepository.save(product);
    }

    public void delete(Long id) {
        Product product = findById(id);
        product.setIsActive(false);
        productRepository.save(product);
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }
}