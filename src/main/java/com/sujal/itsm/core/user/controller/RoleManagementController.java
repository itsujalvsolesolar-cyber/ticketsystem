package com.sujal.itsm.core.user.controller;

import com.sujal.itsm.core.user.model.Permission;
import com.sujal.itsm.core.user.model.Role;
import com.sujal.itsm.core.user.repository.PermissionRepository;
import com.sujal.itsm.core.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
@Slf4j
public class RoleManagementController {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    /**
     * Show the Permission Management page for a specific role.
     * Matches your existing HTML template.
     */
    @GetMapping("/{id}/manage")
    public String managePermissions(@PathVariable Long id, Model model) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // Fetch all available permissions
        List<Permission> allPermissions = permissionRepository.findAll();

        // Group permissions by module (e.g., "TICKETING", "ITAMS", "ADMIN")
        Map<String, List<Permission>> permissionsByModule = allPermissions.stream()
                .collect(Collectors.groupingBy(Permission::getModule));

        model.addAttribute("role", role);
        model.addAttribute("permissionsByModule", permissionsByModule);
        model.addAttribute("pageTitle", "Manage Permissions: " + role.getName());

        return "admin/roles/manage";
    }

    /**
     * Save/Update permissions for a role.
     * Matches your form action: th:action="@{/admin/roles/{id}/permissions(id=${role.id})}"
     */
    @PostMapping("/{id}/permissions")
    public String savePermissions(
            @PathVariable Long id,
            @RequestParam(required = false) List<Long> permissionIds,
            RedirectAttributes redirectAttributes) {

        try {
            Role role = roleRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Role not found"));

            Set<Permission> selectedPermissions = new HashSet<>();
            if (permissionIds != null) {
                selectedPermissions = new HashSet<>(permissionRepository.findAllById(permissionIds));
            }

            role.setPermissions(selectedPermissions);
            roleRepository.save(role);

            log.info("✅ Updated {} permissions for role: {}", selectedPermissions.size(), role.getName());
            redirectAttributes.addFlashAttribute("success", "Permissions updated successfully for " + role.getName());

        } catch (Exception e) {
            log.error("❌ Failed to update permissions", e);
            redirectAttributes.addFlashAttribute("error", "Failed to update permissions: " + e.getMessage());
        }

        return "redirect:/admin/settings#roles";
    }

    /**
     * Create a new custom role (Matches your "Add New Role" modal in settings.html)
     */
    @PostMapping("/create")
    public String createRole(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            RedirectAttributes redirectAttributes) {

        try {
            if (roleRepository.existsByName(name.toUpperCase().replace(" ", "_"))) {
                throw new RuntimeException("A role with this name already exists.");
            }

            Role newRole = Role.builder()
                    .name(name.toUpperCase().replace(" ", "_"))
                    .description(description)
                    .isSystemRole(false) // Custom roles are not system roles
                    .permissions(new HashSet<>())
                    .build();

            roleRepository.save(newRole);
            log.info("✅ Created new custom role: {}", newRole.getName());
            redirectAttributes.addFlashAttribute("success", "Role '" + newRole.getName() + "' created successfully!");

        } catch (Exception e) {
            log.error("❌ Failed to create role", e);
            redirectAttributes.addFlashAttribute("error", "Failed to create role: " + e.getMessage());
        }

        return "redirect:/admin/settings#roles";
    }
}