// src/main/java/com/grocery/grocerybackend/service/ProductService.java
package com.grocery.grocerybackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.grocery.grocerybackend.entity.Product;
import com.grocery.grocerybackend.mapper.ProductMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductMapper mapper;

    public ProductService(ProductMapper mapper) {
        this.mapper = mapper;
    }

    public List<Product> getAll() {
        return mapper.selectList(new QueryWrapper<>());
    }

    public int save(Product product) {
        return mapper.insert(product);
    }

    // NEW
    public Product getOne(Long id) {          // <-- add this
        return mapper.selectById(id);         // returns null if not found
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
