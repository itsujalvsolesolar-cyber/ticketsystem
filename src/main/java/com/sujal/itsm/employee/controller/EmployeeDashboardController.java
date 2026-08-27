package com.sujal.itsm.employee.controller;

import com.sujal.itsm.core.security.CurrentUserService;
import com.sujal.itsm.core.user.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeDashboardController {

    private final CurrentUserService currentUserService;

    // TODO: Inject TicketService, AssetAllocationService, etc. later to fetch real data

    @GetMapping("/dashboard")
    public String showEmployeeDashboard(Model model) {
        // 1. Get the logged-in employee
        AppUser currentUser = currentUserService.getCurrentUser();

        // 2. Add data to the model for the HTML template
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("pageTitle", "Employee Dashboard");

        // TODO: Add KPIs later, for example:
        // model.addAttribute("myOpenTickets", ticketService.countMyOpenTickets(currentUser.getId()));
        // model.addAttribute("myAssets", assetService.countMyAssets(currentUser.getId()));

        // 3. Return the HTML template
        return "employee/dashboard";
    }
}