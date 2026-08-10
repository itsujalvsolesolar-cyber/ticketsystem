package com.sujal.itsm.core.user.repository;

import com.sujal.itsm.core.user.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    // ✅ ADD THIS LINE to prevent duplicate role creation
    boolean existsByName(String name);
}