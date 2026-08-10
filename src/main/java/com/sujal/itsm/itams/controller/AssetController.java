package com.sujal.itsm.itams.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sujal.itsm.itams.enums.AssetCondition;
import com.sujal.itsm.itams.enums.AssetStatus;
import com.sujal.itsm.itams.model.Asset;
import com.sujal.itsm.itams.service.AssetCategoryService;
import com.sujal.itsm.itams.service.AssetService;
import com.sujal.itsm.itams.service.BrandService;
import com.sujal.itsm.itams.service.QrCodeService;
import com.sujal.itsm.itams.service.SupplierService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/itams/assets")
@RequiredArgsConstructor
public class AssetController {

  private final AssetService assetService;
  private final AssetCategoryService categoryService;
  private final BrandService brandService;
  private final SupplierService supplierService;
  private final QrCodeService qrCodeService;

  // ============================================
  // LIST ASSETS
  // ============================================
  @GetMapping
  public String list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir,
      Model model) {

    Sort sort =
        sortDir.equalsIgnoreCase("asc")
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();
    PageRequest pageRequest = PageRequest.of(page, size, sort);
    Page<Asset> assets = assetService.findAll(pageRequest);

    model.addAttribute("assets", assets);
    model.addAttribute("currentPage", page);
    model.addAttribute("totalPages", assets.getTotalPages());
    model.addAttribute("totalItems", assets.getTotalElements());
    model.addAttribute("sortBy", sortBy);
    model.addAttribute("sortDir", sortDir);
    model.addAttribute("pageSize", size);

    // Statistics
    model.addAttribute("totalAssets", assetService.count());
    model.addAttribute("availableAssets", assetService.countAvailable());
    model.addAttribute("assignedAssets", assetService.countAssigned());

    return "itams/assets/index";
  }

  // ============================================
  // CREATE ASSET FORM
  // ============================================
  @GetMapping("/new")
  public String showCreateForm(Model model) {
    model.addAttribute("asset", new Asset());
    model.addAttribute("categories", categoryService.findAllActive());
    model.addAttribute("brands", brandService.findAllActive());
    model.addAttribute("suppliers", supplierService.findAllActive());
    model.addAttribute("statuses", AssetStatus.values());
    model.addAttribute("conditions", AssetCondition.values());
    model.addAttribute("pageTitle", "New Asset");
    return "itams/assets/form";
  }

  // ============================================
  // CREATE ASSET (POST) - ONLY ONE METHOD
  // ============================================
  @PostMapping
  public String create(
      @Valid @ModelAttribute("asset") Asset asset,
      BindingResult result,
      @RequestParam("categoryId") Long categoryId,
      @RequestParam(value = "brandId", required = false) Long brandId,
      @RequestParam(value = "supplierId", required = false) Long supplierId,
      RedirectAttributes redirectAttributes,
      Model model) {

    System.out.println("=== CREATE ASSET CALLED ===");
    System.out.println("Asset name: " + asset.getName());
    System.out.println("Category ID: " + categoryId);
    System.out.println("Has errors: " + result.hasErrors());

    if (result.hasErrors()) {
      System.out.println("Validation errors: " + result.getAllErrors());
    }

    // Manually set the entities from IDs
    if (categoryId != null) {
      asset.setCategory(categoryService.findById(categoryId));
    }
    if (brandId != null) {
      asset.setBrand(brandService.findById(brandId));
    }
    if (supplierId != null) {
      asset.setSupplier(supplierService.findById(supplierId));
    }

    if (result.hasErrors()) {
      model.addAttribute("categories", categoryService.findAllActive());
      model.addAttribute("brands", brandService.findAllActive());
      model.addAttribute("suppliers", supplierService.findAllActive());
      model.addAttribute("statuses", AssetStatus.values());
      model.addAttribute("conditions", AssetCondition.values());
      model.addAttribute("pageTitle", "New Asset");
      return "itams/assets/form";
    }

    try {
      Asset createdAsset = assetService.create(asset);
      redirectAttributes.addFlashAttribute(
          "success", "Asset created successfully! Tag: " + createdAsset.getAssetTag());
      return "redirect:/itams/assets";
    } catch (IllegalArgumentException e) {
      model.addAttribute("error", e.getMessage());
      model.addAttribute("categories", categoryService.findAllActive());
      model.addAttribute("brands", brandService.findAllActive());
      model.addAttribute("suppliers", supplierService.findAllActive());
      model.addAttribute("statuses", AssetStatus.values());
      model.addAttribute("conditions", AssetCondition.values());
      model.addAttribute("pageTitle", "New Asset");
      return "itams/assets/form";
    } catch (Exception e) {
      model.addAttribute("error", "Failed to create asset: " + e.getMessage());
      model.addAttribute("categories", categoryService.findAllActive());
      model.addAttribute("brands", brandService.findAllActive());
      model.addAttribute("suppliers", supplierService.findAllActive());
      model.addAttribute("statuses", AssetStatus.values());
      model.addAttribute("conditions", AssetCondition.values());
      model.addAttribute("pageTitle", "New Asset");
      return "itams/assets/form";
    }
  }

  // ============================================
  // VIEW ASSET DETAILS
  // ============================================
  @GetMapping("/{id}")
  public String view(@PathVariable Long id, Model model) {
    Asset asset = assetService.findById(id);
    model.addAttribute("asset", asset);
    model.addAttribute("pageTitle", "Asset Details - " + asset.getAssetTag());
    return "itams/assets/view";
  }

  // ============================================
  // EDIT ASSET FORM
  // ============================================
  @GetMapping("/{id}/edit")
  public String showEditForm(@PathVariable Long id, Model model) {
    Asset asset = assetService.findById(id);
    model.addAttribute("asset", asset);
    model.addAttribute("categories", categoryService.findAllActive());
    model.addAttribute("brands", brandService.findAllActive());
    model.addAttribute("suppliers", supplierService.findAllActive());
    model.addAttribute("statuses", AssetStatus.values());
    model.addAttribute("conditions", AssetCondition.values());
    model.addAttribute("pageTitle", "Edit Asset");
    return "itams/assets/form";
  }

  // ============================================
  // UPDATE ASSET (POST) - ONLY ONE METHOD
  // ============================================
  @PostMapping("/{id}")
  public String update(
      @PathVariable Long id,
      @Valid @ModelAttribute("asset") Asset asset,
      BindingResult result,
      @RequestParam("categoryId") Long categoryId,
      @RequestParam(value = "brandId", required = false) Long brandId,
      @RequestParam(value = "supplierId", required = false) Long supplierId,
      RedirectAttributes redirectAttributes,
      Model model) {

    // Manually set the entities from IDs
    if (categoryId != null) {
      asset.setCategory(categoryService.findById(categoryId));
    }
    if (brandId != null) {
      asset.setBrand(brandService.findById(brandId));
    }
    if (supplierId != null) {
      asset.setSupplier(supplierService.findById(supplierId));
    }

    if (result.hasErrors()) {
      model.addAttribute("categories", categoryService.findAllActive());
      model.addAttribute("brands", brandService.findAllActive());
      model.addAttribute("suppliers", supplierService.findAllActive());
      model.addAttribute("statuses", AssetStatus.values());
      model.addAttribute("conditions", AssetCondition.values());
      model.addAttribute("pageTitle", "Edit Asset");
      return "itams/assets/form";
    }

    try {
      assetService.update(id, asset);
      redirectAttributes.addFlashAttribute("success", "Asset updated successfully!");
      return "redirect:/itams/assets/" + id;
    } catch (IllegalArgumentException e) {
      model.addAttribute("error", e.getMessage());
      model.addAttribute("categories", categoryService.findAllActive());
      model.addAttribute("brands", brandService.findAllActive());
      model.addAttribute("suppliers", supplierService.findAllActive());
      model.addAttribute("statuses", AssetStatus.values());
      model.addAttribute("conditions", AssetCondition.values());
      model.addAttribute("pageTitle", "Edit Asset");
      return "itams/assets/form";
    }
  }

  // ============================================
  // DELETE (DEACTIVATE) ASSET
  // ============================================
  @PostMapping("/{id}/delete")
  public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
      assetService.delete(id);
      redirectAttributes.addFlashAttribute("success", "Asset deactivated successfully!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "error", "Failed to deactivate asset: " + e.getMessage());
    }
    return "redirect:/itams/assets";
  }

  // ============================================
  // CHANGE ASSET STATUS
  // ============================================
  @PostMapping("/{id}/status")
  public String changeStatus(
      @PathVariable Long id, @RequestParam String status, RedirectAttributes redirectAttributes) {
    try {
      AssetStatus newStatus = AssetStatus.valueOf(status);
      assetService.changeStatus(id, newStatus);
      redirectAttributes.addFlashAttribute(
          "success", "Asset status changed to " + newStatus.getDisplayName());
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Failed to change status: " + e.getMessage());
    }
    return "redirect:/itams/assets/" + id;
  }

  // ============================================
  // DOWNLOAD QR CODE
  // ============================================
  @GetMapping("/{id}/qrcode")
  public ResponseEntity<ByteArrayResource> downloadQrCode(@PathVariable Long id)
      throws IOException {
    Asset asset = assetService.findById(id);

    if (asset.getQrCodeUrl() == null) {
      return ResponseEntity.notFound().build();
    }

    byte[] qrCodeBytes = Files.readAllBytes(Paths.get(asset.getQrCodeUrl()));
    ByteArrayResource resource = new ByteArrayResource(qrCodeBytes);

    return ResponseEntity.ok()
        .contentType(MediaType.IMAGE_PNG)
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + asset.getAssetTag() + ".png\"")
        .body(resource);
  }

  @GetMapping("/qr-template-designer")
  public String showQRTemplateDesigner(Model model) {
    model.addAttribute("pageTitle", "QR Template Designer");
    return "itams/assets/qr-template-designer";
  }

  @GetMapping("/qr-canvas-designer")
  public String showQRCanvasDesigner(Model model) {
    model.addAttribute("pageTitle", "QR Canvas Designer");
    return "itams/assets/qr-canvas-designer"; // ✅ Changed to match the actual file name
  }
}
