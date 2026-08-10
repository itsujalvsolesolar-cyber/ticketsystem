package com.sujal.itsm.core.security;

import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.core.user.model.Permission;
import com.sujal.itsm.core.user.model.Role;
import com.sujal.itsm.core.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (!user.isActive()) {
            log.warn("User {} is inactive", username);
            throw new UsernameNotFoundException("User is inactive");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();

        // ✅ 1. ADD ROLE NAMES AS AUTHORITIES (e.g., ROLE_SUPER_ADMIN, ROLE_IT_MANAGER)
        for (Role role : user.getRoles()) {
            String roleName = "ROLE_" + role.getName().replace(" ", "_").toUpperCase();
            authorities.add(new SimpleGrantedAuthority(roleName));

            // ✅ 2. ADD ALL PERMISSIONS FROM THE ROLE (e.g., nas:folder.view)
            for (Permission permission : role.getPermissions()) {
                authorities.add(new SimpleGrantedAuthority(permission.getCode()));
            }
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isActive(),
                true, true, true,
                authorities
        );
    }
}