/*package com.grocery.grocerybackend.service;

import com.grocery.grocerybackend.entity.Product;
import com.grocery.grocerybackend.entity.ProductImportLog;
import com.grocery.grocerybackend.mapper.ProductImportLogMapper;
import com.grocery.grocerybackend.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductImportService {

    private final ProductMapper productMapper;
    private final ProductImportLogMapper importLogMapper; // optional
    private final PricingService pricingService;

    /**
     * Accepts CSV columns (case-insensitive):
     *   SKU, Name, Cost, Qty
     * Optional columns:
     *   Category, OverridePrice
     *
     * @return summary map

    @Transactional
    public Map<String, Object> importCsv(MultipartFile file, boolean dryRun) throws Exception {
        int created = 0, updated = 0, rows = 0;

        try (var reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             var parser = CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreSurroundingSpaces()
                     .withTrim()
                     .parse(reader)) {

            Map<String, Integer> header = parser.getHeaderMap();
            for (CSVRecord rec : parser) {
                rows++;

                String sku = get(rec, header, "SKU");
                String name = get(rec, header, "Name");
                String costStr = get(rec, header, "Cost");
                String qtyStr = get(rec, header, "Qty");

                if (sku.isBlank()) continue;

                BigDecimal cost = parseMoney(costStr);
                int addQty = parseInt(qtyStr, 0);

                // optional fields
                String category = get(rec, header, "Category");
                BigDecimal overridePrice = parseMoney(get(rec, header, "OverridePrice"));

                Product p = productMapper.selectBySku(sku);

                if (p == null) {
                    p = new Product();
                    p.setSku(sku);
                    p.setName((name == null || name.isBlank()) ? sku : name);
                    p.setCostPrice(cost);
                    p.setStockQuantity(Math.max(addQty, 0));

                    BigDecimal price = overridePrice != null
                            ? overridePrice
                            : pricingService.calculateSelling(cost);
                    p.setPrice(price);

                    if (!dryRun) productMapper.insert(p);
                    created++;
                    logImport(p, sku, 0, addQty, p.getStockQuantity(), cost, price, dryRun);
                } else {
                    int old = Optional.ofNullable(p.getStockQuantity()).orElse(0);
                    int newStock = old + Math.max(addQty, 0);

                    p.setCostPrice(cost);
                    if (name != null && !name.isBlank()) p.setName(name);

                    BigDecimal price = (overridePrice != null)
                            ? overridePrice
                            : pricingService.calculateSelling(cost);
                    p.setPrice(price);
                    p.setStockQuantity(newStock);

                    if (!dryRun) productMapper.updateById(p);
                    updated++;
                    logImport(p, sku, old, addQty, newStock, cost, price, dryRun);
                }
            }
        }

        return Map.of(
                "rows", rows,
                "created", created,
                "updated", updated,
                "dryRun", dryRun
        );
    }

    private void logImport(Product p, String sku, int oldStock, int add, int newStock,
                           BigDecimal cost, BigDecimal price, boolean dryRun) {
        if (importLogMapper == null || dryRun) return;
        ProductImportLog log = new ProductImportLog();
        log.setProductId(p.getId());
        log.setSupplierSku(sku);
        log.setOldStock(oldStock);
        log.setAddedStock(add);
        log.setNewStock(newStock);
        log.setCostPrice(cost);
        log.setSellingPrice(price);
        importLogMapper.insert(log);
    }

    private static String get(CSVRecord r, Map<String, Integer> header, String key) {
        for (String h : header.keySet()) {
            if (h.equalsIgnoreCase(key)) return r.get(h);
        }
        return "";
    }

    private static BigDecimal parseMoney(String s) {
        if (s == null || s.isBlank()) return null;
        return new BigDecimal(s.replaceAll("[^0-9.\\-]", "")).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }
}
*/
