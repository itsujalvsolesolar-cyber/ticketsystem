package com.sujal.itsm.ticketing.controller;

import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sujal.itsm.core.exception.AlreadyClockedInException;
import com.sujal.itsm.core.exception.NotClockedInException;
import com.sujal.itsm.ticketing.dto.ShiftStatusView;
import com.sujal.itsm.ticketing.model.ShiftLog;
import com.sujal.itsm.ticketing.service.ShiftService;

/**
 * Enterprise Shift Controller Handles shift clock-in/clock-out and status display.
 *
 * <p>This controller is now a thin orchestration layer. ALL business logic is delegated to
 * ShiftService.
 *
 * <p>Before: 2 repositories injected, 80+ lines, business logic scattered After: 1 service
 * injected, ~50 lines, pure HTTP orchestration
 *
 * @author Enterprise Architecture Team
 * @version 2.0.0
 */
@Controller
@RequestMapping("/shift")
public class ShiftController {

  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

  private final ShiftService shiftService;

  public ShiftController(ShiftService shiftService) {
    this.shiftService = shiftService;
  }

  // ============================================
  // CLOCK IN
  // ============================================

  @PostMapping("/clock-in")
  public String clockIn(RedirectAttributes redirectAttributes) {
    try {
      ShiftLog shiftLog = shiftService.clockIn();
      redirectAttributes.addFlashAttribute(
          "success",
          "Successfully clocked in at " + shiftLog.getClockInTime().format(TIME_FORMATTER));
    } catch (AlreadyClockedInException e) {
      redirectAttributes.addFlashAttribute("warning", e.getMessage());
    }
    return "redirect:/shift/status";
  }

  // ============================================
  // CLOCK OUT
  // ============================================

  @PostMapping("/clock-out")
  public String clockOut(RedirectAttributes redirectAttributes) {
    try {
      ShiftLog shiftLog = shiftService.clockOut();
      redirectAttributes.addFlashAttribute(
          "success",
          "Successfully clocked out at " + shiftLog.getClockOutTime().format(TIME_FORMATTER));
    } catch (NotClockedInException e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/shift/status";
  }

  // ============================================
  // SHIFT STATUS PAGE
  // ============================================

  @GetMapping("/status")
  public String shiftStatus(Model model) {
    ShiftStatusView view = shiftService.getShiftStatus();
    model.addAllAttributes(view.toModel());
    return "shift-status";
  }
}
