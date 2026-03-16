package com.grocery.grocerybackend;

import com.grocery.grocerybackend.dto.RestockCalculationResponse;
import com.grocery.grocerybackend.entity.InventoryMetrics;
import com.grocery.grocerybackend.service.RestockOptimizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RestockOptimizerTest {

    private RestockOptimizer optimizer;

    @BeforeEach
    public void setup() {
        optimizer = new RestockOptimizer();
    }

    @Test
    public void testHighlyPerishableItem() {
        // 1. Highly perishable, high-volatility item (Fresh Strawberries)
        InventoryMetrics metrics = new InventoryMetrics();
        metrics.setSkuId("STRAW-001");
        metrics.setCurrentStock(50);
        metrics.setLeadTimeDays(2);
        metrics.setReviewPeriodDays(1);
        metrics.setShelfLifeDays(3);
        metrics.setSupplierMoq(10);
        metrics.setWasteLambda(0.1);
        metrics.setAvgSales3d(40.0);
        metrics.setAvgSales30d(45.0);
        metrics.setStdDev30d(15.0);

        RestockCalculationResponse response = optimizer.calculateRestock(metrics);

        // Assert decay factor is penalizing the order
        assertTrue(response.getDecayFactor() < 1.0, "Decay factor should reduce the order quantity");
        assertEquals(Math.exp(-0.1 * 3), response.getDecayFactor(), 0.0001);

        // Calculate manually to assert:
        // M = (40-45)/45 = -0.1111
        // AdjustedDemand = 45 * (1 + 0.8 * -0.1111) = 41
        assertEquals(41.0, response.getAdjustedDemand(), 0.01);
        assertTrue(response.getOrderQuantity() > 0, "Should still recommend an order");
    }

    @Test
    public void testStableNonPerishableItem() {
        // 2. Stable, non-perishable item (Canned Beans)
        InventoryMetrics metrics = new InventoryMetrics();
        metrics.setSkuId("BEAN-001");
        metrics.setCurrentStock(100);
        metrics.setLeadTimeDays(7);
        metrics.setReviewPeriodDays(7);
        metrics.setShelfLifeDays(365);
        metrics.setSupplierMoq(50);
        metrics.setWasteLambda(0.001);
        metrics.setAvgSales3d(15.0);
        metrics.setAvgSales30d(16.0);
        metrics.setStdDev30d(2.0);

        RestockCalculationResponse response = optimizer.calculateRestock(metrics);

        // Assert stable momentum
        assertTrue(response.getMomentum() < 0 && response.getMomentum() > -0.1);

        // Assert low volatility Z score
        double cv = 2.0 / 16.0;
        double dynamicZ = 1.65 * (1 + cv);
        assertEquals(dynamicZ, response.getDynamicZ(), 0.001);
    }

    @Test
    public void testTrendingItem() {
        // 3. Trending item experiencing a sudden demand spike (Viral Hot Sauce)
        InventoryMetrics metrics = new InventoryMetrics();
        metrics.setSkuId("SAUCE-001");
        metrics.setCurrentStock(20);
        metrics.setLeadTimeDays(5);
        metrics.setReviewPeriodDays(2);
        metrics.setShelfLifeDays(180);
        metrics.setSupplierMoq(100);
        metrics.setWasteLambda(0.005);
        metrics.setAvgSales3d(100.0);
        metrics.setAvgSales30d(20.0);
        metrics.setStdDev30d(5.0);

        RestockCalculationResponse response = optimizer.calculateRestock(metrics);

        // Assert momentum is high
        assertEquals(4.0, response.getMomentum(), 0.001);

        // AdjustedDemand = 20 * (1 + 0.8 * 4) = 20 * 4.2 = 84
        assertEquals(84.0, response.getAdjustedDemand(), 0.001);

        // TargetStock = (84 * 2) + safetyStock
        // CV = 5/20 = 0.25 -> DynamicZ = 1.65 * 1.25 = 2.0625
        // SS = 2.0625 * 5 * sqrt(5) ≈ 23.058
        // TargetStock ≈ 168 + 23.058 = 191.058
        // NetReq = 191.058 - 20 = 171.058
        // DF = exp(-0.005 * 180) = exp(-0.9) ≈ 0.4065
        // RawOrderQty = 171.058 * 0.4065 ≈ 69.5
        // Since MOQ is 100, and 69.5 < 100 -> Final = 100

        assertEquals(100, response.getOrderQuantity(),
                "Should respect the MOQ since order is needed and raw is below MOQ");
    }
}
