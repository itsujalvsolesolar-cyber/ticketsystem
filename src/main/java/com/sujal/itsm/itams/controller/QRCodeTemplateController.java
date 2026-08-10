package com.sujal.itsm.itams.controller;

import com.sujal.itsm.itams.model.QRCodeTemplate;
import com.sujal.itsm.itams.repository.QRCodeTemplateRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/itams/assets/api/qr-template")
public class QRCodeTemplateController {

    private final QRCodeTemplateRepository templateRepository;

    public QRCodeTemplateController(QRCodeTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @GetMapping("/default")
    public ResponseEntity<QRCodeTemplate> getDefaultTemplate() {
        QRCodeTemplate template = templateRepository.getDefaultTemplate();
        return ResponseEntity.ok(template);
    }

    @PostMapping
    public ResponseEntity<QRCodeTemplate> saveTemplate(@RequestBody QRCodeTemplate template) {
        // Set all other templates as non-default
        templateRepository.findAll().forEach(t -> {
            if (t.isDefault()) {
                t.setDefault(false);
                templateRepository.save(t);
            }
        });

        // Save new template as default
        template.setDefault(true);
        QRCodeTemplate saved = templateRepository.save(template);
        return ResponseEntity.ok(saved);
    }
}