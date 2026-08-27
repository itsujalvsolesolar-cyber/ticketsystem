package com.sujal.itsm.core.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            .authorizeHttpRequests(auth -> auth
                    // 1. Public Routes
                    .requestMatchers("/ws/**", "/notifications/**", "/tickets/new", "/tickets",
                            "/submit-success", "/login", "/css/**", "/js/**", "/images/**",
                            "/uploads/**", "/favicon.ico", "/error").permitAll()

                    // 2. Employee Portal Routes (Accessible to any authenticated user)
                    .requestMatchers("/employee/**").authenticated()

                    // 3. ITAM Routes (IT Staff & Admins only)
                    .requestMatchers("/itams/categories/**", "/itams/brands/**", "/itams/suppliers/**",
                            "/itams/assets/**", "/itams/employees/**", "/itams/products/**",
                            "/itams/warehouses/**", "/itams/stock/**", "/itams/software-catalog/**",
                            "/itams/software/**", "/itams/access/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_IT_MANAGER", "ROLE_IT_EXECUTIVE")

                    // 4. Workflow & Admin Routes
                    .requestMatchers("/admin/workflows/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_IT_MANAGER")
                    .requestMatchers("/admin/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_IT_MANAGER")

                    // 5. All other requests require authentication
                    .anyRequest().authenticated()
            )
            .formLogin(form -> form
                    .loginPage("/login")
                    .successHandler(customAuthenticationSuccessHandler) // <-- USE CUSTOM HANDLER HERE
                    .permitAll()
            )
            .logout(logout -> logout
                    .logoutSuccessUrl("/login?logout")
                    .permitAll()
            );

    return http.build();
  }
}