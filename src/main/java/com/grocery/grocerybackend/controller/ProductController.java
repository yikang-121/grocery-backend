// src/main/java/com/grocery/grocerybackend/controller/ProductController.java
package com.grocery.grocerybackend.controller;

import com.grocery.grocerybackend.entity.Product;
import com.grocery.grocerybackend.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(
        origins = "http://localhost:3000",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS},
        allowedHeaders = "*"
)
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    // keep existing
    @GetMapping
    public List<Product> getAll(
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "12") Integer limit
    ) {
        // if category provided -> filter, else list all
        if (category != null && !category.isBlank()) {
            return service.listByCategory(category, Math.max(1, Math.min(100, limit)));
        }
        return service.getAll();
    }

    // keep existing
    @PostMapping
    public int add(@RequestBody Product product) {
        return service.save(product);
    }

    // NEW: detail route for your page
    @GetMapping("/{id}")                   // <-- add this
    public Product getOne(@PathVariable Long id) {
        return service.getOne(id);
    }

    @GetMapping("/top-rated")
    public List<Product> topRated(@RequestParam(defaultValue = "4") int limit) {
        return service.getTopRated(limit);
    }
}
