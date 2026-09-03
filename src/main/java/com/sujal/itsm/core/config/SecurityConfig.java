package com.sujal.itsm.core.config;

import com.sujal.itsm.core.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationSuccessHandler authenticationSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/ws/**", "/api/v1/public/**"))
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdn.jsdelivr.net; style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://fonts.googleapis.com; img-src 'self' data: blob:; font-src 'self' data: https://cdn.jsdelivr.net https://fonts.gstatic.com;"
                ))
                .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .frameOptions(frame -> frame.sameOrigin())
            )
            .authorizeHttpRequests(auth -> auth
                // 1. PUBLIC & STATIC ASSETS
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/uploads/**", "/login", "/error", "/actuator/health").permitAll()
                
                // 2. BASELINE SELF-SERVICE (Any authenticated human)
                // Covers: Employee Portal, My Tickets, Shift Status, Notifications
                .requestMatchers("/", "/dashboard", "/employee/**", "/tickets/**", "/notifications/**", "/shifts/**")
                .hasAnyRole("EMPLOYEE", "IT_EXECUTIVE", "IT_MANAGER", "SUPER_ADMIN")
                
                // 3. IT OPERATIONS (Fulfillment & Support)
                // Covers: ITAMS Hardware, Software, Digital Access, Label Printing
                .requestMatchers("/itams/**", "/api/v1/itams/**")
                .hasAnyRole("IT_EXECUTIVE", "IT_MANAGER", "SUPER_ADMIN")
                
                // 4. IT GOVERNANCE & IAM (Approvals, User Provisioning, Workflows)
                // Covers: Identity & Access Management, Workflow Builder
                .requestMatchers("/iam/**", "/api/v1/iam/**", "/admin/workflows/**", "/approval-requests/**")
                .hasAnyRole("IT_MANAGER", "SUPER_ADMIN")
                
                // 5. SYSTEM ADMINISTRATION & AUDIT (Global Settings, Logs)
                // Covers: Admin Console, Audit Viewer, Recycle Bin
                .requestMatchers("/admin/**", "/api/v1/admin/**", "/audit/**", "/system/**", "/executive/**", "/reports/**")
                .hasRole("SUPER_ADMIN")
                
                // 6. LEAST PRIVILEGE ENFORCEMENT: DEFAULT DENY
                // If a new endpoint is created and not explicitly mapped above, it is blocked.
                .anyRequest().denyAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/error?status=403")
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(authenticationSuccessHandler)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}