package com.sujal.itsm.itams.repository;

import com.sujal.itsm.itams.model.QRCodeTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface QRCodeTemplateRepository extends JpaRepository<QRCodeTemplate, Long> {
    Optional<QRCodeTemplate> findByIsDefaultTrue();

    default QRCodeTemplate getDefaultTemplate() {
        return findByIsDefaultTrue().orElseGet(() -> {
            QRCodeTemplate template = new QRCodeTemplate();
            return save(template);
        });
    }
}