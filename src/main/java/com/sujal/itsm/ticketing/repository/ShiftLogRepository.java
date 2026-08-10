package com.sujal.itsm.ticketing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sujal.itsm.ticketing.model.ShiftLog;

@Repository
public interface ShiftLogRepository extends JpaRepository<ShiftLog, Long> {

  Optional<ShiftLog> findByUserIdAndClockOutTimeIsNull(Long userId);

  List<ShiftLog> findByUserIdOrderByClockInTimeDesc(Long userId); // ✅ NEW: Ordered by most recent
}
