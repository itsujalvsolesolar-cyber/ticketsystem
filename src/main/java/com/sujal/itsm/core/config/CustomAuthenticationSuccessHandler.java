package com.sujal.itsm.core.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String targetUrl = "/"; // Default to Admin Dashboard
        boolean isEmployee = false;
        boolean isAdmin = false;

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();

            // Check for Admin/IT/HR Manager roles
            if (role.equals("ROLE_SUPER_ADMIN") || role.equals("ROLE_IT_MANAGER") ||
                    role.equals("ROLE_IT_EXECUTIVE") || role.equals("ROLE_HR_MANAGER")) {
                isAdmin = true;
            }

            // Check for Employee/Agent/Department Head roles
            if (role.equals("ROLE_AGENT") || role.equals("ROLE_HR_EXECUTIVE") || role.equals("ROLE_DEPARTMENT_HEAD")) {
                isEmployee = true;
            }
        }

        // If the user is an employee (and not an admin), redirect to employee dashboard
        if (isEmployee && !isAdmin) {
            targetUrl = "/employee/dashboard";
        }

        response.sendRedirect(targetUrl);
    }
}