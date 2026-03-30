package com.grocery.grocerybackend.service;

import com.grocery.grocerybackend.entity.InventoryMetrics;
import com.grocery.grocerybackend.mapper.InventoryMetricsMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MetricsSyncService {

    private final InventoryMetricsMapper mapper;

    public MetricsSyncService(InventoryMetricsMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Synchronizes sales analytics from order history into the inventory_metrics table.
     * Updates current stock and sales statistics (3d/30d averages and volatility).
     */
    @Transactional
    public int syncAllMetrics() {
        int count = 0;
        // 1. Calculate real-time stats from orders, order_items, and product tables
        List<InventoryMetrics> realTimeStats = mapper.calculateSalesStats();

        for (InventoryMetrics stat : realTimeStats) {
            // SKIP Synchronization for Benchmark Test SKUs
            // These SKUs are manually seeded for the Supervisor Presentation and should not be overwritten.
            String sku = stat.getSkuId();
            if (sku.equals("VG-OAT-001") || sku.equals("VG-STW-002") || 
                sku.equals("VG-WAG-003") || sku.equals("VG-TRK-004")) {
                continue;
            }

            // 2. Check if metrics already exist for this SKU
            InventoryMetrics existing = mapper.findBySkuId(sku);

            if (existing != null) {
                // 3. Update only the dynamic fields
                existing.setCurrentStock(stat.getCurrentStock());
                existing.setAvgSales3d(stat.getAvgSales3d());
                existing.setAvgSales30d(stat.getAvgSales30d());
                existing.setStdDev30d(stat.getStdDev30d());
                existing.setSeasonalityFactor(stat.getSeasonalityFactor());
                
                // Note: Keep lead_time_days, shelf_life, etc. as they are handled by admin settings
                mapper.updateById(existing);
            } else {
                // 4. Create new entry with default safety settings if none exists
                stat.setLeadTimeDays(3);      // Default 3 days
                stat.setReviewPeriodDays(7);  // Default weekly review
                stat.setShelfLifeDays(14);    // Default 2 weeks
                stat.setSupplierMoq(1);      
                stat.setWasteLambda(0.05);    // Default 5% waste risk factor
                
                mapper.insert(stat);
            }
            count++;
        }
        return count;
    }
}
