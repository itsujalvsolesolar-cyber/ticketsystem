package com.sujal.itsm.itams.controller;

import com.sujal.itsm.itams.model.Asset;
import com.sujal.itsm.itams.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/itams/assets")
@RequiredArgsConstructor
@Slf4j
public class AssetLabelController {

    private final AssetRepository assetRepository;

    @GetMapping("/scanner")
    public String showScanner() {
        return "itams/assets/scanner";
    }

    @GetMapping("/scan")
    public String processScan(@RequestParam String assetId, RedirectAttributes redirectAttributes) {
        try {
            // 1. Try to parse as Long (Numeric ID)
            Optional<Asset> assetOpt = assetRepository.findById(Long.parseLong(assetId));

            // 2. If not found by ID, try to find by Asset Tag or Serial Number
            if (assetOpt.isEmpty()) {
                // ✅ USE THE CORRECT METHOD NAME HERE
                assetOpt = assetRepository.findByAssetTagOrSerialNumber(assetId, assetId);
            }

            Asset asset = assetOpt.orElseThrow(() -> new RuntimeException("Asset not found"));

            // Redirect to the asset details page
            return "redirect:/itams/assets/" + asset.getId() + "/view";

        } catch (NumberFormatException e) {
            // If it's not a number, it's likely an Asset Tag or Serial Number, try direct lookup
            try {
                // ✅ USE THE CORRECT METHOD NAME HERE
                Asset asset = assetRepository.findByAssetTagOrSerialNumber(assetId, assetId)
                        .orElseThrow(() -> new RuntimeException("Asset not found"));
                return "redirect:/itams/assets/" + asset.getId() + "/view";
            } catch (Exception ex) {
                log.warn("❌ Scan failed for ID: {}", assetId);
                redirectAttributes.addFlashAttribute("error", "Asset with ID '" + assetId + "' not found.");
                return "redirect:/itams/assets/scanner";
            }
        } catch (Exception e) {
            log.warn("❌ Scan failed for ID: {}", assetId);
            redirectAttributes.addFlashAttribute("error", "Asset with ID '" + assetId + "' not found.");
            return "redirect:/itams/assets/scanner";
        }
    }

    @GetMapping("/label-designer")
    public String showLabelDesigner(@RequestParam(required = false) Long assetId, Model model) {
        if (assetId != null) {
            Asset asset = assetRepository.findById(assetId)
                    .orElseThrow(() -> new RuntimeException("Asset not found"));
            model.addAttribute("asset", asset);
        } else {
            // Provide dummy data for preview if no asset is selected
            Asset dummyAsset = new Asset();
            dummyAsset.setName("MacBook Pro 16\"");
            dummyAsset.setSerialNumber("C02X1234ABCD");
            // ✅ Removed setAssetTagId to prevent compilation errors

            model.addAttribute("asset", dummyAsset);
        }

        model.addAttribute("pageTitle", "Label Designer");
        return "itams/assets/label-designer";
    }

    @GetMapping("/qr-sticker-designer")
    public String showQRStickerDesigner(@RequestParam(required = false) Long assetId, Model model) {
        // Get all assets for the dropdown
        List<Asset> assets = assetRepository.findAll();
        model.addAttribute("assets", assets);

        // If asset is selected, add it to the model
        if (assetId != null) {
            Asset selectedAsset = assetRepository.findById(assetId)
                    .orElseThrow(() -> new RuntimeException("Asset not found"));
            model.addAttribute("selectedAsset", selectedAsset);
        }

        model.addAttribute("pageTitle", "QR Sticker Designer");
        return "itams/assets/qr-sticker-designer";
    }
}