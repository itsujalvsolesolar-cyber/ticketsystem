package com.sujal.itsm.core.security;

import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.core.user.repository.AppUserRepository;
import com.sujal.itsm.itams.model.Employee;
import com.sujal.itsm.itams.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepository;
    private final EmployeeRepository employeeRepository; // Injected for 16.3

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {
        
        // 1. Try standard username
        AppUser appUser = userRepository.findByUsername(input).orElse(null);
        
        // 2. Fallback: Try email
        if (appUser == null) {
            appUser = userRepository.findByEmail(input).orElse(null);
        }
        
        // 3. Fallback (16.3): Try Employee Code (e.g., HR ID)
        if (appUser == null) {
            appUser = employeeRepository.findByEmployeeCode(input)
                    .map(Employee::getUser)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username, email, or employee code: " + input));
        }

        // Build Authorities (Preserving existing 16.1 logic)
        Set<GrantedAuthority> authorities = new HashSet<>();
        if (appUser.getRoles() != null) {
            appUser.getRoles().forEach(role -> {
                String rawName = role.getName().trim().toUpperCase().replace(" ", "_");
                String cleanName = rawName.startsWith("ROLE_") ? rawName.substring(5) : rawName;
                
                authorities.add(new SimpleGrantedAuthority("ROLE_" + cleanName));
                authorities.add(new SimpleGrantedAuthority(cleanName));
                
                if (cleanName.equals("SUPER_ADMIN") || cleanName.equals("SUPERADMIN")) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    authorities.add(new SimpleGrantedAuthority("ADMIN"));
                }
            });
        }

        return User.builder()
                .username(appUser.getUsername())
                .password(appUser.getPassword())
                .disabled(!appUser.isActive())
                .accountLocked(!appUser.isAccountNonLocked())
                .authorities(authorities)
                .build();
    }
}