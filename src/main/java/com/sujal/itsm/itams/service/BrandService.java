package com.sujal.itsm.itams.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sujal.itsm.itams.model.Brand;
import com.sujal.itsm.itams.repository.BrandRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BrandService {

  private final BrandRepository brandRepository;

  public List<Brand> findAll() {
    return brandRepository.findAllOrderByname();
  }

  public List<Brand> findAllActive() {
    return brandRepository.findAllActive();
  }

  public Brand findById(Long id) {
    return brandRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Brand not found with id: " + id));
  }

  public Brand findByName(String name) {
    return brandRepository
        .findByName(name)
        .orElseThrow(() -> new EntityNotFoundException("Brand not found: " + name));
  }

  public Brand create(Brand brand) {
    if (brandRepository.existsByName(brand.getName())) {
      throw new IllegalArgumentException("Brand name already exists: " + brand.getName());
    }
    return brandRepository.save(brand);
  }

  public Brand update(Long id, Brand brandDetails) {
    Brand brand = findById(id);

    // Check if name is changed and already exists
    if (!brand.getName().equals(brandDetails.getName())
        && brandRepository.existsByName(brandDetails.getName())) {
      throw new IllegalArgumentException("Brand name already exists: " + brandDetails.getName());
    }

    brand.setName(brandDetails.getName());
    brand.setDescription(brandDetails.getDescription());
    brand.setLogoUrl(brandDetails.getLogoUrl());
    brand.setWebsite(brandDetails.getWebsite());
    brand.setIsActive(brandDetails.getIsActive());

    return brandRepository.save(brand);
  }

  public void delete(Long id) {
    Brand brand = findById(id);
    brand.setIsActive(false);
    brandRepository.save(brand);
  }

  public void hardDelete(Long id) {
    if (!brandRepository.existsById(id)) {
      throw new EntityNotFoundException("Brand not found with id: " + id);
    }
    brandRepository.deleteById(id);
  }

  public long count() {
    return brandRepository.count();
  }

  public long countActive() {
    return brandRepository.countByIsActive(true);
  }
}
