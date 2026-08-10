package com.sujal.itsm.core.admin.service;

import com.sujal.itsm.core.admin.model.SystemSetting;
import com.sujal.itsm.core.admin.repository.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemConfigurationService {

    private final SystemSettingRepository settingRepository;

    @Cacheable(value = "systemSettings", key = "#category + ':' + #key")
    public String getSetting(String category, String key) {
        return settingRepository.findByCategoryAndKey(category, key)
                .map(SystemSetting::getValue)
                .orElse(null);
    }

    public String getSetting(String category, String key, String defaultValue) {
        String value = getSetting(category, key);
        return value != null ? value : defaultValue;
    }

    @Transactional
    @CacheEvict(value = "systemSettings", key = "#category + ':' + #key")
    public void updateSetting(String category, String key, String value, String dataType, String description) {
        Optional<SystemSetting> existing = settingRepository.findByCategoryAndKey(category, key);

        if (existing.isPresent()) {
            SystemSetting setting = existing.get();
            setting.setValue(value);
            setting.setDataType(dataType);
            setting.setDescription(description);
            settingRepository.save(setting);
            log.info("✅ Updated setting: {}.{} = {}", category, key, value);
        } else {
            SystemSetting newSetting = SystemSetting.builder()
                    .category(category)
                    .key(key)
                    .value(value)
                    .dataType(dataType)
                    .description(description)
                    .build();
            settingRepository.save(newSetting);
            log.info("✅ Created new setting: {}.{} = {}", category, key, value);
        }
    }

    public List<SystemSetting> getSettingsByCategory(String category) {
        return settingRepository.findAllByCategoryOrdered(category);
    }

    @CacheEvict(value = "systemSettings", allEntries = true)
    public void clearCache() {
        log.info("🧹 System configuration cache cleared.");
    }
}