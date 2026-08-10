package com.sujal.itsm.workflow.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sujal.itsm.workflow.model.ApprovalAction;
import com.sujal.itsm.workflow.model.ApprovalRequest;
import org.springframework.stereotype.Service;

import java.awt.Color; // ✅ Use standard Java Color instead of BaseColor
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class PdfCertificateService {

    public byte[] generateApprovalCertificate(ApprovalRequest request) throws Exception {
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        // Fonts (using java.awt.Color)
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
        Font hashFont = FontFactory.getFont(FontFactory.COURIER, 8, Color.GRAY);

        // Title
        Paragraph title = new Paragraph("ENTERPRISE APPROVAL CERTIFICATE", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Request Info Table
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingAfter(20);
        infoTable.addCell(getCell("Request ID: " + request.getId(), headerFont));
        infoTable.addCell(getCell("Module: " + request.getModuleType(), normalFont));
        infoTable.addCell(getCell("Requester: " + request.getRequester().getUsername(), normalFont));
        infoTable.addCell(getCell("Final Status: " + request.getStatus(), headerFont));
        document.add(infoTable);

        // Timeline / Actions
        Paragraph timelineTitle = new Paragraph("Approval Audit Trail", headerFont);
        timelineTitle.setSpacingBefore(10);
        timelineTitle.setSpacingAfter(10);
        document.add(timelineTitle);

        for (ApprovalAction action : request.getActions()) {
            PdfPTable actionTable = new PdfPTable(1);
            actionTable.setWidthPercentage(100);
            actionTable.setSpacingAfter(15);
            actionTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            String roleName = action.getApprover().getRoles().isEmpty() ? "Unknown" : action.getApprover().getRoles().iterator().next().getName();
            String actionText = String.format("%s by %s (%s) on %s",
                    action.getAction(),
                    action.getApprover().getUsername(),
                    roleName,
                    action.getSignedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))
            );

            PdfPCell actionCell = new PdfPCell(new Phrase(actionText, headerFont));
            actionCell.setBorder(Rectangle.BOTTOM);
            actionCell.setBorderColor(Color.LIGHT_GRAY); // ✅ Changed here
            actionCell.setPaddingBottom(5);
            actionTable.addCell(actionCell);

            // Signature
            if (action.getSignatureData() != null && !action.getSignatureData().isEmpty()) {
                if (action.getSignatureType().name().equals("DRAWN") && action.getSignatureData().startsWith("data:image")) {
                    String base64Image = action.getSignatureData().split(",")[1];
                    Image img = Image.getInstance(Base64.getDecoder().decode(base64Image));
                    img.scaleToFit(150, 50);
                    PdfPCell imgCell = new PdfPCell(img);
                    imgCell.setBorder(Rectangle.NO_BORDER);
                    imgCell.setPaddingTop(5);
                    actionTable.addCell(imgCell);
                } else {
                    PdfPCell sigCell = new PdfPCell(new Phrase("Typed Signature: " + action.getSignatureData(), normalFont));
                    sigCell.setBorder(Rectangle.NO_BORDER);
                    actionTable.addCell(sigCell);
                }
            }

            // Metadata
            PdfPCell metaCell = new PdfPCell(new Phrase(
                    String.format("Device: %s | %s | IP: %s", action.getDeviceType(), action.getBrowser(), action.getIpAddress()),
                    normalFont
            ));
            metaCell.setBorder(Rectangle.NO_BORDER);
            metaCell.setPaddingTop(5);
            actionTable.addCell(metaCell);

            // Remarks
            if (action.getRemarks() != null && !action.getRemarks().isEmpty()) {
                PdfPCell remarkCell = new PdfPCell(new Phrase("Remarks: " + action.getRemarks(), normalFont));
                remarkCell.setBorder(Rectangle.NO_BORDER);
                remarkCell.setPaddingTop(5);
                actionTable.addCell(remarkCell);
            }

            // Hash
            PdfPCell hashCell = new PdfPCell(new Phrase("Integrity Hash: " + action.getHashValue(), hashFont));
            hashCell.setBorder(Rectangle.NO_BORDER);
            hashCell.setPaddingTop(5);
            actionTable.addCell(hashCell);

            document.add(actionTable);
        }

        // Footer
        Paragraph footer = new Paragraph("This document is a cryptographically secured record of the approval workflow. Any alteration to this document invalidates the integrity hashes.", normalFont);
        footer.setSpacingBefore(30);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
        return out.toByteArray();
    }

    private PdfPCell getCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        return cell;
    }
}