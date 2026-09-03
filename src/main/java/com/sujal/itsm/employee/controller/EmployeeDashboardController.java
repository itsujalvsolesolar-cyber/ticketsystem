package com.sujal.itsm.employee.controller;

import com.sujal.itsm.core.security.CurrentUserService;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.itams.enums.AcceptanceStatus;
import com.sujal.itsm.itams.model.AssetAllocation;
import com.sujal.itsm.itams.model.Employee;
import com.sujal.itsm.itams.repository.AssetAllocationRepository;
import com.sujal.itsm.itams.repository.DigitalAccessRepository;
import com.sujal.itsm.itams.repository.EmployeeRepository;
import com.sujal.itsm.ticketing.model.Ticket;
import com.sujal.itsm.ticketing.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

/**
 * Phase 16.6 — Context-Aware Employee Portal.
 *
 * SECURITY MODEL (Zero IDOR):
 *  1. Identity is ALWAYS resolved from the SecurityContext — NEVER from request params.
 *  2. Chain: SecurityContext -> AppUser -> Employee -> scoped resources.
 *  3. Detail routes are object-level guarded via @securityEvaluator.
 */
@Controller
@RequestMapping("/employee")
@RequiredArgsConstructor
@Slf4j
public class EmployeeDashboardController {

    private final EmployeeRepository employeeRepository;
    private final AssetAllocationRepository allocationRepository;
    private final DigitalAccessRepository digitalAccessRepository;
    private final TicketRepository ticketRepository;
    private final CurrentUserService currentUserService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN', 'STAFF')")
    public String dashboard(Model model) {
        // (1) Identity from SecurityContext only
        AppUser currentUser = currentUserService.getCurrentUser();

        // (2) AppUser -> Employee (null-safe for pure admins)
        Employee employee = employeeRepository
                .findByUserUsername(currentUser.getUsername())
                .orElse(null);

        int hour = LocalTime.now().getHour();
        String timeOfDay = (hour < 12) ? "morning" : (hour < 17) ? "afternoon" : "evening";

        // (3) Scoped widgets — every query keyed by the authenticated identity
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

        long activeAccessCount = (employee != null)
                ? digitalAccessRepository.countByEmployee_Id(employee.getId())
                : 0;

        List<Ticket> myTickets = ticketRepository.findByUsernameOrFullName(
                currentUser.getUsername(),
                employee != null ? employee.getFullName() : currentUser.getFullName());

        long openTicketsCount = myTickets.stream()
                .filter(t -> t.getStatus() != null
                        && !"RESOLVED".equals(t.getStatus().name())
                        && !"CLOSED".equals(t.getStatus().name()))
                .count();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("employee", employee);
        model.addAttribute("timeOfDay", timeOfDay);
        model.addAttribute("allocations", allocations);
        model.addAttribute("primaryAsset", primaryAsset);
        model.addAttribute("pendingAcceptances", pendingAcceptances);
        model.addAttribute("activeAccessCount", activeAccessCount);
        model.addAttribute("myTickets", myTickets);
        model.addAttribute("openTicketsCount", openTicketsCount);
        model.addAttribute("assignedAssetsCount", (long) allocations.size());

        return "employee/dashboard";
    }

    /** IDOR-proof employee detail (owner or ADMIN/STAFF override only). */
    @GetMapping("/{id}/details")
    @PreAuthorize("@securityEvaluator.isEmployeeOwner(authentication, #id)")
    public String viewEmployeeDetails(@PathVariable("id") Long id, Model model) {
        Employee employee = employeeRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee record not found."));
        model.addAttribute("employee", employee);
        return "employee/details";
    }

    /** IDOR-proof allocation/asset handover view. */
    @GetMapping("/allocations/{allocationId}")
    @PreAuthorize("@securityEvaluator.isAllocationOwner(authentication, #allocationId)")
    public String viewMyAllocation(@PathVariable Long allocationId, Model model) {
        AssetAllocation allocation = allocationRepository.findById(allocationId)
                .orElseThrow(() -> new IllegalArgumentException("Allocation not found."));
        model.addAttribute("allocation", allocation);
        model.addAttribute("asset", allocation.getAsset());
        return "employee/asset-details";
    }
}