// src/main/java/com/grocery/grocerybackend/service/InventoryService.java
package com.grocery.grocerybackend.service;

import com.grocery.grocerybackend.entity.Batch;
import com.grocery.grocerybackend.entity.Product;
import com.grocery.grocerybackend.exception.InsufficientStockException;
import com.grocery.grocerybackend.mapper.BatchMapper;
import com.grocery.grocerybackend.mapper.ProductMapper;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;

@Service
public class InventoryService {

    private final ProductMapper productMapper;
    private final BatchMapper batchMapper;
    private final com.grocery.grocerybackend.mapper.SpoilageLogMapper spoilageLogMapper;

    public InventoryService(ProductMapper productMapper, BatchMapper batchMapper,
            com.grocery.grocerybackend.mapper.SpoilageLogMapper spoilageLogMapper) {
        this.productMapper = productMapper;
        this.batchMapper = batchMapper;
        this.spoilageLogMapper = spoilageLogMapper;
    }

    @Data
    public static class BulkUploadResult {
        private int totalRows;
        private int created;
        private int updated;
        private int restocked;
        private List<String> errors = new ArrayList<>();
    }

    @Transactional
    public BulkUploadResult uploadCsv(MultipartFile file, BigDecimal profitMargin) throws Exception {
        BulkUploadResult summary = new BulkUploadResult();

        if (file == null || file.isEmpty()) {
            summary.getErrors().add("Empty file");
            return summary;
        }

        // e.g. 25 -> 0.25
        final BigDecimal marginFactor = Optional.ofNullable(profitMargin)
                .orElse(BigDecimal.ZERO)
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = br.readLine();
            if (headerLine == null) {
                summary.getErrors().add("Missing header row");
                return summary;
            }

            String[] headers = Arrays.stream(headerLine.split(",", -1))
                    .map(h -> h.trim().toLowerCase())
                    .toArray(String[]::new);

            // Build header index
            Map<String, Integer> idx = index(headers,
                    "sku",
                    "name", "product_name",
                    "category",
                    "cost_price", "supplier_price",
                    "price", "selling_price",
                    "stock_quantity", "quantity",
                    "image_url",
                    "original_price",
                    "product_url");

            // Column-existence flags (used to decide if a field may be updated)
            boolean hasSkuCol = present(idx, "sku");
            boolean hasNameCol = present(idx, "name") || present(idx, "product_name");
            boolean hasCategoryCol = present(idx, "category");
            boolean hasCostCol = present(idx, "cost_price") || present(idx, "supplier_price");
            boolean hasPriceCol = present(idx, "price") || present(idx, "selling_price");
            boolean hasQtyCol = present(idx, "stock_quantity") || present(idx, "quantity");
            boolean hasImageCol = present(idx, "image_url");
            boolean hasOriginalPriceCol = present(idx, "original_price");
            boolean hasProductUrlCol = present(idx, "product_url");

            String line;
            int row = 1;

            while ((line = br.readLine()) != null) {
                row++;
                if (line.trim().isEmpty())
                    continue;

                summary.setTotalRows(summary.getTotalRows() + 1);

                // Split; pad if shorter than header length
                String[] cols = splitCsvLine(line, headers.length);

                try {
                    // Read values only if the column exists
                    String sku = read(cols, idx, "sku");
                    if (sku == null || sku.isBlank()) {
                        summary.getErrors().add("Row " + row + ": missing sku");
                        continue;
                    }

                    String nameRaw = read(cols, idx, "name", "product_name");
                    String categoryRaw = read(cols, idx, "category");
                    String costRaw = read(cols, idx, "cost_price", "supplier_price");
                    String priceRaw = read(cols, idx, "price", "selling_price");
                    String qtyRaw = read(cols, idx, "stock_quantity", "quantity");
                    String imageRaw = read(cols, idx, "image_url");
                    String origPriceRaw = read(cols, idx, "original_price");
                    String productUrlRaw = read(cols, idx, "product_url");

                    String name = normalize(nameRaw);
                    String category = normalize(categoryRaw);
                    BigDecimal costPrice = toBig(costRaw);
                    BigDecimal csvPrice = toBig(priceRaw);
                    Integer qty = toInt(qtyRaw);
                    String imageUrl = normalize(imageRaw);
                    BigDecimal originalPrice = toBig(origPriceRaw);
                    String productUrl = normalize(productUrlRaw);

                    // Compute price (only when CSV omitted price but provided cost)
                    BigDecimal computedPrice = null;
                    if (csvPrice == null && costPrice != null) {
                        computedPrice = costPrice.multiply(BigDecimal.ONE.add(marginFactor))
                                .setScale(2, RoundingMode.HALF_UP);
                    }

                    Product existing = productMapper.findBySku(sku);

                    if (existing == null) {
                        // INSERT: apply safe defaults when supplier doesn't send values
                        Product p = new Product();
                        p.setSku(sku);
                        p.setName(name != null ? name : sku); // fallback to sku
                        p.setCategory(category != null ? category : "General");

                        if (costPrice != null)
                            p.setCostPrice(costPrice);
                        if (csvPrice != null)
                            p.setPrice(csvPrice);
                        else if (computedPrice != null)
                            p.setPrice(computedPrice);

                        if (originalPrice != null)
                            p.setOriginalPrice(originalPrice);
                        if (imageUrl != null)
                            p.setImageUrl(imageUrl);
                        if (productUrl != null)
                            p.setProductUrl(productUrl);

                        p.setStockQuantity(qty != null ? qty : 0);

                        Timestamp now = new Timestamp(System.currentTimeMillis());
                        p.setCreatedAt(now);
                        p.setUpdatedAt(now);

                        productMapper.insert(p);
                        summary.setCreated(summary.getCreated() + 1);
                    } else {
                        // UPDATE: only touch fields that were actually present in the CSV
                        boolean anyUpdated = false;

                        if (hasNameCol && name != null && !name.equals(existing.getName())) {
                            existing.setName(name);
                            anyUpdated = true;
                        }
                        if (hasCategoryCol && category != null && !category.equals(existing.getCategory())) {
                            existing.setCategory(category);
                            anyUpdated = true;
                        }
                        if (hasImageCol && imageUrl != null && !imageUrl.equals(existing.getImageUrl())) {
                            existing.setImageUrl(imageUrl);
                            anyUpdated = true;
                        }
                        if (hasProductUrlCol && productUrl != null && !productUrl.equals(existing.getProductUrl())) {
                            existing.setProductUrl(productUrl);
                            anyUpdated = true;
                        }
                        if (hasCostCol && costPrice != null && notEqual(costPrice, existing.getCostPrice())) {
                            existing.setCostPrice(costPrice);
                            anyUpdated = true;
                        }

                        // Price precedence: CSV price > computed-from-cost > keep existing
                        if (hasPriceCol && csvPrice != null && notEqual(csvPrice, existing.getPrice())) {
                            existing.setPrice(csvPrice);
                            anyUpdated = true;
                        } else if (hasCostCol && csvPrice == null && computedPrice != null
                                && notEqual(computedPrice, existing.getPrice())) {
                            existing.setPrice(computedPrice);
                            anyUpdated = true;
                        }

                        if (hasOriginalPriceCol && originalPrice != null
                                && notEqual(originalPrice, existing.getOriginalPrice())) {
                            existing.setOriginalPrice(originalPrice);
                            anyUpdated = true;
                        }

                        if (anyUpdated) {
                            existing.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
                            productMapper.updateById(existing);
                            summary.setUpdated(summary.getUpdated() + 1);
                        }

                        // Restock only if quantity column exists and value > 0
                        if (hasQtyCol && qty != null && qty > 0) {
                            productMapper.incrementStock(existing.getId(), qty);
                            summary.setRestocked(summary.getRestocked() + 1);
                        }
                    }
                } catch (Exception ex) {
                    summary.getErrors().add("Row " + row + ": " + ex.getMessage());
                }
            }
        }

        return summary;
    }

    @Transactional
    public void deductStockFEFO(Long productId, int quantity) {
        if (quantity <= 0)
            return;

        LocalDate today = LocalDate.now();
        List<Batch> batches = batchMapper.findValidBatchesForFefo(productId, today);

        int totalAvailable = batches.stream().mapToInt(Batch::getAvailableQuantity).sum();
        if (totalAvailable < quantity) {
            throw new InsufficientStockException("Insufficient total stock for product ID: " + productId +
                    ". Requested: " + quantity + ", Available: " + totalAvailable);
        }

        int remainingDeduction = quantity;
        for (Batch batch : batches) {
            int deductFromThisBatch = Math.min(batch.getAvailableQuantity(), remainingDeduction);

            batch.setAvailableQuantity(batch.getAvailableQuantity() - deductFromThisBatch);
            batchMapper.updateById(batch);

            remainingDeduction -= deductFromThisBatch;
            if (remainingDeduction == 0)
                break;
        }

        // Synchronize product table's total stock (optional but good for display)
        productMapper.decrementStock(productId, quantity);
    }

    @Transactional
    public Batch addBatch(Batch batch) {
        if (batch.getCreatedAt() == null)
            batch.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        if (batch.getUpdatedAt() == null)
            batch.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        batchMapper.insert(batch);

        // Update product table's total stock
        if (batch.getAvailableQuantity() != null && batch.getAvailableQuantity() > 0) {
            productMapper.incrementStock(batch.getProductId(), batch.getAvailableQuantity());
        }

        return batch;
    }

    @Transactional
    public void recordSpoilage(Long batchId, Integer quantity, String reason) {
        Batch batch = batchMapper.selectById(batchId);
        if (batch == null)
            throw new IllegalArgumentException("Batch not found: " + batchId);
        if (batch.getAvailableQuantity() < quantity) {
            throw new InsufficientStockException("Insufficient quantity in batch. Batch: "
                    + batch.getAvailableQuantity() + ", Required: " + quantity);
        }

        Product product = productMapper.selectById(batch.getProductId());
        if (product == null)
            throw new IllegalArgumentException("Product not found for batch: " + batchId);

        // 1. Deduct from batch
        batch.setAvailableQuantity(batch.getAvailableQuantity() - quantity);
        batch.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        batchMapper.updateById(batch);

        // 2. Deduct from total stock
        productMapper.decrementStock(product.getId(), quantity);

        // 3. Log spoilage (Financials)
        com.grocery.grocerybackend.entity.SpoilageLog log = new com.grocery.grocerybackend.entity.SpoilageLog();
        log.setBatchId(batchId);
        log.setProductId(product.getId());
        log.setQuantity(quantity);
        log.setCostPrice(product.getCostPrice() != null ? product.getCostPrice() : BigDecimal.ZERO);
        log.setTotalLoss(log.getCostPrice().multiply(BigDecimal.valueOf(quantity)));
        log.setReason(reason);
        log.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        spoilageLogMapper.insert(log);
    }

    @Transactional
    public int cleanupExpiredBatches() {
        LocalDate today = LocalDate.now();
        // Find all batches with expiry date before today and available quantity > 0
        List<Batch> expired = batchMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Batch>()
                        .lt("expiry_date", today)
                        .gt("available_quantity", 0));

        for (Batch b : expired) {
            recordSpoilage(b.getId(), b.getAvailableQuantity(), "EXPIRED");
        }
        return expired.size();
    }

    @Transactional(readOnly = true)
    public List<com.grocery.grocerybackend.dto.SpoilageLogResponse> getSpoilageLogs() {
        List<com.grocery.grocerybackend.entity.SpoilageLog> logs = spoilageLogMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.grocery.grocerybackend.entity.SpoilageLog>()
                        .orderByDesc("created_at"));

        return logs.stream().map(log -> {
            com.grocery.grocerybackend.dto.SpoilageLogResponse res = new com.grocery.grocerybackend.dto.SpoilageLogResponse();
            res.setId(log.getId());
            res.setBatchId(log.getBatchId());
            res.setProductId(log.getProductId());
            res.setQuantity(log.getQuantity());
            res.setCostPrice(log.getCostPrice());
            res.setTotalLoss(log.getTotalLoss());
            res.setReason(log.getReason());
            res.setCreatedAt(log.getCreatedAt());

            Product p = productMapper.selectById(log.getProductId());
            if (p != null) {
                res.setProductName(p.getName());
                res.setSku(p.getSku());
            }

            return res;
        }).collect(java.util.stream.Collectors.toList());
    }

    /* ----------------- helpers ----------------- */

    private static String normalize(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private static boolean notEqual(BigDecimal a, BigDecimal b) {
        return b == null || a.compareTo(b) != 0;
    }

    private static Map<String, Integer> index(String[] headers, String... keys) {
        Map<String, Integer> m = new HashMap<>();
        for (int i = 0; i < headers.length; i++)
            m.put(headers[i], i);
        // ensure map has keys for all aliases (with -1 if absent)
        for (String k : keys)
            m.putIfAbsent(k, -1);
        return m;
    }

    private static boolean present(Map<String, Integer> idx, String key) {
        Integer i = idx.get(key);
        return i != null && i >= 0;
    }

    /** read first present column among the names */
    private static String read(String[] cols, Map<String, Integer> idx, String... names) {
        for (String n : names) {
            Integer i = idx.get(n);
            if (i != null && i >= 0 && i < cols.length) {
                String v = cols[i];
                return v == null ? null : v.trim();
            }
        }
        return null;
    }

    private static String[] splitCsvLine(String line, int expectedCols) {
        String[] parts = line.split(",", -1);
        if (parts.length < expectedCols) {
            String[] pad = new String[expectedCols];
            System.arraycopy(parts, 0, pad, 0, parts.length);
            return pad;
        }
        return parts;
    }

    private static BigDecimal toBig(String s) {
        if (s == null || s.isBlank())
            return null;
        try {
            return new BigDecimal(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static Integer toInt(String s) {
        if (s == null || s.isBlank())
            return null;
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
