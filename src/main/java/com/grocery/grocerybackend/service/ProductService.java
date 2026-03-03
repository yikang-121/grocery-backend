// src/main/java/com/grocery/grocerybackend/service/ProductService.java
package com.grocery.grocerybackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.grocery.grocerybackend.entity.Product;
import com.grocery.grocerybackend.mapper.BatchMapper;
import com.grocery.grocerybackend.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import com.grocery.grocerybackend.entity.*;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProductService {

    private final ProductMapper mapper;
    private final BatchMapper batchMapper;

    public ProductService(ProductMapper mapper, BatchMapper batchMapper) {
        this.mapper = mapper;
        this.batchMapper = batchMapper;
    }

    public List<Product> getAll() {
        List<Product> products = mapper.selectList(new QueryWrapper<>());
        for (Product p : products) {
            p.setBatches(batchMapper.findValidBatchesForFefo(p.getId(), LocalDate.of(2000, 1, 1)));
        }
        return products;
    }

    public int save(Product product) {
        return mapper.insert(product);
    }

    // NEW
    public Product getOne(Long id) {
        Product p = mapper.selectById(id);
        if (p != null) {
            List<Batch> batches = batchMapper.findValidBatchesForFefo(p.getId(), LocalDate.of(2000, 1, 1));
            p.setBatches(batches);
            // Ensure stock quantity is synced with sum of batches if any exist
            if (!batches.isEmpty()) {
                int total = batches.stream().mapToInt(Batch::getAvailableQuantity).sum();
                p.setStockQuantity(total);
            }
        }
        return p;
    }

    // NEW
    public List<Product> listByCategory(String category, int limit) {
        QueryWrapper<Product> qw = new QueryWrapper<Product>()
                .eq("category", category)
                .last("LIMIT " + limit);
        return mapper.selectList(qw);
    }

    public List<Product> getTopRated(int limit) {
        int safeLimit = (limit <= 0 || limit > 24) ? 4 : limit; // cap for safety
        return mapper.selectTopRated(safeLimit);
    }
}
