package com.grocery.grocerybackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.grocery.grocerybackend.dto.RestockCalculationResponse;
import com.grocery.grocerybackend.entity.InventoryMetrics;
import com.grocery.grocerybackend.entity.Product;
import com.grocery.grocerybackend.mapper.InventoryMetricsMapper;
import com.grocery.grocerybackend.mapper.ProductMapper;
import com.grocery.grocerybackend.service.RestockOptimizer;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/inventory")
@CrossOrigin(origins = "http://localhost:3000")
public class RestockAlgorithmController {

    private final RestockOptimizer restockOptimizer;
    private final com.grocery.grocerybackend.service.BaselineRestockOptimizer baselineRestockOptimizer;
    private final InventoryMetricsMapper inventoryMetricsMapper;
    private final com.grocery.grocerybackend.service.MetricsSyncService metricsSyncService;
    private final ProductMapper productMapper;

    public RestockAlgorithmController(RestockOptimizer restockOptimizer,
            com.grocery.grocerybackend.service.BaselineRestockOptimizer baselineRestockOptimizer,
            InventoryMetricsMapper inventoryMetricsMapper,
            com.grocery.grocerybackend.service.MetricsSyncService metricsSyncService,
            ProductMapper productMapper) {
        this.restockOptimizer = restockOptimizer;
        this.baselineRestockOptimizer = baselineRestockOptimizer;
        this.inventoryMetricsMapper = inventoryMetricsMapper;
        this.metricsSyncService = metricsSyncService;
        this.productMapper = productMapper;
    }

    @PostMapping("/sync-metrics")
    public int syncMetrics() {
        return metricsSyncService.syncAllMetrics();
    }

    @PostMapping("/calculate-restock")
    public List<RestockCalculationResponse> calculateRestock(@RequestBody List<String> skuIds) {
        List<RestockCalculationResponse> responses = new ArrayList<>();

        if (skuIds == null || skuIds.isEmpty()) {
            return responses;
        }

        List<InventoryMetrics> metricsList = inventoryMetricsMapper.selectList(
                new QueryWrapper<InventoryMetrics>().in("sku_id", skuIds));
        Map<String, Product> skuMap = getSkuProductMap();

        for (InventoryMetrics metrics : metricsList) {
            RestockCalculationResponse response = restockOptimizer.calculateRestock(metrics);
            enrichWithProductInfo(response, skuMap);
            responses.add(response);
        }

        return responses;
    }

    @PostMapping("/calculate-baseline")
    public List<RestockCalculationResponse> calculateBaseline(@RequestBody List<String> skuIds) {
        List<RestockCalculationResponse> responses = new ArrayList<>();

        if (skuIds == null || skuIds.isEmpty()) {
            return responses;
        }

        List<InventoryMetrics> metricsList = inventoryMetricsMapper.selectList(
                new QueryWrapper<InventoryMetrics>().in("sku_id", skuIds));
        Map<String, Product> skuMap = getSkuProductMap();

        for (InventoryMetrics metrics : metricsList) {
            RestockCalculationResponse response = baselineRestockOptimizer.calculateBaseline(metrics);
            enrichWithProductInfo(response, skuMap);
            responses.add(response);
        }

        return responses;
    }

    @GetMapping("/all-metrics")
    public List<InventoryMetrics> getAllMetrics() {
        return inventoryMetricsMapper.selectList(null);
    }

    @GetMapping("/calculate-restock-all")
    public List<RestockCalculationResponse> calculateRestockAll() {
        List<InventoryMetrics> allMetrics = inventoryMetricsMapper.selectList(null);
        Map<String, Product> skuMap = getSkuProductMap();
        List<RestockCalculationResponse> responses = new ArrayList<>();
        for (InventoryMetrics metrics : allMetrics) {
            RestockCalculationResponse r = restockOptimizer.calculateRestock(metrics);
            enrichWithProductInfo(r, skuMap);
            responses.add(r);
        }
        return responses;
    }

    @GetMapping("/calculate-baseline-all")
    public List<RestockCalculationResponse> calculateBaselineAll() {
        List<InventoryMetrics> allMetrics = inventoryMetricsMapper.selectList(null);
        Map<String, Product> skuMap = getSkuProductMap();
        List<RestockCalculationResponse> responses = new ArrayList<>();
        for (InventoryMetrics metrics : allMetrics) {
            RestockCalculationResponse r = baselineRestockOptimizer.calculateBaseline(metrics);
            enrichWithProductInfo(r, skuMap);
            responses.add(r);
        }
        return responses;
    }

    // --- helpers ---

    private Map<String, Product> getSkuProductMap() {
        List<Product> products = productMapper.selectList(null);
        return products.stream()
                .filter(p -> p.getSku() != null)
                .collect(Collectors.toMap(Product::getSku, p -> p, (a, b) -> a));
    }

    private void enrichWithProductInfo(RestockCalculationResponse response, Map<String, Product> skuMap) {
        Product product = skuMap.get(response.getSkuId());
        if (product != null) {
            response.setProductName(product.getName());
            response.setCategory(product.getCategory());
        }
    }
}
