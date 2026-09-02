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
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
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
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/ws/**", "/api/v1/public/**")
            )
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives(
                        "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdn.jsdelivr.net https://html2canvas.hertzen.com; " +
                        "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://fonts.googleapis.com; " +
                        "img-src 'self' data: blob:; " +
                        "font-src 'self' data: https://cdn.jsdelivr.net https://fonts.gstatic.com; " +
                        "connect-src 'self' wss: https:; " +
                        "frame-ancestors 'self'"
                    )
                )
                .referrerPolicy(referrer -> referrer
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                )
                .frameOptions(frame -> frame.sameOrigin())
            )
            .authorizeHttpRequests(auth -> auth
                // Static assets & public endpoints
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/uploads/**", "/login", "/error").permitAll()
                
                // Super Admin & Admin modules
                .requestMatchers("/admin/**", "/api/v1/admin/**", "/system/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                
                // IAM module
                .requestMatchers("/iam/**", "/api/v1/iam/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "STAFF", "IT_MANAGER")
                
                // ITAMS module
                .requestMatchers("/itams/**", "/api/v1/itams/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "STAFF", "IT_MANAGER", "IT_EXECUTIVE")
                
                // Executive & Reporting
                .requestMatchers("/executive/**", "/reports/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "EXECUTIVE", "IT_MANAGER")
                
                // Audit & Workflows
                .requestMatchers("/audit/**", "/workflows/**", "/approval-requests/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "STAFF")
                
                // Self-service Employee & Core Ticketing
                .requestMatchers("/employee/**").hasAnyRole("EMPLOYEE", "ADMIN", "SUPER_ADMIN", "STAFF", "USER")
                .requestMatchers("/", "/dashboard", "/tickets/**", "/notifications/**", "/shifts/**").authenticated()
                
                .anyRequest().authenticated()
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