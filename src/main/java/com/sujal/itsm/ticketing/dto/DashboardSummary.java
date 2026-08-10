package com.sujal.itsm.ticketing.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardSummary {
  private long totalCount;
  private long openCount;
  private long resolvedCount;
  private long closedCount;
  private long archivedCount;
}
