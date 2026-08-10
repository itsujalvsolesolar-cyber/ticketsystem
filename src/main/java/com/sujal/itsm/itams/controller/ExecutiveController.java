package com.sujal.itsm.itams.controller;

import com.sujal.itsm.core.security.CurrentUserService;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.core.user.repository.AppUserRepository;
import com.sujal.itsm.itams.enums.NasPermissionLevel;
import com.sujal.itsm.itams.model.NasAccessRequest;
import com.sujal.itsm.itams.model.NasFolder;
import com.sujal.itsm.itams.service.NasService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/itams/executive") // ✅ CHANGED from "/executive" to avoid conflict
@RequiredArgsConstructor
public class ExecutiveController {

    private final NasService nasService;
    private final CurrentUserService currentUserService;
    private final AppUserRepository appUserRepository;

    @GetMapping("/dashboard")
    public String executiveDashboard(Model model) {
        // Get all active employees
        List<AppUser> employees = appUserRepository.findByIsActiveTrue();

        // Get all folders
        List<NasFolder> folders = nasService.getAllFolders();

        // Get all requests for the history table
        List<NasAccessRequest> allRequests = nasService.getAllRequests();

        model.addAttribute("employees", employees);
        model.addAttribute("folders", folders);
        model.addAttribute("allRequests", allRequests);
        model.addAttribute("pageTitle", "NAS Executive Dashboard");

        return "itams/executive/dashboard";
    }

    @PostMapping("/requests")
    public String createRequest(
            @RequestParam Long employeeId,
            @RequestParam Long folderId,
            @RequestParam NasPermissionLevel permissionLevel,
            @RequestParam(defaultValue = "false") boolean isTemporary,
            @RequestParam(required = false) String temporaryEndDate,
            @RequestParam String remarks,
            RedirectAttributes redirectAttributes
    ) {
        try {
            AppUser currentUser = currentUserService.getCurrentUser();

            LocalDateTime endDate = null;
            if (isTemporary && temporaryEndDate != null && !temporaryEndDate.isEmpty()) {
                endDate = LocalDateTime.parse(temporaryEndDate);
            }

            nasService.requestAccessForEmployee(
                    folderId,
                    employeeId,
                    currentUser.getId(),
                    permissionLevel,
                    isTemporary,
                    endDate,
                    remarks
            );
            redirectAttributes.addFlashAttribute("success", "NAS Access request created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create request: " + e.getMessage());
        }
        return "redirect:/itams/executive/dashboard"; // ✅ UPDATED REDIRECT
    }
}