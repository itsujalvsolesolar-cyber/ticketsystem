package com.sujal.itsm.core.admin.repository;

import com.sujal.itsm.core.admin.model.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, Long> {

    Optional<SystemSetting> findByCategoryAndKey(String category, String key);

    List<SystemSetting> findByCategory(String category);

    @Query("SELECT s FROM SystemSetting s WHERE s.category = :category ORDER BY s.key ASC")
    List<SystemSetting> findAllByCategoryOrdered(String category);
}