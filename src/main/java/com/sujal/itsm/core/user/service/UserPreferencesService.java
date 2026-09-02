package com.sujal.itsm.core.user.service;

import com.sujal.itsm.core.enums.ThemePreference;
import com.sujal.itsm.core.security.CurrentUserService;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.core.user.model.UserPreferences;
import com.sujal.itsm.core.user.repository.UserPreferencesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserPreferencesService {

    private final UserPreferencesRepository preferencesRepository;
    private final CurrentUserService currentUserService;

    /** Returns the current user's preferences, creating defaults on first access. */
    @Transactional
    public UserPreferences getForCurrentUser() {
        AppUser user = currentUserService.getCurrentUser();
        return preferencesRepository.findByUserId(user.getId())
                .orElseGet(() -> preferencesRepository.save(UserPreferences.builder().user(user).build()));
    }

    @Transactional
    public UserPreferences updateTheme(ThemePreference theme) {
        UserPreferences prefs = getForCurrentUser();
        prefs.setTheme(theme);
        return preferencesRepository.save(prefs);
    }

    @Transactional
    public UserPreferences updateSidebarCollapsed(boolean collapsed) {
        UserPreferences prefs = getForCurrentUser();
        prefs.setSidebarCollapsed(collapsed);
        return preferencesRepository.save(prefs);
    }
}