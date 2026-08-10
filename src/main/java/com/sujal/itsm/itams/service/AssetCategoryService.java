package com.sujal.itsm.itams.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sujal.itsm.itams.model.AssetCategory;
import com.sujal.itsm.itams.repository.AssetCategoryRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AssetCategoryService {

  private final AssetCategoryRepository categoryRepository;

  public List<AssetCategory> findAll() {
    return categoryRepository.findAllOrderByname();
  }

  public List<AssetCategory> findAllActive() {
    return categoryRepository.findAllActive();
  }

  public AssetCategory findById(Long id) {
    return categoryRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Asset Category not found with id: " + id));
  }

  public AssetCategory findByName(String name) {
    return categoryRepository
        .findByName(name)
        .orElseThrow(() -> new EntityNotFoundException("Asset Category not found: " + name));
  }

  public AssetCategory create(AssetCategory category) {
    if (categoryRepository.existsByName(category.getName())) {
      throw new IllegalArgumentException("Category name already exists: " + category.getName());
    }
    if (categoryRepository.existsByPrefix(category.getPrefix())) {
      throw new IllegalArgumentException("Category prefix already exists: " + category.getPrefix());
    }
    return categoryRepository.save(category);
  }

  public AssetCategory update(Long id, AssetCategory categoryDetails) {
    AssetCategory category = findById(id);

    // Check if name is changed and already exists
    if (!category.getName().equals(categoryDetails.getName())
        && categoryRepository.existsByName(categoryDetails.getName())) {
      throw new IllegalArgumentException(
          "Category name already exists: " + categoryDetails.getName());
    }

    // Check if prefix is changed and already exists
    if (!category.getPrefix().equals(categoryDetails.getPrefix())
        && categoryRepository.existsByPrefix(categoryDetails.getPrefix())) {
      throw new IllegalArgumentException(
          "Category prefix already exists: " + categoryDetails.getPrefix());
    }

    category.setName(categoryDetails.getName());
    category.setDescription(categoryDetails.getDescription());
    category.setPrefix(categoryDetails.getPrefix());
    category.setDefaultWarrantyMonths(categoryDetails.getDefaultWarrantyMonths());
    category.setIsActive(categoryDetails.getIsActive());

    return categoryRepository.save(category);
  }

  public void delete(Long id) {
    AssetCategory category = findById(id);
    category.setIsActive(false);
    categoryRepository.save(category);
  }

  public void hardDelete(Long id) {
    if (!categoryRepository.existsById(id)) {
      throw new EntityNotFoundException("Asset Category not found with id: " + id);
    }
    categoryRepository.deleteById(id);
  }

  public long count() {
    return categoryRepository.count();
  }

  public long countActive() {
    return categoryRepository.countByIsActive(true);
  }
}
