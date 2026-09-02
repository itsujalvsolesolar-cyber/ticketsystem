package com.sujal.itsm.core.config;

import com.sujal.itsm.core.user.model.UserPreferences;
import com.sujal.itsm.core.user.service.UserPreferencesService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class PreferencesUiAdvice {

    private final UserPreferencesService preferencesService;

    @ModelAttribute("userPrefs")
    public UserPreferences userPrefs(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return null; // login page falls back to localStorage
        }
        try {
            return preferencesService.getForCurrentUser();
        } catch (Exception e) {
            return null;
        }
    }
}