package com.sujal.itsm.core.admin.controller;

import com.sujal.itsm.core.admin.model.SystemSetting;
import com.sujal.itsm.core.admin.service.SystemConfigurationService;
import com.sujal.itsm.core.email.service.EmailService;
import com.sujal.itsm.core.security.CurrentUserService;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.core.user.repository.AppUserRepository;
import com.sujal.itsm.ticketing.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.sujal.itsm.core.user.repository.DepartmentRepository;
import com.sujal.itsm.core.user.repository.RoleRepository;
import com.sujal.itsm.ticketing.repository.CategoryRepository;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminSettingsController {

    private final SystemConfigurationService configService;
    private final AppUserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final EmailService emailService;
    private final CurrentUserService currentUserService;
    private final DepartmentRepository departmentRepository;
    private final CategoryRepository categoryRepository;
    private final RoleRepository roleRepository;

    @GetMapping("/console")
    public String showConsole(
            @RequestParam(defaultValue = "DASHBOARD") String tab,
            Model model) {

        log.info("🔍 Loading admin console for tab: {}", tab);

        try {
            if ("DASHBOARD".equalsIgnoreCase(tab)) {
                model.addAttribute("totalUsers", userRepository.count());
                model.addAttribute("totalTickets", ticketRepository.count());

                long activeUsers = 0;
                try {
                    activeUsers = userRepository.countByIsActiveTrue();
                } catch (Exception e) {
                    activeUsers = userRepository.count();
                }
                model.addAttribute("activeUsers", activeUsers);
                model.addAttribute("javaVersion", System.getProperty("java.version"));
                model.addAttribute("osName", System.getProperty("os.name"));
            } else {
                List<SystemSetting> settings = configService.getSettingsByCategory(tab);
                log.info("📊 Found {} settings for category: {}",
                        settings != null ? settings.size() : "null", tab);

                // NEVER pass null to Thymeleaf
                if (settings == null) {
                    log.warn("⚠️ Settings returned null for category: {}. Using empty list.", tab);
                    settings = new ArrayList<>();
                }
                model.addAttribute("settings", settings);
            }

            model.addAttribute("activeTab", tab != null ? tab : "DASHBOARD");
            model.addAttribute("pageTitle", "System Administration");

            log.info("✅ Admin console loaded successfully for tab: {}", tab);
            return "admin/console";

        } catch (Exception e) {
            log.error("❌ CRITICAL ERROR loading admin console for tab: {}", tab, e);
            model.addAttribute("error", "Failed to load settings: " + e.getMessage());
            model.addAttribute("activeTab", "DASHBOARD"); // Fallback to dashboard
            model.addAttribute("pageTitle", "System Administration - Error");
            return "admin/console";
        }
    }

    @PostMapping("/console/save")
    public String saveSettings(
            @RequestParam String category,
            @RequestParam List<String> keys,
            @RequestParam List<String> values,
            RedirectAttributes redirectAttributes) {

        try {
            for (int i = 0; i < keys.size(); i++) {
                configService.updateSetting(category, keys.get(i), values.get(i), "STRING", "");
            }
            redirectAttributes.addFlashAttribute("success", "Settings saved successfully!");
        } catch (Exception e) {
            log.error("❌ Failed to save settings", e);
            redirectAttributes.addFlashAttribute("error", "Failed to save settings: " + e.getMessage());
        }

        return "redirect:/admin/console?tab=" + category;
    }

    @PostMapping("/console/clear-cache")
    public String clearCache(RedirectAttributes redirectAttributes) {
        configService.clearCache();
        redirectAttributes.addFlashAttribute("success", "System configuration cache cleared successfully!");
        return "redirect:/admin/console";
    }

    @PostMapping("/console/test-email")
    public String testEmail(RedirectAttributes redirectAttributes) {
        try {
            AppUser currentUser = currentUserService.getCurrentUser();
            if (currentUser == null || currentUser.getEmail() == null) {
                throw new RuntimeException("Current user or email not found. Please ensure you are logged in.");
            }

            emailService.sendTestEmail(currentUser.getEmail());
            redirectAttributes.addFlashAttribute("success", "✅ Test email sent successfully to " + currentUser.getEmail());

        } catch (Exception e) {
            log.error("❌ Test email failed", e);
            redirectAttributes.addFlashAttribute("error", "❌ Failed to send test email: " + e.getMessage());
        }
        return "redirect:/admin/console?tab=EMAIL";
    }

    /**
     * Master Data & User Management Settings Page
     */
    @GetMapping("/settings")
    public String showSystemSettings(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("availableRoles", roleRepository.findAll());
        model.addAttribute("pageTitle", "System Settings");

        return "admin/settings";
    }
}