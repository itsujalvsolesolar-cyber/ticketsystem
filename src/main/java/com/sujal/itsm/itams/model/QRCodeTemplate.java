package com.sujal.itsm.itams.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "qr_code_templates")
public class QRCodeTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_name")
    private String templateName = "Default QR Template";

    @Column(name = "is_default")
    private boolean isDefault = true;

    // Layout settings
    @Column(name = "label_size")
    private String labelSize = "medium"; // small, medium, large, xl

    @Column(name = "color_scheme")
    private String colorScheme = "default"; // default, dark, minimal, corporate

    @Column(name = "width_px")
    private Integer widthPx = 300;

    @Column(name = "height_px")
    private Integer heightPx = 200;

    // Show/hide fields
    @Column(name = "show_logo")
    private Boolean showLogo = true;

    @Column(name = "show_qr_code")
    private Boolean showQrCode = true;

    @Column(name = "show_asset_name")
    private Boolean showAssetName = true;

    @Column(name = "show_asset_tag")
    private Boolean showAssetTag = true;

    @Column(name = "show_serial_number")
    private Boolean showSerialNumber = true;

    @Column(name = "show_category")
    private Boolean showCategory = false;

    @Column(name = "show_department")
    private Boolean showDepartment = false;

    @Column(name = "show_location")
    private Boolean showLocation = false;

    @Column(name = "show_purchase_date")
    private Boolean showPurchaseDate = false;

    @Column(name = "show_warranty")
    private Boolean showWarranty = false;

    @Column(name = "show_status")
    private Boolean showStatus = false;

    @Column(name = "show_assigned_to")
    private Boolean showAssignedTo = false;

    @Column(name = "show_barcode")
    private Boolean showBarcode = false;

    // Custom text
    @Column(name = "company_name")
    private String companyName = "YOUR COMPANY";

    @Column(name = "company_logo_url")
    private String companyLogoUrl;

    @Column(name = "footer_text")
    private String footerText = "Asset Management System";

    @Column(name = "custom_css")
    @Lob
    private String customCss;

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