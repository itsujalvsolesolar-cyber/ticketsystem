package com.sujal.itsm.ticketing.dto;

import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.core.user.model.Department;
import com.sujal.itsm.core.user.model.Role; // ✅ Import Role instead of UserRole
import com.sujal.itsm.ticketing.model.Category;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AdminSettingsView {
  private AppUser currentUser;
  private List<Department> departments;
  private List<Category> categories;
  private List<AppUser> users;

  // ✅ CHANGE THIS LINE:
  private List<Role> availableRoles;
}