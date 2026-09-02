package com.sujal.itsm.itams.controller;

import com.sujal.itsm.itams.enums.AccessType;
import com.sujal.itsm.itams.model.DigitalAccess;
import com.sujal.itsm.itams.model.Employee;
import com.sujal.itsm.itams.service.DigitalAccessService;
import com.sujal.itsm.itams.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/itams/access")
@RequiredArgsConstructor
public class DigitalAccessController {

    private final DigitalAccessService accessService;
    private final EmployeeService employeeService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("accessRecords", accessService.findAllActive());
        model.addAttribute("pageTitle", "Digital Access Management");
        return "itams/access/index";
    }

    @GetMapping("/new")
    public String showForm(Model model) {
        List<Employee> employees = employeeService.findAllActive();
        model.addAttribute("employees", employees);
        model.addAttribute("accessTypes", AccessType.values());
        model.addAttribute("access", new DigitalAccess());
        model.addAttribute("pageTitle", "Grant Digital Access");
        return "itams/access/form";
    }

    @PostMapping
    public String create(@ModelAttribute DigitalAccess access, 
                         @RequestParam Long employeeId,
                         RedirectAttributes redirectAttributes) {

        // FIX: Fetch the target employee selected in the dropdown, NOT the logged-in admin
        Employee employee = employeeService.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));
        
        access.setEmployee(employee);
        accessService.create(access);
        
        redirectAttributes.addFlashAttribute("success", "Digital access granted successfully!");
        return "redirect:/itams/access";
    }

    @PostMapping("/{id}/revoke")
    public String revoke(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        accessService.revoke(id);
        redirectAttributes.addFlashAttribute("success", "Digital access revoked successfully!");
        return "redirect:/itams/access";
    }
}