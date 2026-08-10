package com.sujal.itsm.core.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.sujal.itsm.core.security.CurrentUserService;
import com.sujal.itsm.core.user.model.AppUser;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Global UI Controller Advice Injects common UI variables into every Thymeleaf model. This
 * eliminates the need to manually add these attributes in every controller.
 */
@ControllerAdvice
public class GlobalUIControllerAdvice {

  private final CurrentUserService currentUserService;

  public GlobalUIControllerAdvice(CurrentUserService currentUserService) {
    this.currentUserService = currentUserService;
  }

  /**
   * Injects the current request URI into every Thymeleaf model. Used for active state highlighting
   * in the sidebar.
   */
  @ModelAttribute("currentUri")
  public String currentUri(HttpServletRequest request) {
    return request.getRequestURI();
  }

  /**
   * Injects the current authenticated user into every Thymeleaf model. Returns null if the user is
   * not authenticated (e.g., on login page).
   */
  @ModelAttribute("currentUser")
  public AppUser currentUser() {
    try {
      return currentUserService.getCurrentUser();
    } catch (Exception e) {
      // User not authenticated (e.g., on login page)
      return null;
    }
  }
}
