package com.grocery.grocerybackend.util;

import com.grocery.grocerybackend.entity.Batch;
import com.grocery.grocerybackend.entity.Product;
import com.grocery.grocerybackend.mapper.BatchMapper;
import com.grocery.grocerybackend.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
public class BatchDataSeeder implements CommandLineRunner {

    private final ProductMapper productMapper;
    private final BatchMapper batchMapper;

    public BatchDataSeeder(ProductMapper productMapper, BatchMapper batchMapper) {
        this.productMapper = productMapper;
        this.batchMapper = batchMapper;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking for products to seed batches...");

        List<Product> products = productMapper.selectList(null);
        if (products.isEmpty()) {
            log.warn("No products found in database. Skipping batch seeding.");
            return;
        }

        int limit = Math.min(products.size(), 5);
        for (int i = 0; i < limit; i++) {
            Product p = products.get(i);

            // Check if product already has batches
            List<Batch> existingBatches = batchMapper.findValidBatchesForFefo(p.getId(), LocalDate.of(2000, 1, 1));
            if (!existingBatches.isEmpty()) {
                log.info("Product {} already has batches. Skipping.", p.getName());
                continue;
            }

            log.info("Seeding batches for product: {}", p.getName());

            // Seed 2-3 batches for each product
            seedBatch(p, "B" + p.getId() + "01", LocalDate.now().plusDays(5), 20);
            seedBatch(p, "B" + p.getId() + "02", LocalDate.now().plusDays(15), 30);
            seedBatch(p, "B" + p.getId() + "03", LocalDate.now().plusDays(40), 50);

            // Synchronize product's total stock
            p.setStockQuantity(100);
            productMapper.updateById(p);
        }
    }

    private void seedBatch(Product p, String batchNo, LocalDate expiryDate, int qty) {
        Batch b = new Batch();
        b.setProductId(p.getId());
        b.setBatchNo(batchNo);
        b.setExpiryDate(expiryDate);
        b.setAvailableQuantity(qty);
        b.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        b.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        batchMapper.insert(b);
    }
}
