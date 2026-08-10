package com.sujal.itsm.itams.controller;

import com.sujal.itsm.itams.model.Employee;
import com.sujal.itsm.itams.model.SoftwareCatalog;
import com.sujal.itsm.itams.model.SoftwareLicense;
import com.sujal.itsm.itams.service.EmployeeService;
import com.sujal.itsm.itams.service.SoftwareCatalogService;
import com.sujal.itsm.itams.service.SoftwareLicenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/itams/software")
@RequiredArgsConstructor
public class SoftwareLicenseController {

    private final SoftwareLicenseService licenseService;
    private final EmployeeService employeeService;
    private final SoftwareCatalogService catalogService; // ✅ MUST BE HERE

    @GetMapping
    public String index(Model model) {
        model.addAttribute("licenses", licenseService.findAllActive());
        model.addAttribute("pageTitle", "Software Licenses");
        return "itams/software/index";
    }

    @GetMapping("/new")
    public String showForm(Model model) {
        // ✅ These two lines fetch the data for the dropdowns
        List<Employee> employees = employeeService.findAllActive();
        List<SoftwareCatalog> catalogs = catalogService.findAllActive(); 
        
        model.addAttribute("employees", employees);
        model.addAttribute("catalogs", catalogs); 
        model.addAttribute("license", new SoftwareLicense());
        model.addAttribute("pageTitle", "Assign Software License");
        return "itams/software/form";
    }

    @PostMapping
    public String create(@ModelAttribute SoftwareLicense license, 
                         @RequestParam Long employeeId,
                         @RequestParam Long catalogId, 
                         RedirectAttributes redirectAttributes) {
        
        Employee employee = employeeService.findById(employeeId);
        SoftwareCatalog catalog = catalogService.findById(catalogId); 
        
        license.setEmployee(employee);
        license.setSoftwareCatalog(catalog); 
        
        licenseService.create(license);
        redirectAttributes.addFlashAttribute("success", "Software license assigned successfully!");
        return "redirect:/itams/software";
    }

    @PostMapping("/{id}/revoke")
    public String revoke(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        licenseService.revoke(id);
        redirectAttributes.addFlashAttribute("success", "Software license revoked successfully!");
        return "redirect:/itams/software";
    }
}