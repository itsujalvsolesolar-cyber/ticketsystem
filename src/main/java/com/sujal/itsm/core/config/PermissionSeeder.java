package com.sujal.itsm.config;

import com.sujal.itsm.core.user.model.Permission;
import com.sujal.itsm.core.user.model.Role;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.core.user.repository.PermissionRepository;
import com.sujal.itsm.core.user.repository.RoleRepository;
import com.sujal.itsm.core.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionSeeder implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("🚀 Starting Dynamic RBAC Permission Seeding...");
        
        // Only run if no roles exist (prevents re-seeding)
        if (roleRepository.count() > 0) {
            log.info("✅ Roles already exist. Skipping permission seeding.");
            return;
        }

        // Step 1: Create All Permissions
        Map<String, Permission> permissions = createAllPermissions();
        log.info("✅ Created {} permissions across all modules", permissions.size());

        // Step 2: Create Roles with Permissions
        Map<String, Role> roles = createAllRoles(permissions);
        log.info("✅ Created {} roles with assigned permissions", roles.size());

        // Step 3: Create Default Super Admin User
        createDefaultSuperAdmin(roles);
        log.info("✅ Created default Super Admin user (username: admin, password: admin123)");

        log.info("🎉 Dynamic RBAC seeding completed successfully!");
    }

    /**
     * Creates comprehensive permission matrix for all modules
     */
    private Map<String, Permission> createAllPermissions() {
        List<Permission> permissionList = new ArrayList<>();

        // ========== SYSTEM MODULE ==========
        permissionList.addAll(List.of(
            createPerm("system:user.view", "View Users", "SYSTEM"),
            createPerm("system:user.create", "Create Users", "SYSTEM"),
            createPerm("system:user.edit", "Edit Users", "SYSTEM"),
            createPerm("system:user.delete", "Delete Users", "SYSTEM"),
            createPerm("system:role.view", "View Roles", "SYSTEM"),
            createPerm("system:role.manage", "Manage Roles & Permissions", "SYSTEM"),
            createPerm("system:settings.view", "View Settings", "SYSTEM"),
            createPerm("system:settings.edit", "Edit System Settings", "SYSTEM"),
            createPerm("system:audit.view", "View Audit Logs", "SYSTEM"),
            createPerm("system:dashboard.view", "View Dashboard", "SYSTEM")
        ));

        // ========== TICKETING MODULE ==========
        permissionList.addAll(List.of(
            createPerm("tickets:view.all", "View All Tickets", "TICKETS"),
            createPerm("tickets:view.own", "View Own Tickets", "TICKETS"),
            createPerm("tickets:create", "Create Tickets", "TICKETS"),
            createPerm("tickets:edit", "Edit Tickets", "TICKETS"),
            createPerm("tickets:delete", "Delete Tickets", "TICKETS"),
            createPerm("tickets:assign", "Assign Tickets", "TICKETS"),
            createPerm("tickets:resolve", "Resolve Tickets", "TICKETS"),
            createPerm("tickets:reopen", "Reopen Tickets", "TICKETS"),
            createPerm("tickets:priority.change", "Change Priority", "TICKETS"),
            createPerm("tickets:comment.add", "Add Comments", "TICKETS"),
            createPerm("tickets:attachment.add", "Add Attachments", "TICKETS"),
            createPerm("tickets:sla.view", "View SLA Metrics", "TICKETS"),
            createPerm("tickets:report.view", "View Reports", "TICKETS")
        ));

        // ========== ITAM (IT ASSET MANAGEMENT) MODULE ==========
        // Categories
        permissionList.addAll(List.of(
            createPerm("itams:category.view", "View Categories", "ITAM"),
            createPerm("itams:category.create", "Create Categories", "ITAM"),
            createPerm("itams:category.edit", "Edit Categories", "ITAM"),
            createPerm("itams:category.delete", "Delete Categories", "ITAM"),
            
            // Brands
            createPerm("itams:brand.view", "View Brands", "ITAM"),
            createPerm("itams:brand.create", "Create Brands", "ITAM"),
            createPerm("itams:brand.edit", "Edit Brands", "ITAM"),
            createPerm("itams:brand.delete", "Delete Brands", "ITAM"),
            
            // Suppliers
            createPerm("itams:supplier.view", "View Suppliers", "ITAM"),
            createPerm("itams:supplier.create", "Create Suppliers", "ITAM"),
            createPerm("itams:supplier.edit", "Edit Suppliers", "ITAM"),
            createPerm("itams:supplier.delete", "Delete Suppliers", "ITAM"),
            
            // Assets
            createPerm("itams:asset.view", "View Assets", "ITAM"),
            createPerm("itams:asset.create", "Create Assets", "ITAM"),
            createPerm("itams:asset.edit", "Edit Assets", "ITAM"),
            createPerm("itams:asset.delete", "Delete/Scrap Assets", "ITAM"),
            createPerm("itams:asset.allocate", "Allocate Assets", "ITAM"),
            createPerm("itams:asset.return", "Return Assets", "ITAM"),
            createPerm("itams:asset.transfer", "Transfer Assets", "ITAM"),
            createPerm("itams:asset.qrcode.view", "View QR Codes", "ITAM"),
            createPerm("itams:asset.qrcode.generate", "Generate QR Codes", "ITAM"),
            createPerm("itams:asset.warranty.view", "View Warranty Info", "ITAM"),
            
            // Employees
            createPerm("itams:employee.view", "View Employees", "ITAM"),
            createPerm("itams:employee.create", "Create Employees", "ITAM"),
            createPerm("itams:employee.edit", "Edit Employees", "ITAM"),
            createPerm("itams:employee.delete", "Delete Employees", "ITAM"),
            
            // Products & Inventory
            createPerm("itams:product.view", "View Products", "ITAM"),
            createPerm("itams:product.create", "Create Products", "ITAM"),
            createPerm("itams:product.edit", "Edit Products", "ITAM"),
            createPerm("itams:product.delete", "Delete Products", "ITAM"),
            
            // Warehouses
            createPerm("itams:warehouse.view", "View Warehouses", "ITAM"),
            createPerm("itams:warehouse.create", "Create Warehouses", "ITAM"),
            createPerm("itams:warehouse.edit", "Edit Warehouses", "ITAM"),
            createPerm("itams:warehouse.delete", "Delete Warehouses", "ITAM"),
            
            // Stock Management
            createPerm("itams:stock.view", "View Stock Movements", "ITAM"),
            createPerm("itams:stock.in", "Record Stock In", "ITAM"),
            createPerm("itams:stock.out", "Record Stock Out", "ITAM"),
            createPerm("itams:stock.transfer", "Transfer Stock", "ITAM"),
            createPerm("itams:stock.adjust", "Adjust Stock", "ITAM"),
            createPerm("itams:stock.damaged", "Mark as Damaged", "ITAM"),
            
            // Software
            createPerm("itams:software.view", "View Software Licenses", "ITAM"),
            createPerm("itams:software.assign", "Assign Software", "ITAM"),
            createPerm("itams:software.revoke", "Revoke Software", "ITAM"),
            createPerm("itams:software.catalog.manage", "Manage Software Catalog", "ITAM"),
            
            // Digital Access
            createPerm("itams:access.view", "View Digital Access", "ITAM"),
            createPerm("itams:access.grant", "Grant Digital Access", "ITAM"),
            createPerm("itams:access.revoke", "Revoke Digital Access", "ITAM"),
            
            // NAS & Folder Management
            createPerm("itams:nas.view", "View NAS Folders", "ITAM"),
            createPerm("itams:nas.request", "Request NAS Access", "ITAM"),
            createPerm("itams:nas.approve.it", "Approve NAS Access (IT Level)", "ITAM"),
            createPerm("itams:nas.approve.md", "Approve NAS Access (MD/CEO Level)", "ITAM"),
            createPerm("itams:nas.manage", "Manage NAS Folders", "ITAM")
        ));

        // ========== HR MODULE ==========
        permissionList.addAll(List.of(
            createPerm("hr:employee.view", "View Employee Records", "HR"),
            createPerm("hr:employee.create", "Create Employee Records", "HR"),
            createPerm("hr:employee.edit", "Edit Employee Records", "HR"),
            createPerm("hr:employee.delete", "Delete Employee Records", "HR"),
            createPerm("hr:department.view", "View Departments", "HR"),
            createPerm("hr:department.manage", "Manage Departments", "HR"),
            createPerm("hr:report.view", "View HR Reports", "HR")
        ));

        // ========== NAS & FOLDER MANAGEMENT MODULE ==========
        permissionList.addAll(List.of(
                createPerm("nas:folder.view", "View NAS Folders", "NAS"),
                createPerm("nas:folder.manage", "Manage NAS Folders", "NAS"),
                createPerm("nas:access.request", "Request NAS Access", "NAS"),
                createPerm("nas:access.view", "View NAS Access Requests", "NAS"),
                createPerm("nas:access.approve.it", "Approve NAS Access (IT Level)", "NAS"),
                createPerm("nas:access.approve.md", "Approve NAS Access (MD/CEO Level)", "NAS"),
                createPerm("nas:access.revoke", "Revoke NAS Access", "NAS")
        ));

        // Save all permissions
        return permissionRepository.saveAll(permissionList).stream()
                .collect(Collectors.toMap(Permission::getCode, p -> p));
    }

    /**
     * Creates roles with appropriate permission assignments
     */
    private Map<String, Role> createAllRoles(Map<String, Permission> permissions) {
        Map<String, Role> roles = new HashMap<>();

        // ========== SUPER ADMIN - Full Access ==========
        Role superAdmin = createRole(
            "SUPER ADMIN",
            "Full system access with all permissions",
            true,
            new HashSet<>(permissions.values())
        );
        roles.put("SUPER ADMIN", superAdmin);

        // ========== IT MANAGER - ITAM Full + Tickets Management ==========
        Set<Permission> itManagerPerms = new HashSet<>();
        itManagerPerms.addAll(getPermissionsByModule(permissions, "ITAM"));
        itManagerPerms.addAll(getPermissionsByPrefix(permissions, "tickets:"));
        itManagerPerms.addAll(getPermissionsByPrefix(permissions, "system:dashboard"));
        itManagerPerms.addAll(getPermissionsByPrefix(permissions, "system:user.view"));
        
        Role itManager = createRole(
            "IT MANAGER",
            "Manages IT assets, infrastructure, and support tickets",
            true,
            itManagerPerms
        );
        roles.put("IT MANAGER", itManager);

        // ========== IT EXECUTIVE - ITAM Operations + Ticket Handling ==========
        Set<Permission> itExecutivePerms = new HashSet<>();
        itExecutivePerms.addAll(getPermissionsByPrefix(permissions, "itams:asset.view"));
        itExecutivePerms.addAll(getPermissionsByPrefix(permissions, "itams:asset.allocate"));
        itExecutivePerms.addAll(getPermissionsByPrefix(permissions, "itams:asset.return"));
        itExecutivePerms.addAll(getPermissionsByPrefix(permissions, "itams:software.view"));
        itExecutivePerms.addAll(getPermissionsByPrefix(permissions, "itams:software.assign"));
        itExecutivePerms.addAll(getPermissionsByPrefix(permissions, "itams:access.view"));
        itExecutivePerms.addAll(getPermissionsByPrefix(permissions, "itams:access.grant"));
        itExecutivePerms.addAll(getPermissionsByPrefix(permissions, "tickets:view"));
        itExecutivePerms.addAll(getPermissionsByPrefix(permissions, "tickets:create"));
        itExecutivePerms.addAll(getPermissionsByPrefix(permissions, "tickets:assign"));
        itExecutivePerms.addAll(getPermissionsByPrefix(permissions, "tickets:resolve"));
        itExecutivePerms.addAll(getPermissionsByPrefix(permissions, "tickets:comment"));
        
        Role itExecutive = createRole(
            "IT EXECUTIVE",
            "Handles IT support tickets and asset allocation",
            true,
            itExecutivePerms
        );
        roles.put("IT EXECUTIVE", itExecutive);

        // ========== HR MANAGER - HR Full Access ==========
        Set<Permission> hrManagerPerms = new HashSet<>();
        hrManagerPerms.addAll(getPermissionsByModule(permissions, "HR"));
        hrManagerPerms.addAll(getPermissionsByPrefix(permissions, "system:user.view"));
        hrManagerPerms.addAll(getPermissionsByPrefix(permissions, "itams:employee"));
        
        Role hrManager = createRole(
            "HR MANAGER",
            "Manages HR operations and employee records",
            true,
            hrManagerPerms
        );
        roles.put("HR MANAGER", hrManager);

        // ========== HR EXECUTIVE - HR Operations ==========
        Set<Permission> hrExecutivePerms = new HashSet<>();
        hrExecutivePerms.addAll(getPermissionsByPrefix(permissions, "hr:employee.view"));
        hrExecutivePerms.addAll(getPermissionsByPrefix(permissions, "hr:department.view"));
        
        Role hrExecutive = createRole(
            "HR EXECUTIVE",
            "Supports HR operations and employee onboarding",
            true,
            hrExecutivePerms
        );
        roles.put("HR EXECUTIVE", hrExecutive);

        // ========== DEPARTMENT HEAD - Department-Specific Access ==========
        Set<Permission> deptHeadPerms = new HashSet<>();
        deptHeadPerms.addAll(getPermissionsByPrefix(permissions, "tickets:view.all"));
        deptHeadPerms.addAll(getPermissionsByPrefix(permissions, "tickets:create"));
        deptHeadPerms.addAll(getPermissionsByPrefix(permissions, "system:dashboard.view"));
        deptHeadPerms.addAll(getPermissionsByPrefix(permissions, "itams:employee.view"));
        
        Role deptHead = createRole(
            "DEPARTMENT HEAD",
            "Views department tickets and employee assets",
            true,
            deptHeadPerms
        );
        roles.put("DEPARTMENT HEAD", deptHead);

        // ========== AGENT/USER - Basic Access ==========
        Set<Permission> agentPerms = new HashSet<>();
        agentPerms.add(permissions.get("tickets:view.own"));
        agentPerms.add(permissions.get("tickets:create"));
        agentPerms.add(permissions.get("tickets:comment.add"));
        agentPerms.add(permissions.get("tickets:attachment.add"));
        agentPerms.add(permissions.get("system:dashboard.view"));
        agentPerms.add(permissions.get("itams:asset.view")); // View own assets
        
        Role agent = createRole(
            "AGENT",
            "Basic user with ticket creation and viewing rights",
            true,
            agentPerms
        );
        roles.put("AGENT", agent);

        return roles;
    }

    /**
     * Creates default Super Admin user
     */
    private void createDefaultSuperAdmin(Map<String, Role> roles) {
        if (userRepository.findByUsername("admin").isPresent()) {
            return; // Already exists
        }

        Role superAdminRole = roles.get("SUPER ADMIN");
        
        AppUser admin = AppUser.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123")) // CHANGE IN PRODUCTION!
                .email("admin@company.com")
                .fullName("System Administrator")
                .firstName("System")
                .lastName("Administrator")
                .isActive(true)
                .isEmailVerified(true)
                .roles(new HashSet<>(Collections.singletonList(superAdminRole)))
                .build();

        userRepository.save(admin);
        log.warn("⚠️  DEFAULT ADMIN CREATED - Username: admin, Password: admin123");
        log.warn("️  PLEASE CHANGE THE PASSWORD IMMEDIATELY AFTER FIRST LOGIN!");
    }

    // ========== HELPER METHODS ==========

    private Permission createPerm(String code, String name, String module) {
        return Permission.builder()
                .code(code)
                .name(name)
                .module(module)
                .description(name + " - " + module)
                .build();
    }

    private Role createRole(String name, String description, boolean isSystemRole, Set<Permission> permissions) {
        Role role = Role.builder()
                .name(name)
                .description(description)
                .isSystemRole(isSystemRole)
                .permissions(permissions)
                .build();
        return roleRepository.save(role);
    }

    private Set<Permission> getPermissionsByModule(Map<String, Permission> permissions, String module) {
        return permissions.values().stream()
                .filter(p -> p.getModule().equals(module))
                .collect(Collectors.toSet());
    }

    private Set<Permission> getPermissionsByPrefix(Map<String, Permission> permissions, String prefix) {
        return permissions.values().stream()
                .filter(p -> p.getCode().startsWith(prefix))
                .collect(Collectors.toSet());
    }
}