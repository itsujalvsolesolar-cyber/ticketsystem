package com.sujal.itsm.itams.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sujal.itsm.core.security.CurrentUserService;
import com.sujal.itsm.itams.enums.AssetStatus;
import com.sujal.itsm.itams.model.Asset;
import com.sujal.itsm.itams.model.AssetCategory;
import com.sujal.itsm.itams.repository.AssetRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AssetService {

  private final AssetRepository assetRepository;
  private final AssetCategoryService categoryService;
  private final CurrentUserService currentUserService;
  private final QrCodeService qrCodeService;

  public Page<Asset> findAll(Pageable pageable) {
    return assetRepository.findAllActive(pageable);
  }

  public List<Asset> findAll() {
    return assetRepository.findAll();
  }

  public Asset findById(Long id) {
    return assetRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Asset not found with id: " + id));
  }

  public Asset findByAssetTag(String assetTag) {
    return assetRepository
        .findByAssetTag(assetTag)
        .orElseThrow(() -> new EntityNotFoundException("Asset not found with tag: " + assetTag));
  }

  @Transactional
  public Asset create(Asset asset) {
    // Generate asset tag if not provided
    if (asset.getAssetTag() == null || asset.getAssetTag().isBlank()) {
      asset.setAssetTag(generateAssetTag(asset.getCategory()));
    }

    // Validate unique serial number
    if (asset.getSerialNumber() != null && !asset.getSerialNumber().isBlank()) {
      if (assetRepository.existsBySerialNumber(asset.getSerialNumber())) {
        throw new IllegalArgumentException(
            "Serial number already exists: " + asset.getSerialNumber());
      }
    }

    // Set created by
    asset.setCreatedBy(currentUserService.getCurrentUser());

    // Save asset
    Asset savedAsset = assetRepository.save(asset);

    // Generate QR code
    try {
      String qrCodeUrl = qrCodeService.generateQrCode(savedAsset);
      savedAsset.setQrCodeUrl(qrCodeUrl);
      assetRepository.save(savedAsset);
    } catch (Exception e) {
      log.error("Failed to generate QR code for asset: {}", savedAsset.getAssetTag(), e);
    }

    log.info("Created asset: {}", savedAsset.getAssetTag());
    return savedAsset;
  }

  @Transactional
  public Asset update(Long id, Asset assetDetails) {
    Asset asset = findById(id);

    // Validate unique serial number if changed
    if (assetDetails.getSerialNumber() != null
        && !assetDetails.getSerialNumber().equals(asset.getSerialNumber())) {
      if (assetRepository.existsBySerialNumber(assetDetails.getSerialNumber())) {
        throw new IllegalArgumentException(
            "Serial number already exists: " + assetDetails.getSerialNumber());
      }
    }

    // Update fields
    asset.setSerialNumber(assetDetails.getSerialNumber());
    asset.setName(assetDetails.getName());
    asset.setDescription(assetDetails.getDescription());
    asset.setCategory(assetDetails.getCategory());
    asset.setBrand(assetDetails.getBrand());
    asset.setSupplier(assetDetails.getSupplier());
    asset.setPurchaseDate(assetDetails.getPurchaseDate());
    asset.setPurchasePrice(assetDetails.getPurchasePrice());
    asset.setInvoiceNumber(assetDetails.getInvoiceNumber());
    asset.setPoNumber(assetDetails.getPoNumber());
    asset.setWarrantyStartDate(assetDetails.getWarrantyStartDate());
    asset.setWarrantyEndDate(assetDetails.getWarrantyEndDate());
    asset.setAmcEndDate(assetDetails.getAmcEndDate());
    asset.setStatus(assetDetails.getStatus());
    asset.setCondition(assetDetails.getCondition());
    asset.setLocation(assetDetails.getLocation());
    asset.setNotes(assetDetails.getNotes());
    asset.setUpdatedBy(currentUserService.getCurrentUser());

    Asset updatedAsset = assetRepository.save(asset);
    log.info("Updated asset: {}", updatedAsset.getAssetTag());
    return updatedAsset;
  }

  @Transactional
  public void delete(Long id) {
    Asset asset = findById(id);
    asset.setIsActive(false);
    asset.setUpdatedBy(currentUserService.getCurrentUser());
    assetRepository.save(asset);
    log.info("Deactivated asset: {}", asset.getAssetTag());
  }

  @Transactional
  public void changeStatus(Long id, AssetStatus newStatus) {
    Asset asset = findById(id);
    AssetStatus oldStatus = asset.getStatus();
    asset.setStatus(newStatus);
    asset.setUpdatedBy(currentUserService.getCurrentUser());
    assetRepository.save(asset);
    log.info("Changed asset {} status from {} to {}", asset.getAssetTag(), oldStatus, newStatus);
  }

  public long count() {
    return assetRepository.count();
  }

  public long countByStatus(AssetStatus status) {
    return assetRepository.countByStatus(status);
  }

  public long countAvailable() {
    return assetRepository.countByStatus(AssetStatus.AVAILABLE);
  }

  public long countAssigned() {
    return assetRepository.countByStatus(AssetStatus.ASSIGNED);
  }

  public List<Asset> findAssetsWithExpiringWarranty(int days) {
    return assetRepository.findAssetsWithExpiringWarranty(LocalDate.now().plusDays(days));
  }

  /**
   * Generate unique asset tag based on category prefix Format: PREFIX-XXXX (e.g., LT-0001, DS-0002)
   */
  private String generateAssetTag(AssetCategory category) {
    String prefix = category.getPrefix();

    // Find the highest numbered asset in this category
    Optional<Asset> latestAsset = assetRepository.findLatestByCategory(category);

    int nextNumber = 1;
    if (latestAsset.isPresent()) {
      String latestTag = latestAsset.get().getAssetTag();
      // Extract number from tag (e.g., "LT-0001" -> 1)
      String[] parts = latestTag.split("-");
      if (parts.length == 2) {
        try {
          nextNumber = Integer.parseInt(parts[1]) + 1;
        } catch (NumberFormatException e) {
          log.warn("Could not parse asset tag number: {}", latestTag);
        }
      }
    }

    // Format: PREFIX-0001
    return String.format("%s-%04d", prefix, nextNumber);
  }
}
