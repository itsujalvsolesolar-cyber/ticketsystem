package com.sujal.itsm.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import com.sujal.itsm.core.admin.model.SystemSetting;
import com.sujal.itsm.core.admin.repository.SystemSettingRepository;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.core.user.model.Department;
import com.sujal.itsm.core.user.model.Role;
import com.sujal.itsm.core.user.repository.AppUserRepository;
import com.sujal.itsm.core.user.repository.DepartmentRepository;
import com.sujal.itsm.core.user.repository.RoleRepository;
import com.sujal.itsm.ticketing.model.Category;
import com.sujal.itsm.ticketing.model.DashboardWidget;
import com.sujal.itsm.ticketing.repository.CategoryRepository;
import com.sujal.itsm.ticketing.repository.DashboardWidgetRepository;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

  private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

  @Value("${app.seeding.enabled:true}")
  private boolean seedingEnabled;

  @Value("${app.seeding.force-password-reset:false}")
  private boolean forcePasswordReset;

  @Value("${app.seeding.admin.password:admin123}")
  private String defaultAdminPassword;

  @Value("${app.seeding.manager.password:manager123}")
  private String defaultManagerPassword;

  @Value("${app.seeding.agent.password:1234}")
  private String defaultAgentPassword;

  private final RoleRepository roleRepository;

  // ✅ ADDED: Inject SystemSettingRepository
  private final SystemSettingRepository systemSettingRepository;

  @Bean
  @Transactional
  CommandLineRunner initDatabase(
          AppUserRepository userRepository,
          DepartmentRepository deptRepo,
          CategoryRepository catRepo,
          RoleRepository roleRepo,
          DashboardWidgetRepository widgetRepo,
          PasswordEncoder encoder) {

    return args -> {
      if (!seedingEnabled) {
        logger.info("⏭️ Database seeding is disabled. Skipping initialization.");
        return;
      }

      logger.info("🚀 Starting enterprise database seeding process...");

      try {
        // 1. Seed System Settings (✅ NEW)
        seedSystemSettings(systemSettingRepository);

        // 2. Seed Departments
        seedDepartments(deptRepo);

        // 3. Seed Categories
        seedCategories(catRepo);

        // 4. Seed Roles
        seedRoles(roleRepo);

        // 5. Seed Users
        seedUsers(userRepository, deptRepo, encoder);

        // 6. Seed Dashboard Widgets
        seedWidgets(widgetRepo);

        logger.info("✅ Database seeding completed successfully!");

      } catch (Exception e) {
        logger.error("❌ CRITICAL ERROR during database seeding. Transaction will be rolled back.", e);
        throw new RuntimeException("Database seeding failed", e);
      }
    };
  }

   // ============================================
    // ONE-TIME PASSWORD REPAIR
    // ============================================
    @Bean
    public CommandLineRunner syncDefaultPasswords(
            AppUserRepository userRepository,
            PasswordEncoder encoder) {

        return args -> {

            if (!forcePasswordReset) {
                return;
            }

            logger.info(
                "🔑 Force-syncing default user passwords " +
                "with valid BCrypt hashes..."
            );

            syncPassword(
                userRepository,
                encoder,
                "admin",
                defaultAdminPassword
            );

            syncPassword(
                userRepository,
                encoder,
                "manager",
                defaultManagerPassword
            );

            syncPassword(
                userRepository,
                encoder,
                "sujal",
                defaultAgentPassword
            );

            logger.info(
                "✅ Password repair complete. " +
                "Set app.seeding.force-password-reset=false when done."
            );
        };
    }

    private void syncPassword(
            AppUserRepository repo,
            PasswordEncoder encoder,
            String username,
            String rawPassword) {

        repo.findByUsername(username).ifPresent(user -> {

            user.setPassword(encoder.encode(rawPassword));
            user.setActive(true);
            user.setAccountNonLocked(true);
            user.setEmailVerified(true);

            repo.save(user);

            logger.info(
                "✅ Valid BCrypt hash generated and saved for user '{}'",
                username
            );
        });
    }



  // ============================================
  // NEW: SEED SYSTEM SETTINGS
  // ============================================
  private void seedSystemSettings(SystemSettingRepository repo) {
    logger.debug("Seeding default system settings...");

    // Company Settings
    createSettingIfNotExists(repo, "COMPANY", "company_name", "Enterprise IT Solutions", "STRING", "Official company name");
    createSettingIfNotExists(repo, "COMPANY", "timezone", "Asia/Kolkata", "STRING", "Default system timezone");
    createSettingIfNotExists(repo, "COMPANY", "date_format", "dd-MM-yyyy", "STRING", "Default date display format");

    // Security Settings
    createSettingIfNotExists(repo, "SECURITY", "password_min_length", "8", "NUMBER", "Minimum password length");
    createSettingIfNotExists(repo, "SECURITY", "password_expiry_days", "90", "NUMBER", "Days until password expires (0 = never)");
    createSettingIfNotExists(repo, "SECURITY", "session_timeout_minutes", "30", "NUMBER", "Inactive session timeout in minutes");
    createSettingIfNotExists(repo, "SECURITY", "max_login_attempts", "5", "NUMBER", "Failed login attempts before lockout");

    // Email Settings (Defaults, to be configured by Admin)
    createSettingIfNotExists(repo, "EMAIL", "smtp_host", "smtp.gmail.com", "STRING", "SMTP Server Host");
    createSettingIfNotExists(repo, "EMAIL", "smtp_port", "587", "NUMBER", "SMTP Server Port");
    createSettingIfNotExists(repo, "EMAIL", "smtp_tls_enabled", "true", "BOOLEAN", "Enable TLS for SMTP");

    logger.info("✅ System settings seeded.");
  }

  // ============================================
  // EXISTING SEEDING METHODS
  // ============================================

  private void seedDepartments(DepartmentRepository repo) {
    logger.debug("Seeding departments...");
    getOrCreateDept(repo, "IT Support", "Handles all technical infrastructure and support requests.");
    getOrCreateDept(repo, "Human Resources", "Manages employee relations, payroll, and benefits.");
    getOrCreateDept(repo, "Sales", "Handles customer acquisition and revenue generation.");
    getOrCreateDept(repo, "Finance", "Manages corporate accounting and financial planning.");
    logger.info("✅ Departments seeded.");
  }

  private void seedCategories(CategoryRepository repo) {
    logger.debug("Seeding categories...");
    getOrCreateCat(repo, "Hardware", "Physical device issues (laptops, monitors, printers).", 8);
    getOrCreateCat(repo, "Software", "Application and OS related issues.", 4);
    getOrCreateCat(repo, "Network", "Connectivity, VPN, and internet access issues.", 2);
    getOrCreateCat(repo, "Access & Permissions", "User account creation and access rights.", 24);
    getOrCreateCat(repo, "Email", "Email client and server issues.", 4);
    logger.info("✅ Categories seeded.");
  }

  private void seedRoles(RoleRepository repo) {
    logger.debug("Seeding roles...");
    getOrCreateRole(repo, "SUPER ADMIN", "Full system access including user management and settings.");
    getOrCreateRole(repo, "ROLE_MANAGER", "View all tickets, assign agents, no admin settings access.");
    getOrCreateRole(repo, "ROLE_AGENT", "Handle assigned tickets and update status.");
    logger.info("✅ Roles seeded.");
  }

  private void seedUsers(AppUserRepository userRepo, DepartmentRepository deptRepo, PasswordEncoder encoder) {
    logger.debug("Seeding default users...");
    Department itDept = deptRepo.findByName("IT Support").orElse(null);

    if (userRepo.findByUsername("admin").isEmpty()) {
      Role adminRole = roleRepository.findByName("SUPER ADMIN")
              .orElseThrow(() -> new RuntimeException("SUPER ADMIN role not found. Run PermissionSeeder first."));

      AppUser admin = AppUser.builder()
              .username("admin")
              .password(encoder.encode(defaultAdminPassword))
              .email("admin@company.com")
              .fullName("System Administrator")
              .isActive(true)
              .isEmailVerified(true)
              .roles(Set.of(adminRole))
              .department(itDept)
              .build();
      userRepo.save(admin);
      logger.debug("Created user: admin");
    } else {
      logger.debug("User 'admin' already exists. Skipping.");
    }

    createUserIfNotExists(userRepo, "manager", defaultManagerPassword, "manager@company.com", "IT Manager", "IT", "Manager", "ROLE_MANAGER", itDept, encoder);
    createUserIfNotExists(userRepo, "sujal", defaultAgentPassword, "sujal@company.com", "Sujal Khunt", "Sujal", "Khunt", "ROLE_AGENT", itDept, encoder);

    logger.info("✅ Default users seeded.");
  }

  private void seedWidgets(DashboardWidgetRepository repo) {
    if (repo.count() == 0) {
      logger.debug("Seeding dashboard widgets...");
      createWidget(repo, "STAT_CARD", "Total Tickets", "primary", 1);
      createWidget(repo, "STAT_CARD", "Open Tickets", "danger", 2);
      createWidget(repo, "STAT_CARD", "Resolved Today", "success", 3);
      createWidget(repo, "STAT_CARD", "Closed Tickets", "secondary", 4);
      logger.info("✅ Dashboard widgets seeded.");
    } else {
      logger.debug("Dashboard widgets already exist. Skipping.");
    }
  }

  // ============================================
  // HELPER METHODS
  // ============================================

  private void createSettingIfNotExists(SystemSettingRepository repo, String category, String key, String value, String dataType, String description) {
    if (repo.findByCategoryAndKey(category, key).isEmpty()) {
      repo.save(SystemSetting.builder()
              .category(category)
              .key(key)
              .value(value)
              .dataType(dataType)
              .description(description)
              .build());
    }
  }

  private void createUserIfNotExists(AppUserRepository repo, String username, String rawPassword, String email, String fullName, String firstName, String lastName, String roleName, Department dept, PasswordEncoder encoder) {
    if (repo.findByUsername(username).isEmpty()) {
      Role role = roleRepository.findByName(roleName)
              .orElseThrow(() -> new RuntimeException(roleName + " role not found. Run PermissionSeeder first."));

      AppUser user = AppUser.builder()
              .username(username)
              .password(encoder.encode(rawPassword))
              .email(email)
              .fullName(fullName)
              .firstName(firstName)
              .lastName(lastName)
              .roles(Set.of(role))
              .department(dept)
              .isActive(true)
              .isEmailVerified(true)
              .build();
      repo.save(user);
      logger.debug("Created user: {}", username);
    } else {
      logger.debug("User '{}' already exists. Skipping.", username);
    }
  }

  private Department getOrCreateDept(DepartmentRepository repo, String name, String description) {
    return repo.findByName(name).orElseGet(() -> repo.save(Department.builder().name(name).description(description).build()));
  }

  private Category getOrCreateCat(CategoryRepository repo, String name, String description, int slaHours) {
    return repo.findByName(name).orElseGet(() -> repo.save(Category.builder().name(name).description(description).slaHours(slaHours).build()));
  }

  private Role getOrCreateRole(RoleRepository repo, String name, String description) {
    return repo.findByName(name).orElseGet(() -> repo.save(Role.builder().name(name).description(description).build()));
  }

  private void createWidget(DashboardWidgetRepository repo, String type, String title, String color, int order) {
    DashboardWidget w = new DashboardWidget();
    w.setWidgetType(type);
    w.setTitle(title);
    w.setConfigJson("{\"color\": \"" + color + "\"}");
    w.setSortOrder(order);
    w.setActive(true);
    repo.save(w);
  }
}