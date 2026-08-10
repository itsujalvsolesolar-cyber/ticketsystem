package com.sujal.itsm.itams.controller;

import com.sujal.itsm.itams.model.Employee;
import com.sujal.itsm.itams.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/itams/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("employees", employeeService.findAllActive());
        model.addAttribute("pageTitle", "Employees");
        return "itams/employees/index";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("employee", new Employee());
        model.addAttribute("pageTitle", "New Employee");
        return "itams/employees/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("employee") Employee employee, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) return "itams/employees/form";
        employeeService.create(employee);
        redirectAttributes.addFlashAttribute("success", "Employee created successfully!");
        return "redirect:/itams/employees";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("employee", employeeService.findById(id));
        model.addAttribute("pageTitle", "Edit Employee");
        return "itams/employees/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("employee") Employee employee, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) return "itams/employees/form";
        employeeService.update(id, employee);
        redirectAttributes.addFlashAttribute("success", "Employee updated successfully!");
        return "redirect:/itams/employees";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        employeeService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Employee deactivated successfully!");
        return "redirect:/itams/employees";
    }
}