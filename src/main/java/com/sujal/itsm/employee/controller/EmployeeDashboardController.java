package com.sujal.itsm.employee.controller;

import com.sujal.itsm.core.security.CurrentUserService;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.core.user.repository.AppUserRepository;
import com.sujal.itsm.itams.enums.AcceptanceStatus;
import com.sujal.itsm.itams.model.AssetAllocation;
import com.sujal.itsm.itams.model.Employee;
import com.sujal.itsm.itams.repository.AssetAllocationRepository;
import com.sujal.itsm.itams.repository.DigitalAccessRepository;
import com.sujal.itsm.itams.repository.EmployeeRepository;
import com.sujal.itsm.ticketing.model.Ticket;
import com.sujal.itsm.ticketing.repository.TicketRepository; // Ensure this exists
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeDashboardController {

    private final EmployeeRepository employeeRepository;
    private final AppUserRepository userRepository;
    private final AssetAllocationRepository allocationRepository;
    private final DigitalAccessRepository digitalAccessRepository;
    private final TicketRepository ticketRepository; // Injected for 16.5
    private final CurrentUserService currentUserService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN', 'STAFF', 'SUPER_ADMIN')")
    public String dashboard(Model model) {
        AppUser currentUser = currentUserService.getCurrentUser();
        String username = currentUser.getUsername();
        Employee employee = employeeRepository.findByUserUsername(username).orElse(null);

        int hour = LocalTime.now().getHour();
        String timeOfDay = (hour < 12) ? "morning" : (hour < 17) ? "afternoon" : "evening";

        // 1. Scoped Assets
        List<AssetAllocation> allocations = (employee != null)
                ? allocationRepository.findByEmployee_Id(employee.getId())
                : Collections.emptyList();

        long pendingAcceptances = allocations.stream()
                .filter(a -> a.getAcceptanceStatus() == AcceptanceStatus.PENDING)
                .count();

        var primaryAsset = allocations.stream()
                .map(AssetAllocation::getAsset)
                .findFirst()
                .orElse(null);

        // 2. Scoped Digital Access
        long nasAccessCount = (employee != null)
                ? digitalAccessRepository.countByEmployee_Id(employee.getId())
                : 0;

        // 3. Scoped Tickets (16.5 Implementation)
        // Fetches tickets where the logged-in user is the requester OR the assignee
        List<Ticket> myTickets = ticketRepository.findTicketsByUsername(username);
        long openTicketsCount = myTickets.stream()
                .filter(t -> !t.getStatus().name().equals("CLOSED") && !t.getStatus().name().equals("RESOLVED"))
                .count();

        // Populate Model
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("employee", employee);
        model.addAttribute("timeOfDay", timeOfDay);
        model.addAttribute("primaryAsset", primaryAsset);
        model.addAttribute("allocations", allocations);
        model.addAttribute("nasAccessCount", nasAccessCount);
        model.addAttribute("pendingAcceptances", pendingAcceptances);
        model.addAttribute("myTickets", myTickets);             // New for 16.5
        model.addAttribute("openTicketsCount", openTicketsCount); // New for 16.5

        return "employee/dashboard";
    }

    @GetMapping("/{id}/details")
    @PreAuthorize("@securityEvaluator.isEmployeeOwner(authentication, #id)")
    public String viewEmployeeDetails(@PathVariable("id") Long id, Model model) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee record not found."));
        model.addAttribute("employee", employee);
        return "employee/details";
    }
}