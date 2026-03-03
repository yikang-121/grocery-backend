package com.grocery.grocerybackend.service;

import com.grocery.grocerybackend.dto.MonthlySummary;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

@Service
public class ReportExportService {

    public byte[] generateCsv(MonthlySummary summary) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);

        CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT
                .withHeader("Month", "Year", "Total Revenue (RM)", "Spoilage Loss (RM)",
                        "Net Performance (RM)", "Loss Ratio (%)", "Orders Count", "Spoilage Count"));

        BigDecimal net = summary.getTotalRevenue().subtract(summary.getTotalSpoilageLoss());
        BigDecimal lossRatio = summary.getTotalRevenue().compareTo(BigDecimal.ZERO) > 0
                ? summary.getTotalSpoilageLoss()
                        .divide(summary.getTotalRevenue(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        String monthName = Month.of(summary.getMonth()).getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        printer.printRecord(
                monthName,
                summary.getYear(),
                summary.getTotalRevenue().setScale(2, RoundingMode.HALF_UP),
                summary.getTotalSpoilageLoss().setScale(2, RoundingMode.HALF_UP),
                net.setScale(2, RoundingMode.HALF_UP),
                lossRatio,
                summary.getOrdersCount(),
                summary.getSpoilageCount());

        printer.flush();
        writer.flush();
        return out.toByteArray();
    }

    public byte[] generatePdf(MonthlySummary summary) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter pdfWriter = new PdfWriter(out);
        PdfDocument pdfDoc = new PdfDocument(pdfWriter);
        Document doc = new Document(pdfDoc);

        String monthName = Month.of(summary.getMonth()).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String title = "Financial Report — " + monthName + " " + summary.getYear();

        // Title
        doc.add(new Paragraph(title)
                .setFontSize(22)
                .setBold()
                .setFontColor(new DeviceRgb(31, 41, 55))
                .setMarginBottom(5));

        // Subtitle
        doc.add(new Paragraph("Monthly Financial Summary")
                .setFontSize(12)
                .setFontColor(new DeviceRgb(107, 114, 128))
                .setMarginBottom(20));

        // Compute derived values
        BigDecimal net = summary.getTotalRevenue().subtract(summary.getTotalSpoilageLoss());
        BigDecimal lossRatio = summary.getTotalRevenue().compareTo(BigDecimal.ZERO) > 0
                ? summary.getTotalSpoilageLoss()
                        .divide(summary.getTotalRevenue(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Summary Table
        Table table = new Table(UnitValue.createPercentArray(new float[] { 1, 1 }))
                .useAllAvailableWidth()
                .setMarginBottom(20);

        DeviceRgb headerBg = new DeviceRgb(16, 185, 129); // emerald
        DeviceRgb headerText = new DeviceRgb(255, 255, 255);

        table.addHeaderCell(new Cell().add(new Paragraph("Metric").setBold().setFontColor(headerText))
                .setBackgroundColor(headerBg).setPadding(8));
        table.addHeaderCell(new Cell().add(new Paragraph("Value").setBold().setFontColor(headerText))
                .setBackgroundColor(headerBg).setPadding(8));

        addRow(table, "Total Revenue", "RM " + summary.getTotalRevenue().setScale(2, RoundingMode.HALF_UP));
        addRow(table, "Spoilage Loss", "RM " + summary.getTotalSpoilageLoss().setScale(2, RoundingMode.HALF_UP));
        addRow(table, "Net Performance", "RM " + net.setScale(2, RoundingMode.HALF_UP));
        addRow(table, "Loss Ratio", lossRatio + "%");
        addRow(table, "Orders Count", String.valueOf(summary.getOrdersCount()));
        addRow(table, "Spoilage Count", String.valueOf(summary.getSpoilageCount()));

        doc.add(table);

        // Footer
        doc.add(new Paragraph("Generated on " + java.time.LocalDate.now())
                .setFontSize(9)
                .setFontColor(new DeviceRgb(156, 163, 175))
                .setTextAlignment(TextAlignment.RIGHT));

        doc.close();
        return out.toByteArray();
    }

    private void addRow(Table table, String label, String value) {
        DeviceRgb labelColor = new DeviceRgb(55, 65, 81);
        DeviceRgb valueColor = new DeviceRgb(17, 24, 39);

        table.addCell(new Cell().add(new Paragraph(label).setFontColor(labelColor)).setPadding(8));
        table.addCell(new Cell().add(new Paragraph(value).setBold().setFontColor(valueColor)).setPadding(8));
    }
}
