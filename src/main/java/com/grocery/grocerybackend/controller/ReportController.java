package com.grocery.grocerybackend.controller;

import com.grocery.grocerybackend.dto.MonthlySummary;
import com.grocery.grocerybackend.service.FinancialService;
import com.grocery.grocerybackend.service.ReportExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

@RestController
@RequestMapping("/api/admin/reports")
@CrossOrigin(origins = "http://localhost:3000")
public class ReportController {

    private final FinancialService financialService;
    private final ReportExportService reportExportService;

    public ReportController(FinancialService financialService, ReportExportService reportExportService) {
        this.financialService = financialService;
        this.reportExportService = reportExportService;
    }

    @GetMapping("/monthly-summary")
    public MonthlySummary getSummary(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        int y = (year != null) ? year : LocalDate.now().getYear();
        int m = (month != null) ? month : LocalDate.now().getMonthValue();

        return financialService.getMonthlySummary(y, m);
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) throws IOException {

        int y = (year != null) ? year : LocalDate.now().getYear();
        int m = (month != null) ? month : LocalDate.now().getMonthValue();

        MonthlySummary summary = financialService.getMonthlySummary(y, m);
        byte[] csv = reportExportService.generateCsv(summary);

        String monthName = Month.of(m).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String filename = "Financial_Report_" + monthName + "_" + y + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) throws IOException {

        int y = (year != null) ? year : LocalDate.now().getYear();
        int m = (month != null) ? month : LocalDate.now().getMonthValue();

        MonthlySummary summary = financialService.getMonthlySummary(y, m);
        byte[] pdf = reportExportService.generatePdf(summary);

        String monthName = Month.of(m).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String filename = "Financial_Report_" + monthName + "_" + y + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
