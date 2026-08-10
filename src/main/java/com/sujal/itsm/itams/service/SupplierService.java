package com.sujal.itsm.itams.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sujal.itsm.itams.model.Supplier;
import com.sujal.itsm.itams.repository.SupplierRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierService {

  private final SupplierRepository supplierRepository;

  public List<Supplier> findAll() {
    return supplierRepository.findAllOrderByName();
  }

  public List<Supplier> findAllActive() {
    return supplierRepository.findAllActive();
  }

  public Supplier findById(Long id) {
    return supplierRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Supplier not found with id: " + id));
  }

  public Supplier findByName(String name) {
    return supplierRepository
        .findByName(name)
        .orElseThrow(() -> new EntityNotFoundException("Supplier not found: " + name));
  }

  public Supplier create(Supplier supplier) {
    if (supplierRepository.existsByName(supplier.getName())) {
      throw new IllegalArgumentException("Supplier name already exists: " + supplier.getName());
    }
    if (supplier.getGstNumber() != null
        && !supplier.getGstNumber().isBlank()
        && supplierRepository.existsByGstNumber(supplier.getGstNumber())) {
      throw new IllegalArgumentException("GST number already exists: " + supplier.getGstNumber());
    }
    return supplierRepository.save(supplier);
  }

  public Supplier update(Long id, Supplier supplierDetails) {
    Supplier supplier = findById(id);

    // Check if name is changed and already exists
    if (!supplier.getName().equals(supplierDetails.getName())
        && supplierRepository.existsByName(supplierDetails.getName())) {
      throw new IllegalArgumentException(
          "Supplier name already exists: " + supplierDetails.getName());
    }

    // Check if GST number is changed and already exists
    if (supplierDetails.getGstNumber() != null
        && !supplierDetails.getGstNumber().isBlank()
        && !supplierDetails.getGstNumber().equals(supplier.getGstNumber())
        && supplierRepository.existsByGstNumber(supplierDetails.getGstNumber())) {
      throw new IllegalArgumentException(
          "GST number already exists: " + supplierDetails.getGstNumber());
    }

    supplier.setName(supplierDetails.getName());
    supplier.setAddress(supplierDetails.getAddress());
    supplier.setPhone(supplierDetails.getPhone());
    supplier.setEmail(supplierDetails.getEmail());
    supplier.setContactPerson(supplierDetails.getContactPerson());
    supplier.setGstNumber(supplierDetails.getGstNumber());
    supplier.setNotes(supplierDetails.getNotes());
    supplier.setIsActive(supplierDetails.getIsActive());

    return supplierRepository.save(supplier);
  }

  public void delete(Long id) {
    Supplier supplier = findById(id);
    supplier.setIsActive(false);
    supplierRepository.save(supplier);
  }

  public void hardDelete(Long id) {
    if (!supplierRepository.existsById(id)) {
      throw new EntityNotFoundException("Supplier not found with id: " + id);
    }
    supplierRepository.deleteById(id);
  }

  public long count() {
    return supplierRepository.count();
  }

  public long countActive() {
    return supplierRepository.countByIsActive(true);
  }
}
