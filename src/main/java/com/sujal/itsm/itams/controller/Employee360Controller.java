package com.sujal.itsm.itams.controller;

import com.sujal.itsm.itams.model.Employee;
import com.sujal.itsm.itams.repository.AssetAllocationRepository;
import com.sujal.itsm.itams.repository.DigitalAccessRepository;
import com.sujal.itsm.itams.repository.EmployeeRepository;
import com.sujal.itsm.itams.repository.SoftwareLicenseRepository;
import com.sujal.itsm.ticketing.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/itams/employees")
@RequiredArgsConstructor
public class Employee360Controller {

    private final EmployeeRepository employeeRepository;
    private final AssetAllocationRepository allocationRepository;
    private final TicketRepository ticketRepository;
    private final DigitalAccessRepository digitalAccessRepository;
    private final SoftwareLicenseRepository softwareLicenseRepository;

    @GetMapping("/{id}/360")
    @PreAuthorize("hasAnyRole('IT_EXECUTIVE', 'IT_MANAGER', 'SUPER_ADMIN')")
    public String view360Profile(@PathVariable Long id, Model model) {
        // Fetch employee with User and Department eagerly loaded
        Employee employee = employeeRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // 1. Hardware Assets
        var allocations = allocationRepository.findByEmployee_Id(id);
        
        // 2. Support Tickets (Match by username or full name)
        String username = (employee.getUser() != null) ? employee.getUser().getUsername() : "";
        var tickets = ticketRepository.findByUsernameOrFullName(username, employee.getFullName());
        
        // 3. IAM & Digital Access
        var digitalAccess = digitalAccessRepository.findByEmployee_Id(id);
        
        // 4. Software Licenses
        var licenses = softwareLicenseRepository.findByEmployee_Id(id);

        model.addAttribute("employee", employee);
        model.addAttribute("allocations", allocations);
        model.addAttribute("tickets", tickets);
        model.addAttribute("digitalAccess", digitalAccess);
        model.addAttribute("licenses", licenses);
        
        model.addAttribute("pageTitle", employee.getFullName() + " - 360° Profile");
        return "itams/employees/360";
    }
}