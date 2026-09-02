package com.sujal.itsm.itams.controller;

import com.sujal.itsm.core.user.repository.DepartmentRepository;
import com.sujal.itsm.itams.dto.EmployeeOnboardingDTO;
import com.sujal.itsm.itams.model.Employee;
import com.sujal.itsm.itams.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @GetMapping
    public String list(Model model) {
        model.addAttribute("employees", employeeService.findAllActive());
        model.addAttribute("pageTitle", "Workforce Directory");
        return "itams/employees/index";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        // Generate a secure random temporary password for the UI
        String tempPassword = generateSecurePassword();
        
        EmployeeOnboardingDTO dto = new EmployeeOnboardingDTO();
        dto.setTemporaryPassword(tempPassword);
        
        model.addAttribute("dto", dto);
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("pageTitle", "Onboard New Employee");
        return "itams/employees/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("dto") EmployeeOnboardingDTO dto, 
                         BindingResult result, Model model, RedirectAttributes redirectAttributes) {
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

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Employee employee = employeeService.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        
        model.addAttribute("employee", employee);
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("pageTitle", "Edit Employee Profile");
        return "itams/employees/edit"; // Ensure you have an edit.html or reuse form.html
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("employee") Employee employee, 
                         BindingResult result, Model model, RedirectAttributes redirectAttributes) {
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

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // This triggers the Leaver workflow (disables AppUser login)
            employeeService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Employee deactivated and IT access revoked successfully!");
        } catch (Exception e) {
            log.error("❌ Deactivation failed", e);
            redirectAttributes.addFlashAttribute("error", "Failed to deactivate employee: " + e.getMessage());
        }
        return "redirect:/itams/employees";
    }

    private String generateSecurePassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[12];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes) + "Aa1!"; // Ensures complexity
    }
}