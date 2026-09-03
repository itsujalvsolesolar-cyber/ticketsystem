package com.sujal.itsm.itams.controller;

import com.sujal.itsm.core.user.repository.DepartmentRepository;
import com.sujal.itsm.itams.dto.EmployeeOnboardingDTO;
import com.sujal.itsm.itams.model.Employee;
import com.sujal.itsm.itams.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.SecureRandom;
import java.util.Base64;

@Controller
@RequestMapping("/itams/employees")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController {

    private final EmployeeService employeeService;
    private final DepartmentRepository departmentRepository;

    // =========================================================================
    // 1. DIRECTORY VIEW
    // Accessible by IT Staff, Executives, Managers, and Admins
    // =========================================================================
    @GetMapping
    @PreAuthorize("hasAnyRole('IT_EXECUTIVE', 'IT_MANAGER', 'SUPER_ADMIN', 'ADMIN', 'STAFF')")
    public String list(Model model) {
        model.addAttribute("employees", employeeService.findAllActive());
        model.addAttribute("pageTitle", "Workforce Directory");
        return "itams/employees/index";
    }

    // =========================================================================
    // 2. ONBOARDING (HR / IT PROVISIONING BOUNDARY)
    // Accessible by IT Managers, Super Admins, and Admins
    // =========================================================================
    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('IT_MANAGER', 'SUPER_ADMIN', 'ADMIN')")
    public String showCreateForm(Model model) {
        String tempPassword = generateSecurePassword();
        
        EmployeeOnboardingDTO dto = new EmployeeOnboardingDTO();
        dto.setTemporaryPassword(tempPassword);
        
        model.addAttribute("dto", dto);
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("pageTitle", "Onboard New Employee");
        return "itams/employees/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('IT_MANAGER', 'SUPER_ADMIN', 'ADMIN')")
    public String create(@Valid @ModelAttribute("dto") EmployeeOnboardingDTO dto, 
                         BindingResult result, 
                         Model model, 
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("departments", departmentRepository.findAll());
            return "itams/employees/form";
        }
        
        try {
            employeeService.onboardEmployee(dto);
            redirectAttributes.addFlashAttribute("success", "Employee onboarded and IT account provisioned successfully!");
            return "redirect:/itams/employees";
        } catch (Exception e) {
            log.error("❌ Provisioning failed", e);
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("error", "Provisioning failed: " + e.getMessage());
            return "itams/employees/form";
        }
    }

    // =========================================================================
    // 3. PROFILE MODIFICATION
    // Accessible by IT Managers, Super Admins, and Admins
    // =========================================================================
    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('IT_MANAGER', 'SUPER_ADMIN', 'ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model) {
        Employee employee = employeeService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + id));
        
        model.addAttribute("employee", employee);
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("pageTitle", "Edit Employee Profile");
        return "itams/employees/edit";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('IT_MANAGER', 'SUPER_ADMIN', 'ADMIN')")
    public String update(@PathVariable Long id, 
                         @Valid @ModelAttribute("employee") Employee employee, 
                         BindingResult result, 
                         Model model, 
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("departments", departmentRepository.findAll());
            return "itams/employees/edit";
        }
        
        try {
            employeeService.update(id, employee);
            redirectAttributes.addFlashAttribute("success", "Employee profile updated successfully!");
            return "redirect:/itams/employees";
        } catch (Exception e) {
            log.error("❌ Update failed", e);
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("error", "Update failed: " + e.getMessage());
            return "itams/employees/edit";
        }
    }

    // =========================================================================
    // 4. LEAVER WORKFLOW (DEACTIVATION / REVOCATION)
    // Strictly restricted to Super Admins and System Admins
    // =========================================================================
    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            employeeService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Employee deactivated and IT access revoked successfully!");
        } catch (Exception e) {
            log.error("❌ Deactivation failed", e);
            redirectAttributes.addFlashAttribute("error", "Failed to deactivate employee: " + e.getMessage());
        }
        return "redirect:/itams/employees";
    }

    // =========================================================================
    // HELPER: SECURE PASSWORD GENERATION
    // =========================================================================
    private String generateSecurePassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[12];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes) + "Aa1!";
    }
}