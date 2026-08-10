package com.sujal.itsm.core.user.controller;

import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.core.user.model.Department;
import com.sujal.itsm.core.user.model.Role;
import com.sujal.itsm.core.user.repository.AppUserRepository;
import com.sujal.itsm.core.user.repository.DepartmentRepository;
import com.sujal.itsm.core.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.HashSet;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final AppUserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Show the "Edit User" page
     * Matches your HTML: th:value="${user.username}", ${roles}, ${departments}
     */
    @GetMapping("/{id}/edit")
    public String editUser(@PathVariable Long id, Model model) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("roles", roleRepository.findAll()); // Matches your HTML ${roles}
        model.addAttribute("pageTitle", "Edit User: " + user.getUsername());

        return "admin/users/edit";
    }

    /**
     * Handle User Update Form Submission
     * Matches your HTML: th:action="@{/admin/users/{id}/update(id=${user.id})}"
     */
    @PostMapping("/{id}/update")
    public String updateUser(
            @PathVariable Long id,
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam(required = false) String password,
            @RequestParam Long roleId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "false") boolean isActive,
            RedirectAttributes redirectAttributes) {

        try {
            AppUser user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setUsername(username);
            user.setEmail(email);
            user.setActive(isActive);

            // Only update password if provided
            if (password != null && !password.trim().isEmpty()) {
                user.setPassword(passwordEncoder.encode(password));
            }

            // Set Department
            if (departmentId != null) {
                Department dept = departmentRepository.findById(departmentId)
                        .orElseThrow(() -> new RuntimeException("Department not found"));
                user.setDepartment(dept);
            } else {
                user.setDepartment(null);
            }

            // Set Role (Your HTML uses singular roleId)
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            user.setRoles(new HashSet<>(Collections.singletonList(role)));

            userRepository.save(user);

            log.info("✅ Updated user: {}", username);
            redirectAttributes.addFlashAttribute("success", "User updated successfully!");

        } catch (Exception e) {
            log.error("❌ Failed to update user", e);
            redirectAttributes.addFlashAttribute("error", "Failed to update user: " + e.getMessage());
        }

        return "redirect:/admin/settings#users";
    }
}