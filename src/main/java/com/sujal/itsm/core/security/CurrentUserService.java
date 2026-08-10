package com.sujal.itsm.core.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.core.user.repository.AppUserRepository;

@Service
public class CurrentUserService {

  private final AppUserRepository appUserRepository;

  public CurrentUserService(AppUserRepository appUserRepository) {
    this.appUserRepository = appUserRepository;
  }

  public AppUser getCurrentUser() {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    return appUserRepository
        .findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("Logged in user not found: " + username));
  }
}
