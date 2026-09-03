package com.sujal.itsm.core.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final RequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                        HttpServletResponse response, 
                                        Authentication authentication) throws IOException, ServletException {
        
        // 1. If user previously requested a protected URL before login, prioritize it
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            String redirectUrl = savedRequest.getRedirectUrl();
            if (redirectUrl != null && !redirectUrl.contains("/login") && !redirectUrl.contains("/error")) {
                log.debug("Redirecting to previously saved request: {}", redirectUrl);
                super.onAuthenticationSuccess(request, response, authentication);
                return;
            }
        }

        // 2. Normalize authorities into clean uppercase strings without "ROLE_"
        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(String::trim)
                .map(String::toUpperCase)
                .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
                .map(r -> r.replace(" ", "_"))
                .collect(Collectors.toSet());

        String targetUrl;

        // 3. Route based on highest privilege tier
        if (roles.contains("SUPER_ADMIN") || roles.contains("ADMIN") || roles.contains("IT_MANAGER") || roles.contains("HR_MANAGER")) {
            // Tier 1: System Admin & Management Dashboard
            targetUrl = "/";
        } else if (roles.contains("IT_EXECUTIVE") || roles.contains("STAFF")) {
            // Tier 2: IT Operations / ITAM Dashboard
            targetUrl = "/itams/dashboard";
        } else {
            // Tier 3: Employee Self-Service Workspace
            targetUrl = "/employee/dashboard";
        }

        log.info("User '{}' authenticated successfully. Redirecting to workspace: {}", 
                authentication.getName(), targetUrl);

        // Clear any leftover authentication attributes
        clearAuthenticationAttributes(request);

        // Redirect to intended target
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}