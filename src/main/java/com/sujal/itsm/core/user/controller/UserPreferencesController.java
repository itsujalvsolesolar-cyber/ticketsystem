package com.sujal.itsm.core.user.controller;

import com.sujal.itsm.core.enums.ThemePreference;
import com.sujal.itsm.core.user.model.UserPreferences;
import com.sujal.itsm.core.user.service.UserPreferencesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/me/preferences")
@RequiredArgsConstructor
public class UserPreferencesController {

    private final UserPreferencesService preferencesService;

    @GetMapping
    public ResponseEntity<UserPreferences> get() {
        return ResponseEntity.ok(preferencesService.getForCurrentUser());
    }

    @PutMapping("/theme")
    public ResponseEntity<Map<String, Object>> setTheme(@RequestBody Map<String, String> body) {
        ThemePreference theme = ThemePreference.valueOf(body.getOrDefault("theme", "SYSTEM").toUpperCase());
        UserPreferences prefs = preferencesService.updateTheme(theme);
        return ResponseEntity.ok(Map.of("status", "ok", "theme", prefs.getTheme().name()));
    }

    @PutMapping("/sidebar")
    public ResponseEntity<Map<String, Object>> setSidebar(@RequestBody Map<String, Boolean> body) {
        boolean collapsed = Boolean.TRUE.equals(body.get("collapsed"));
        UserPreferences prefs = preferencesService.updateSidebarCollapsed(collapsed);
        return ResponseEntity.ok(Map.of("status", "ok", "sidebarCollapsed", prefs.isSidebarCollapsed()));
    }
}