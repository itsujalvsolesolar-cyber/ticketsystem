package com.sujal.itsm.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(auth -> auth
                    // 1. Public Routes
                    .requestMatchers("/ws/**", "/notifications/**", "/tickets/new", "/tickets",
                            "/submit-success", "/login", "/css/**", "/js/**", "/images/**",
                            "/uploads/**", "/favicon.ico", "/error").permitAll()

                    // 2. ITAM Routes
                    .requestMatchers("/itams/categories/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_IT_MANAGER", "ROLE_IT_EXECUTIVE")
                    .requestMatchers("/itams/brands/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_IT_MANAGER", "ROLE_IT_EXECUTIVE")
                    .requestMatchers("/itams/suppliers/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_IT_MANAGER", "ROLE_IT_EXECUTIVE")
                    .requestMatchers("/itams/assets/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_IT_MANAGER", "ROLE_IT_EXECUTIVE")
                    .requestMatchers("/itams/employees/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_IT_MANAGER", "ROLE_IT_EXECUTIVE", "ROLE_HR_MANAGER")
                    .requestMatchers("/itams/products/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_IT_MANAGER", "ROLE_IT_EXECUTIVE")
                    .requestMatchers("/itams/warehouses/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_IT_MANAGER", "ROLE_IT_EXECUTIVE")
                    .requestMatchers("/itams/stock/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_IT_MANAGER", "ROLE_IT_EXECUTIVE")
                    .requestMatchers("/itams/software/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_IT_MANAGER", "ROLE_IT_EXECUTIVE")
                    .requestMatchers("/itams/access/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_IT_MANAGER", "ROLE_IT_EXECUTIVE")
                    .requestMatchers("/itams/software-catalog/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_IT_MANAGER", "ROLE_IT_EXECUTIVE")
                    .requestMatchers("/nas/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_IT_MANAGER", "ROLE_IT_EXECUTIVE", "ROLE_AGENT")
                    .requestMatchers("/executive/**").hasAuthority("ROLE_SUPER_ADMIN") // Only Super Admin (CEO/MD)
                    .requestMatchers("/admin/workflows/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_IT_MANAGER")// Inside filterChain method, add this line:

                    // 3. Admin/Settings Routes - ✅ FIXED: Use hasAnyAuthority with exact underscored names
                    .requestMatchers("/admin/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_IT_MANAGER")

                    // 4. All other requests require authentication
                    .anyRequest().authenticated()
            )
            .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/", true).permitAll())
            .logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll());

    return http.build();
  }
}