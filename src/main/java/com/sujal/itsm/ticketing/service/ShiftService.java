package com.sujal.itsm.ticketing.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sujal.itsm.core.exception.AlreadyClockedInException;
import com.sujal.itsm.core.exception.NotClockedInException;
import com.sujal.itsm.core.security.CurrentUserService;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.ticketing.dto.ShiftStatusView;
import com.sujal.itsm.ticketing.model.ShiftLog;
import com.sujal.itsm.ticketing.repository.ShiftLogRepository;

/**
 * Enterprise Shift Service Handles all shift-related business logic: clock-in, clock-out, and
 * status retrieval.
 *
 * <p>Key improvements over controller-based logic: - Centralized validation (no duplicate checks
 * scattered across methods) - Transactional integrity (all-or-nothing operations) - Testable in
 * isolation (no web dependencies) - Clean separation of concerns - Audit logging for compliance
 *
 * @author Enterprise Architecture Team
 * @version 2.0.0
 */
@Service
@Transactional
public class ShiftService {

  private static final Logger logger = LoggerFactory.getLogger(ShiftService.class);

  private final ShiftLogRepository shiftLogRepository;
  private final CurrentUserService currentUserService;

  public ShiftService(
      ShiftLogRepository shiftLogRepository, CurrentUserService currentUserService) {
    this.shiftLogRepository = shiftLogRepository;
    this.currentUserService = currentUserService;
  }

  // ============================================
  // CLOCK IN
  // ============================================

  /**
   * Clocks in the current user.
   *
   * @throws AlreadyClockedInException if user already has an active shift
   */
  public ShiftLog clockIn() {
    AppUser user = currentUserService.getCurrentUser();

    // Validate: check if already clocked in
    if (getActiveShift(user.getId()).isPresent()) {
      throw new AlreadyClockedInException();
    }

    // Create new shift log
    ShiftLog shiftLog = ShiftLog.builder().user(user).clockInTime(LocalDateTime.now()).build();

    ShiftLog saved = shiftLogRepository.save(shiftLog);
    logger.info("User {} clocked in at {}", user.getUsername(), saved.getClockInTime());
    return saved;
  }

  // ============================================
  // CLOCK OUT
  // ============================================

  /**
   * Clocks out the current user.
   *
   * @throws NotClockedInException if user doesn't have an active shift
   */
  public ShiftLog clockOut() {
    AppUser user = currentUserService.getCurrentUser();

    // Validate: must be clocked in
    ShiftLog activeShift = getActiveShift(user.getId()).orElseThrow(NotClockedInException::new);

    // Close the shift
    activeShift.closeShift();
    ShiftLog saved = shiftLogRepository.save(activeShift);

    logger.info("User {} clocked out at {}", user.getUsername(), saved.getClockOutTime());
    return saved;
  }

  // ============================================
  // SHIFT STATUS
  // ============================================

  /**
   * Retrieves the shift status for the current user. Returns a DTO containing the active shift (if
   * any) and recent shifts.
   */
  @Transactional(readOnly = true)
  public ShiftStatusView getShiftStatus() {
    AppUser user = currentUserService.getCurrentUser();

    // Get current active shift
    ShiftLog activeShift = getActiveShift(user.getId()).orElse(null);

    // Get recent shifts (ordered by clock-in time descending)
    List<ShiftLog> recentShifts =
        shiftLogRepository.findByUserIdOrderByClockInTimeDesc(user.getId());

    return ShiftStatusView.builder()
        .currentUser(user)
        .activeShift(activeShift)
        .recentShifts(recentShifts)
        .build();
  }

  // ============================================
  // PRIVATE HELPERS
  // ============================================

  /** Finds the active shift for a user (clocked in but not out). */
  private Optional<ShiftLog> getActiveShift(Long userId) {
    return shiftLogRepository.findByUserIdAndClockOutTimeIsNull(userId);
  }
}
