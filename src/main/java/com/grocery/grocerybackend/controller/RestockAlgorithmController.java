package com.grocery.grocerybackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.grocery.grocerybackend.dto.RestockCalculationResponse;
import com.grocery.grocerybackend.entity.InventoryMetrics;
import com.grocery.grocerybackend.mapper.InventoryMetricsMapper;
import com.grocery.grocerybackend.service.RestockOptimizer;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@CrossOrigin(origins = "http://localhost:3000")
public class RestockAlgorithmController {

    private final RestockOptimizer restockOptimizer;
    private final com.grocery.grocerybackend.service.BaselineRestockOptimizer baselineRestockOptimizer;
    private final InventoryMetricsMapper inventoryMetricsMapper;
    private final com.grocery.grocerybackend.service.MetricsSyncService metricsSyncService;

    public RestockAlgorithmController(RestockOptimizer restockOptimizer,
            com.grocery.grocerybackend.service.BaselineRestockOptimizer baselineRestockOptimizer,
            InventoryMetricsMapper inventoryMetricsMapper,
            com.grocery.grocerybackend.service.MetricsSyncService metricsSyncService) {
        this.restockOptimizer = restockOptimizer;
        this.baselineRestockOptimizer = baselineRestockOptimizer;
        this.inventoryMetricsMapper = inventoryMetricsMapper;
        this.metricsSyncService = metricsSyncService;
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

        // Fetch metrics for the provided SKUs
        List<InventoryMetrics> metricsList = inventoryMetricsMapper.selectList(
                new QueryWrapper<InventoryMetrics>().in("sku_id", skuIds));

        // Run the RestockOptimizer for each SKU
        for (InventoryMetrics metrics : metricsList) {
            RestockCalculationResponse response = restockOptimizer.calculateRestock(metrics);
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

        // Fetch metrics for the provided SKUs
        List<InventoryMetrics> metricsList = inventoryMetricsMapper.selectList(
                new QueryWrapper<InventoryMetrics>().in("sku_id", skuIds));

        // Run the BaselineRestockOptimizer for each SKU
        for (InventoryMetrics metrics : metricsList) {
            RestockCalculationResponse response = baselineRestockOptimizer.calculateBaseline(metrics);
            responses.add(response);
        }

        return responses;
    }
}
