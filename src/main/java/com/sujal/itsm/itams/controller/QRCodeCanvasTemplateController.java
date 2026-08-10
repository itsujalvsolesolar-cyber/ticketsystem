package com.sujal.itsm.itams.controller;

import com.sujal.itsm.itams.model.QRCodeCanvasTemplate;
import com.sujal.itsm.itams.repository.QRCodeCanvasTemplateRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/itams/assets/api/qr-canvas-template")
public class QRCodeCanvasTemplateController {

    private final QRCodeCanvasTemplateRepository templateRepository;

    public QRCodeCanvasTemplateController(QRCodeCanvasTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @GetMapping("/default")
    public ResponseEntity<QRCodeCanvasTemplate> getDefaultTemplate() {
        return ResponseEntity.ok(templateRepository.getDefaultTemplate());
    }

    @PostMapping
    public ResponseEntity<QRCodeCanvasTemplate> saveTemplate(@RequestBody QRCodeCanvasTemplate template) {
        // 1. Set all existing templates as non-default
        templateRepository.findAll().forEach(t -> {
            if (t.isDefault()) {
                t.setDefault(false);
                templateRepository.save(t);
            }
        });

        // 2. Save the new template as the default
        template.setDefault(true);
        QRCodeCanvasTemplate saved = templateRepository.save(template);
        return ResponseEntity.ok(saved);
    }
}