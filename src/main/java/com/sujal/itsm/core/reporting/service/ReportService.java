package com.sujal.itsm.core.reporting.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.opencsv.CSVWriter;
import com.sujal.itsm.core.offboarding.model.AssetReturnRecord;
import com.sujal.itsm.core.offboarding.model.ClearanceChecklist;
import com.sujal.itsm.core.offboarding.model.OffboardingRequest;
import com.sujal.itsm.core.reporting.enums.ExportFormat;
import com.sujal.itsm.itams.model.Asset;
import com.sujal.itsm.itams.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.awt.Color; // ✅ 100% COMPATIBLE COLOR IMPORT
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final AssetRepository assetRepository;

    public void exportAssets(HttpServletResponse response, ExportFormat format) throws IOException {
        List<Asset> assets = assetRepository.findAll();
        String fileName = "Asset_Register_" + System.currentTimeMillis();

        switch (format) {
            case CSV:
                exportToCsv(response, assets, fileName + ".csv");
                break;
            case EXCEL:
                exportToExcel(response, assets, fileName + ".xlsx");
                break;
            case PDF:
                exportToPdf(response, assets, fileName + ".pdf");
                break;
        }
    }

    private void exportToCsv(HttpServletResponse response, List<Asset> assets, String fileName) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        try (Writer writer = new OutputStreamWriter(response.getOutputStream());
             CSVWriter csvWriter = new CSVWriter(writer)) {

            csvWriter.writeNext(new String[]{"Asset Tag", "Name", "Category", "Status", "Purchase Date", "Warranty End"});
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (Asset asset : assets) {
                csvWriter.writeNext(new String[]{
                        asset.getAssetTag(),
                        asset.getName(),
                        asset.getCategory() != null ? asset.getCategory().getName() : "N/A",
                        asset.getStatus().name(),
                        asset.getPurchaseDate() != null ? asset.getPurchaseDate().format(formatter) : "N/A",
                        asset.getWarrantyEndDate() != null ? asset.getWarrantyEndDate().format(formatter) : "N/A"
                });
            }
        }
        log.info("✅ Exported {} assets to CSV", assets.size());
    }

    private void exportToExcel(HttpServletResponse response, List<Asset> assets, String fileName) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Asset Register");
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Asset Tag", "Name", "Category", "Status", "Purchase Date", "Warranty End"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            int rowNum = 1;
            for (Asset asset : assets) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(asset.getAssetTag());
                row.createCell(1).setCellValue(asset.getName());
                row.createCell(2).setCellValue(asset.getCategory() != null ? asset.getCategory().getName() : "N/A");
                row.createCell(3).setCellValue(asset.getStatus().name());
                row.createCell(4).setCellValue(asset.getPurchaseDate() != null ? asset.getPurchaseDate().format(formatter) : "N/A");
                row.createCell(5).setCellValue(asset.getWarrantyEndDate() != null ? asset.getWarrantyEndDate().format(formatter) : "N/A");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(response.getOutputStream());
        }
        log.info("✅ Exported {} assets to Excel", assets.size());
    }

    private void exportToPdf(HttpServletResponse response, List<Asset> assets, String fileName) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        Document document = new Document(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            document.add(new Paragraph("Asset Register Report", titleFont));
            document.add(new Paragraph("Generated on: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 3f, 2f, 2f, 2f, 2f});

            // ✅ USING java.awt.Color (Fully supported by OpenPDF)
            com.lowagie.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            String[] headers = {"Asset Tag", "Name", "Category", "Status", "Purchase Date", "Warranty End"};

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(Color.DARK_GRAY); // ✅ Compatible fallback
                cell.setPadding(5);
                table.addCell(cell);
            }

            com.lowagie.text.Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (Asset asset : assets) {
                table.addCell(new PdfPCell(new Phrase(asset.getAssetTag(), dataFont)));
                table.addCell(new PdfPCell(new Phrase(asset.getName(), dataFont)));
                table.addCell(new PdfPCell(new Phrase(asset.getCategory() != null ? asset.getCategory().getName() : "N/A", dataFont)));
                table.addCell(new PdfPCell(new Phrase(asset.getStatus().name(), dataFont)));
                table.addCell(new PdfPCell(new Phrase(asset.getPurchaseDate() != null ? asset.getPurchaseDate().format(formatter) : "N/A", dataFont)));
                table.addCell(new PdfPCell(new Phrase(asset.getWarrantyEndDate() != null ? asset.getWarrantyEndDate().format(formatter) : "N/A", dataFont)));
            }

            document.add(table);
            document.close();
            log.info("✅ Exported {} assets to PDF", assets.size());

        } catch (DocumentException e) {
            throw new IOException("Error generating PDF", e);
        }
    }

    // Add this to ReportService.java
    public void generateExitClearancePdf(HttpServletResponse response, OffboardingRequest request,
                                         List<ClearanceChecklist> clearances, List<AssetReturnRecord> assets) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=Exit_Clearance_" + request.getEmployee().getUsername() + ".pdf");

        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, java.awt.Color.DARK_GRAY);
            document.add(new Paragraph("EMPLOYEE EXIT CLEARANCE REPORT", titleFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Employee: " + request.getEmployee().getFullName()));
            document.add(new Paragraph("Last Working Day: " + request.getLastWorkingDay()));
            document.add(new Paragraph("Status: COMPLETED"));
            document.add(new Paragraph(" "));

            // Add Clearances
            com.lowagie.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            document.add(new Paragraph("DEPARTMENTAL CLEARANCES", headerFont));

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.addCell("Department");
            table.addCell("Status");
            table.addCell("Cleared By / Signature");

            for (ClearanceChecklist c : clearances) {
                table.addCell(c.getDepartment().name());
                table.addCell(c.getStatus().name());
                table.addCell(c.getClearedBy() != null ? c.getClearedBy().getFullName() : "N/A");
                // Note: Adding Base64 images to OpenPDF requires decoding to byte array.
                // For simplicity, we just list the name here. You can add image rendering if needed.
            }
            document.add(table);

            document.close();
            log.info("✅ Generated Exit Clearance PDF for {}", request.getEmployee().getUsername());
        } catch (DocumentException e) {
            throw new IOException("Error generating PDF", e);
        }
    }
}