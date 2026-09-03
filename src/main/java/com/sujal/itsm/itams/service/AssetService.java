package com.sujal.itsm.itams.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sujal.itsm.core.security.CurrentUserService;
import com.sujal.itsm.itams.enums.AssetStatus;
import com.sujal.itsm.itams.model.Asset;
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
  private final CurrentUserService currentUserService;
  private final QrCodeService qrCodeService;
  private final AssetTagService assetTagService; // Injected tag generator

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
    // 1. Global identifier: assigned once, decoupled from category/brand/serial
    if (asset.getAssetTag() == null || asset.getAssetTag().isBlank()) {
      asset.setAssetTag(assetTagService.nextAssetTag());
    }

    // 2. Default status fallback (IN_STOCK / AVAILABLE)
    if (asset.getStatus() == null) {
      asset.setStatus(AssetStatus.AVAILABLE);
    }

    // 3. Prevent duplicate hardware entries
    if (asset.getSerialNumber() != null && !asset.getSerialNumber().isBlank()) {
      if (assetRepository.existsBySerialNumber(asset.getSerialNumber())) {
        throw new IllegalArgumentException(
            "Serial number already exists: " + asset.getSerialNumber());
      }
    }

    // 4. Audit trail
    asset.setCreatedBy(currentUserService.getCurrentUser());

    // 5. Persist entity
    Asset savedAsset = assetRepository.save(asset);

    // 6. Generate QR code link
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
}