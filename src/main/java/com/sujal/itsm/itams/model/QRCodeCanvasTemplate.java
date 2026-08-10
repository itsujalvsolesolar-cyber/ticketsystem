package com.sujal.itsm.itams.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "qr_code_canvas_templates")
public class QRCodeCanvasTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_name")
    private String templateName = "Default Canvas Template";

    @Column(name = "is_default")
    private boolean isDefault = true;

    @Column(name = "canvas_width")
    private Integer canvasWidth = 300;

    @Column(name = "canvas_height")
    private Integer canvasHeight = 200;

    @Column(name = "background_color")
    private String backgroundColor = "#ffffff";

    @Column(name = "canvas_size")
    private String canvasSize = "medium";

    @Column(name = "elements_json")
    @Lob
    private String elementsJson;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}