package com.sujal.itsm.core.config;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sujal.itsm.core.exception.*;
import com.sujal.itsm.ticketing.exception.TicketNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(TicketNotFoundException.class)
  public String handleTicketNotFound(
      TicketNotFoundException ex, RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
    return "redirect:/";
  }

  @ExceptionHandler(UserNotFoundException.class)
  public String handleUserNotFound(
      UserNotFoundException ex, RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("error", ex.getMessage());
    return "redirect:/admin/settings";
  }

  @ExceptionHandler(DuplicateResourceException.class)
  public String handleDuplicateResource(
      DuplicateResourceException ex, RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("error", ex.getMessage());
    return "redirect:/admin/settings";
  }

  @ExceptionHandler(FileStorageException.class)
  public String handleFileStorageException(FileStorageException ex, Model model) {
    model.addAttribute("error", "File operation failed: " + ex.getMessage());
    return "error";
  }

  @ExceptionHandler(Exception.class)
  public String handleGeneralException(Exception ex, Model model) {
    model.addAttribute("error", "An unexpected error occurred. Please contact support.");
    return "error";
  }

  @ExceptionHandler(AlreadyClockedInException.class)
  public String handleAlreadyClockedIn(
      AlreadyClockedInException ex, RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("warning", ex.getMessage());
    return "redirect:/shift/status";
  }

  @ExceptionHandler(NotClockedInException.class)
  public String handleNotClockedIn(
      NotClockedInException ex, RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("error", ex.getMessage());
    return "redirect:/shift/status";
  }
}
