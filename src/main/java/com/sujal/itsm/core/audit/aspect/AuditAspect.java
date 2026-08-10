package com.sujal.itsm.core.audit.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sujal.itsm.core.audit.annotation.Auditable;
import com.sujal.itsm.core.audit.model.AuditLog;
import com.sujal.itsm.core.audit.service.AuditService;
import com.sujal.itsm.core.security.CurrentUserService;
import com.sujal.itsm.core.user.model.AppUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditService auditService;
    private final CurrentUserService currentUserService;

    // ✅ Instantiate directly to completely bypass Spring injection issues in Aspects
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        // 1. Execute the actual business method
        Object result = joinPoint.proceed();

        // 2. Run audit logic asynchronously (fire and forget)
        try {
            saveAuditLog(joinPoint, auditable, result);
        } catch (Exception e) {
            log.error("❌ Audit Aspect failed to log activity: {}", e.getMessage());
        }

        return result;
    }

    private void saveAuditLog(ProceedingJoinPoint joinPoint, Auditable auditable, Object result) {
        // --- Extract User ---
        String username = "SYSTEM";
        String userRole = "SYSTEM";
        try {
            AppUser user = currentUserService.getCurrentUser();
            if (user != null) {
                username = user.getUsername();
                userRole = user.getRoles().stream()
                        .map(r -> r.getName())
                        .reduce((first, second) -> second) // Get last role
                        .orElse("USER");
            }
        } catch (Exception ignored) {}

        // --- Extract HTTP Context (IP, Browser, OS) ---
        String ipAddress = "N/A";
        String userAgent = "N/A";
        String deviceType = "DESKTOP";

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            ipAddress = request.getHeader("X-Forwarded-For");
            if (ipAddress == null || ipAddress.isEmpty()) {
                ipAddress = request.getRemoteAddr();
            }

            userAgent = request.getHeader("User-Agent");
            if (userAgent != null && userAgent.toLowerCase().contains("mobile")) {
                deviceType = "MOBILE";
            } else if (userAgent != null && userAgent.toLowerCase().contains("tablet")) {
                deviceType = "TABLET";
            }
        }

        // --- Serialize Method Arguments (New Value) ---
        String newValueJson = "{}";
        try {
            // Filter out standard Spring/Servlet objects from serialization to prevent infinite loops
            Object[] args = joinPoint.getArgs();
            Object[] serializableArgs = Arrays.stream(args)
                    .filter(arg -> !(arg instanceof HttpServletRequest) &&
                            !(arg instanceof jakarta.servlet.http.HttpServletResponse) &&
                            !(arg instanceof org.springframework.ui.Model))
                    .toArray();
            newValueJson = objectMapper.writeValueAsString(serializableArgs);
        } catch (Exception e) {
            newValueJson = "Error serializing arguments: " + e.getMessage();
        }

        // --- Build and Save Log ---
        AuditLog auditLog = AuditLog.builder()
                .action(auditable.action())
                .module(auditable.module())
                .entityType(auditable.entityType())
                .performedBy(username)
                .userRole(userRole)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .deviceType(deviceType)
                .newValue(newValueJson)
                .remarks(auditable.remarks())
                .build();

        auditService.logActivity(auditLog);
    }
}