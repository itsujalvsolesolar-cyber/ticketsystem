package com.sujal.itsm.itams.controller;

import com.sujal.itsm.itams.model.Asset;
import com.sujal.itsm.itams.model.Employee;
import com.sujal.itsm.itams.service.AssetAllocationService;
import com.sujal.itsm.itams.service.AssetService;
import com.sujal.itsm.itams.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/itams/allocations")
@RequiredArgsConstructor
public class AssetAllocationController {

    private final AssetAllocationService allocationService;
    private final AssetService assetService;
    private final EmployeeService employeeService; // ✅ Inject EmployeeService

    // Show Allocation Form
    @GetMapping("/new")
    public String showAllocationForm(@RequestParam("assetId") Long assetId, Model model) {
        Asset asset = assetService.findById(assetId);

        // Prevent allocating if already assigned
        if (asset.getStatus().name().equals("ASSIGNED")) {
            return "redirect:/itams/assets/" + assetId + "?error=already_assigned";
        }

        // ✅ Fetch active Employees instead of AppUsers
        List<Employee> employees = employeeService.findAllActive();

        model.addAttribute("asset", asset);
        model.addAttribute("employees", employees);
        model.addAttribute("conditions", com.sujal.itsm.itams.enums.AssetCondition.values());
        model.addAttribute("pageTitle", "Allocate Asset: " + asset.getName());

        return "itams/allocations/new";
    }

    // Process Allocation
    @PostMapping
    public String processAllocation(@RequestParam("assetId") Long assetId,
                                    @RequestParam("employeeId") Long employeeId,
                                    @RequestParam("condition") String condition,
                                    @RequestParam(value = "expectedReturn", required = false) LocalDate expectedReturn,
                                    @RequestParam(value = "notes", required = false) String notes,
                                    RedirectAttributes redirectAttributes) {
        try {
            allocationService.allocateAsset(assetId, employeeId, condition, notes, expectedReturn);
            redirectAttributes.addFlashAttribute("success", "Asset allocated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to allocate asset: " + e.getMessage());
        }
        return "redirect:/itams/assets/" + assetId;
    }

    // Process Return
    @PostMapping("/{id}/return")
    public String processReturn(@PathVariable Long id,
                                @RequestParam("condition") String condition,
                                @RequestParam(value = "notes", required = false) String notes,
                                RedirectAttributes redirectAttributes) {
        try {
            allocationService.returnAsset(id, condition, notes);
            redirectAttributes.addFlashAttribute("success", "Asset returned successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to return asset: " + e.getMessage());
        }
        return "redirect:/itams/assets";
    }
}