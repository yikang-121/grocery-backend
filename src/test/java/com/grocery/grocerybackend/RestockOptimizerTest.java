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
    public void testTC018_AdaptiveVsBaselineHighMomentum() {
        // TC-018: Adaptive vs Baseline Comparison
        // Run both algorithms on a product with high short-term demand momentum.
        // Expected: Adaptive algorithm suggests a higher restock quantity than the baseline algorithm.
        InventoryMetrics metrics = new InventoryMetrics();
        metrics.setSkuId("TC018");
        metrics.setCurrentStock(10);
        metrics.setLeadTimeDays(2);
        metrics.setReviewPeriodDays(1);
        metrics.setShelfLifeDays(30);
        metrics.setSupplierMoq(1);
        metrics.setWasteLambda(0.0);
        metrics.setAvgSales3d(50.0); // High short term
        metrics.setAvgSales30d(20.0); // Low long term
        metrics.setStdDev30d(5.0);

        RestockCalculationResponse response = optimizer.calculateRestock(metrics);

        // Baseline would rely purely on 20.0 avg sales.
        // The momentum is (50-20)/20 = 1.5, which gets capped at 1.0
        // Adjusted Demand = 20 * (1 + 0.8*1.0) = 36.0
        assertEquals(1.0, response.getMomentum(), "Momentum should be capped at 1.0");
        assertTrue(response.getAdjustedDemand() > 20.0, "Adaptive adjusted demand must be higher than baseline");
        assertTrue(response.getOrderQuantity() > 0, "System should recommend order");
    }

    @Test
    public void testTC019_PerishabilityCapActivation() {
        // TC-019: Perishability Cap Activation
        // Product has short usable shelf life and moderate demand.
        // Expected: Adaptive algorithm caps the order quantity using Max_Sellable_Qty
        InventoryMetrics metrics = new InventoryMetrics();
        metrics.setSkuId("TC019");
        metrics.setCurrentStock(0);
        metrics.setLeadTimeDays(5);
        metrics.setReviewPeriodDays(2); // total storage expected = 7 days
        metrics.setShelfLifeDays(3); // usable shelf life is shorter than 7
        metrics.setSupplierMoq(1);
        metrics.setWasteLambda(0.0);
        metrics.setAvgSales3d(20.0);
        metrics.setAvgSales30d(20.0);
        metrics.setStdDev30d(2.0);

        RestockCalculationResponse response = optimizer.calculateRestock(metrics);

        // Adjusted Demand = 20
        // Max Sellable Qty = 20 * 3 = 60
        // Target Stock (Uncapped) = 20 * 7 + SS = 140 + SS
        // Cap should activate.
        assertEquals(60.0, response.getMaxSellableQty(), 0.01);
        assertEquals(60.0, response.getSafeTargetStock(), 0.01);
        assertTrue(response.getTargetStock() > response.getSafeTargetStock(), "Perishability cap should reduce target stock");
    }

    @Test
    public void testTC020_ZeroSalesEdgeCase() {
        // TC-020: Zero-Sales Edge Case
        // Product has 0 sales in both 3-day and 30-day windows.
        // Expected: Algorithm handles zero demand safely and returns 0 restock quantity
        InventoryMetrics metrics = new InventoryMetrics();
        metrics.setSkuId("TC020");
        metrics.setCurrentStock(5);
        metrics.setLeadTimeDays(2);
        metrics.setReviewPeriodDays(1);
        metrics.setShelfLifeDays(30);
        metrics.setSupplierMoq(10);
        metrics.setWasteLambda(0.0);
        metrics.setAvgSales3d(0.0);
        metrics.setAvgSales30d(0.0);
        metrics.setStdDev30d(0.0);

        RestockCalculationResponse response = optimizer.calculateRestock(metrics);

        assertEquals(0.0, response.getMomentum(), 0.01);
        assertEquals(0.0, response.getAdjustedDemand(), 0.01);
        assertEquals(0.0, response.getNetRequirement(), 0.01);
        assertEquals(0, response.getOrderQuantity(), "Should return 0 restock quantity safely without error");
    }

    @Test
    public void testTC021_DecayFactorImpact() {
        // TC-021: Decay Factor Impact
        // Product has high spoilage risk and limited expected storage period.
        // Expected: Decay factor reduces the raw order quantity
        InventoryMetrics metrics = new InventoryMetrics();
        metrics.setSkuId("TC021");
        metrics.setCurrentStock(0);
        metrics.setLeadTimeDays(2);
        metrics.setReviewPeriodDays(2); // Expected storage days = 4
        metrics.setShelfLifeDays(5);
        metrics.setSupplierMoq(1);
        metrics.setWasteLambda(0.2); // High spoilage risk lambda
        metrics.setAvgSales3d(30.0);
        metrics.setAvgSales30d(30.0);
        metrics.setStdDev30d(5.0);

        RestockCalculationResponse response = optimizer.calculateRestock(metrics);

        double expectedStorage = Math.min(2 + 2, 5); // 4
        double expectedDecay = Math.exp(-0.2 * expectedStorage);
        
        assertEquals(expectedDecay, response.getDecayFactor(), 0.0001);
        assertTrue(response.getDecayFactor() < 1.0, "Decay factor must be less than 1");
        assertTrue(response.getRawOrderQty() < response.getNetRequirement(), "Raw order quantity should be reduced by decay factor");
    }

    @Test
    public void testTC022_AutoGeneratedPurchaseOrder() {
        // TC-022: Auto-Generated Purchase Order
        // Product stock falls below the calculated threshold.
        // Expected: System generates a smart restock purchase order with the calculated quantity.
        InventoryMetrics metrics = new InventoryMetrics();
        metrics.setSkuId("TC022");
        metrics.setCurrentStock(2); // Low stock
        metrics.setLeadTimeDays(3);
        metrics.setReviewPeriodDays(1);
        metrics.setShelfLifeDays(90);
        metrics.setSupplierMoq(50);
        metrics.setWasteLambda(0.001);
        metrics.setAvgSales3d(15.0);
        metrics.setAvgSales30d(15.0);
        metrics.setStdDev30d(3.0);

        RestockCalculationResponse response = optimizer.calculateRestock(metrics);

        assertTrue(response.getNetRequirement() > 0, "Net requirement should trigger a restock");
        assertTrue(response.getOrderQuantity() >= 50, "Order quantity must respect the Supplier MOQ constraint");
    }
}
