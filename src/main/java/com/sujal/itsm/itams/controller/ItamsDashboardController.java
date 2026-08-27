package com.sujal.itsm.itams.controller;

import com.sujal.itsm.itams.enums.AssetStatus;
import com.sujal.itsm.itams.model.Asset;
import com.sujal.itsm.itams.model.AssetCategory;
import com.sujal.itsm.itams.model.Product;
import com.sujal.itsm.itams.repository.AssetRepository;
import com.sujal.itsm.itams.repository.ProductRepository;
import com.sujal.itsm.itams.service.AssetCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/itams/dashboard")
@RequiredArgsConstructor
@Slf4j
public class ItamsDashboardController {

    private final AssetRepository assetRepository;
    private final ProductRepository productRepository;
    private final AssetCategoryService categoryService;

    @GetMapping
    public String showDashboard(Model model) {
        log.info("📊 Loading ITAMS Dashboard...");

        try {
            // Asset KPIs
            long totalAssets = assetRepository.count();
            long availableAssets = assetRepository.countByStatus(AssetStatus.AVAILABLE);
            long assignedAssets = assetRepository.countByStatus(AssetStatus.ASSIGNED);
            long inRepairAssets = assetRepository.countByStatus(AssetStatus.IN_REPAIR);
            long retiredAssets = assetRepository.countByStatus(AssetStatus.RETIRED);

            // Expiring warranties (next 30 days)
            LocalDate today = LocalDate.now();
            LocalDate futureDate = today.plusDays(30);
            List<Asset> expiringAssets = assetRepository.findAssetsExpiringWithin(today, futureDate);

            // Recent assets (last 5)
            List<Asset> recentAssets = assetRepository.findAll().stream()
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .limit(5)
                    .toList();

            // Low stock products
            List<Product> lowStockProducts = productRepository.findLowStockProducts();

            // Category distribution for chart
            List<Object[]> categoryStats = assetRepository.countAssetsByCategory();
            Map<String, Long> categoryDistribution = new HashMap<>();
            for (Object[] row : categoryStats) {
                String categoryName = row[0] != null ? row[0].toString() : "Uncategorized";
                Long count = (Long) row[1];
                categoryDistribution.put(categoryName, count);
            }

            model.addAttribute("totalAssets", totalAssets);
            model.addAttribute("availableAssets", availableAssets);
            model.addAttribute("assignedAssets", assignedAssets);
            model.addAttribute("inRepairAssets", inRepairAssets);
            model.addAttribute("retiredAssets", retiredAssets);
            model.addAttribute("expiringAssets", expiringAssets);
            model.addAttribute("expiringCount", expiringAssets.size());
            model.addAttribute("recentAssets", recentAssets);
            model.addAttribute("lowStockProducts", lowStockProducts);
            model.addAttribute("categoryDistribution", categoryDistribution);
            model.addAttribute("pageTitle", "ITAMS Dashboard");

            log.info("✅ ITAMS Dashboard loaded successfully");
            return "itams/dashboard";

        } catch (Exception e) {
            log.error("❌ Error loading ITAMS Dashboard: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to load dashboard: " + e.getMessage());
            return "itams/dashboard";
        }
    }
}