package com.grocery.grocerybackend.service;

import com.grocery.grocerybackend.dto.MonthlySummary;
import com.grocery.grocerybackend.entity.Product;
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
import java.util.List;
import java.util.Locale;

@Service
public class ReportExportService {

        public byte[] generateCsv(MonthlySummary summary) throws IOException {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);

                BigDecimal net = summary.getTotalRevenue().subtract(summary.getTotalSpoilageLoss());
                BigDecimal lossRatio = summary.getTotalRevenue().compareTo(BigDecimal.ZERO) > 0
                                ? summary.getTotalSpoilageLoss()
                                                .divide(summary.getTotalRevenue(), 4, RoundingMode.HALF_UP)
                                                .multiply(BigDecimal.valueOf(100))
                                                .setScale(2, RoundingMode.HALF_UP)
                                : BigDecimal.ZERO;

                String monthName = Month.of(summary.getMonth()).getDisplayName(TextStyle.FULL, Locale.ENGLISH);

                String[] headers = { "Month", "Year", "Total Revenue (RM)", "Spoilage Loss (RM)",
                                "Net Performance (RM)", "Loss Ratio (%)", "Orders Count", "Spoilage Count" };
                CSVFormat format = CSVFormat.DEFAULT.builder().setHeader(headers).build();

                try (CSVPrinter printer = new CSVPrinter(writer, format)) {
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
                }
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
                addRow(table, "Spoilage Loss",
                                "RM " + summary.getTotalSpoilageLoss().setScale(2, RoundingMode.HALF_UP));
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

        public byte[] generateStockCsv(List<Product> products) throws IOException {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);

                String[] headers = { "Sort", "Name", "Specifications", "QTY", "Unit price (RM)", "Amount (RM)",
                                "Safety Stock", "Market price (RM)", "Remake" };
                CSVFormat format = CSVFormat.DEFAULT.builder().setHeader(headers).build();

                try (CSVPrinter printer = new CSVPrinter(writer, format)) {
                        for (int i = 0; i < products.size(); i++) {
                                Product p = products.get(i);
                                BigDecimal unitPrice = p.getCostPrice() != null ? p.getCostPrice() : p.getPrice();
                                int qty = p.getStockQuantity() != null ? p.getStockQuantity() : 0;
                                BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(qty));

                                printer.printRecord(
                                                i + 1,
                                                p.getName(),
                                                p.getCategory() != null ? p.getCategory() : "-",
                                                qty,
                                                unitPrice.setScale(2, RoundingMode.HALF_UP),
                                                amount.setScale(2, RoundingMode.HALF_UP),
                                                10, // Safety Stock placeholder
                                                p.getPrice().setScale(2, RoundingMode.HALF_UP),
                                                "" // Remake placeholder
                                );
                        }
                        printer.flush();
                }
                writer.flush();
                return out.toByteArray();
        }

        public byte[] generateStockPdf(List<Product> products) throws IOException {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                PdfWriter pdfWriter = new PdfWriter(out);
                PdfDocument pdfDoc = new PdfDocument(pdfWriter);
                Document doc = new Document(pdfDoc);

                doc.add(new Paragraph("Stock Out & Sales Report - Inventory Breakdown")
                                .setFontSize(18)
                                .setBold()
                                .setFontColor(new DeviceRgb(31, 41, 55))
                                .setMarginBottom(20));

                Table table = new Table(UnitValue.createPercentArray(new float[] { 1, 3, 2, 1, 2, 2, 1, 2, 1 }))
                                .useAllAvailableWidth()
                                .setMarginBottom(20);

                DeviceRgb headerBg = new DeviceRgb(79, 70, 229); // Indigo
                DeviceRgb headerText = new DeviceRgb(255, 255, 255);

                String[] headers = { "Sort", "Name", "Specs", "QTY", "Unit Price", "Amount", "Safety", "Market",
                                "Remake" };
                for (String h : headers) {
                        table.addHeaderCell(new Cell()
                                        .add(new Paragraph(h).setBold().setFontSize(8).setFontColor(headerText))
                                        .setBackgroundColor(headerBg).setPadding(4));
                }

                for (int i = 0; i < products.size(); i++) {
                        Product p = products.get(i);
                        BigDecimal unitPrice = p.getCostPrice() != null ? p.getCostPrice() : p.getPrice();
                        int qty = p.getStockQuantity() != null ? p.getStockQuantity() : 0;
                        BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(qty));

                        table.addCell(new Cell().add(new Paragraph(String.valueOf(i + 1)).setFontSize(8)));
                        table.addCell(new Cell().add(new Paragraph(p.getName()).setFontSize(8)));
                        table.addCell(new Cell().add(
                                        new Paragraph(p.getCategory() != null ? p.getCategory() : "-").setFontSize(8)));
                        table.addCell(new Cell().add(new Paragraph(String.valueOf(qty)).setFontSize(8)));
                        table.addCell(new Cell().add(new Paragraph("RM " + unitPrice.setScale(2, RoundingMode.HALF_UP))
                                        .setFontSize(8)));
                        table.addCell(new Cell().add(new Paragraph("RM " + amount.setScale(2, RoundingMode.HALF_UP))
                                        .setFontSize(8)));
                        table.addCell(new Cell().add(new Paragraph("10").setFontSize(8)));
                        table.addCell(new Cell()
                                        .add(new Paragraph("RM " + p.getPrice().setScale(2, RoundingMode.HALF_UP))
                                                        .setFontSize(8)));
                        table.addCell(new Cell().add(new Paragraph("").setFontSize(8)));
                }

                doc.add(table);
                doc.add(new Paragraph("Generated on " + java.time.LocalDate.now())
                                .setFontSize(8)
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
