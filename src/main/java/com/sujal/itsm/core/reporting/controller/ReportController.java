package com.sujal.itsm.core.reporting.controller;

import com.sujal.itsm.core.reporting.enums.ExportFormat;
import com.sujal.itsm.core.reporting.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/assets")
    public void exportAssets(@RequestParam(defaultValue = "EXCEL") ExportFormat format, HttpServletResponse response) throws IOException {
        reportService.exportAssets(response, format);
    }

    // Future: Add endpoints for /api/reports/tickets, /api/reports/employees, etc.
}