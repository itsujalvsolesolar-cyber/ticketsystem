package com.sujal.itsm.employee.controller;

import com.sujal.itsm.core.security.CurrentUserService;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.ticketing.model.Ticket;
import com.sujal.itsm.ticketing.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeTicketController {

    private final TicketRepository ticketRepository;
    private final CurrentUserService currentUserService;

    @GetMapping("/tickets")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN', 'STAFF')")
    public String myTickets(Model model) {
        // 1. Get the currently logged-in user
        AppUser currentUser = currentUserService.getCurrentUser();
        
        // 2. Extract username and full name to search against the Ticket's requesterName field
        String username = currentUser.getUsername();
        String fullName = currentUser.getFullName(); 
        
        // 3. Fetch scoped tickets (Replaces the broken requesterId query)
        List<Ticket> myTickets = ticketRepository.findByUsernameOrFullName(username, fullName);

        // 4. Calculate open ticket metrics
        long openCount = myTickets.stream()
                .filter(t -> t.getStatus().name().equals("OPEN") || t.getStatus().name().equals("IN_PROGRESS"))
                .count();

        // 5. Pass data to the Thymeleaf template
        model.addAttribute("tickets", myTickets);
        model.addAttribute("openCount", openCount);
        model.addAttribute("currentUser", currentUser);
        
        return "employee/tickets";
    }
}