package com.sujal.itsm.core.dashboard.controller;

import com.sujal.itsm.core.dashboard.dto.ChartDataset;
import com.sujal.itsm.core.dashboard.dto.DashboardKpiResponse;
import com.sujal.itsm.core.dashboard.service.DashboardAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/executive")
@RequiredArgsConstructor
public class ExecutiveDashboardController {

    private final DashboardAnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("pageTitle", "Executive Dashboard");
        return "executive/dashboard";
    }

    @GetMapping("/api/kpis")
    @ResponseBody
    public ResponseEntity<DashboardKpiResponse> getKpis() {
        return ResponseEntity.ok(analyticsService.getExecutiveKpis());
    }

    @GetMapping("/api/charts/assets")
    @ResponseBody
    public ResponseEntity<ChartDataset> getAssetCharts() {
        return ResponseEntity.ok(analyticsService.getAssetCategoryDistribution());
    }
}