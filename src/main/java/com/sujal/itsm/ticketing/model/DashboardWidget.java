package com.sujal.itsm.ticketing.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "dashboard_widgets")
@Data
public class DashboardWidget {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Type of widget: "STAT_CARD", "CHART", "RECENT_LIST"
  private String widgetType;

  private String title;

  // Store configuration as JSON string: '{"color": "primary", "icon": "bi-ticket"}'
  @Column(columnDefinition = "TEXT")
  private String configJson;

  private int sortOrder;
  private boolean isActive = true;
}
