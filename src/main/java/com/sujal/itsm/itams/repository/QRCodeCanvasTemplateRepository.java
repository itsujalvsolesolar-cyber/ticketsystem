package com.sujal.itsm.itams.repository;

import com.sujal.itsm.itams.model.QRCodeCanvasTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface QRCodeCanvasTemplateRepository extends JpaRepository<QRCodeCanvasTemplate, Long> {
    Optional<QRCodeCanvasTemplate> findByIsDefaultTrue();

    default QRCodeCanvasTemplate getDefaultTemplate() {
        return findByIsDefaultTrue().orElseGet(() -> {
            QRCodeCanvasTemplate template = new QRCodeCanvasTemplate();
            return save(template);
        });
    }
}