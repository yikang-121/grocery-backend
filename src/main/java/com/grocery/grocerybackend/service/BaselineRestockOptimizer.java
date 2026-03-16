package com.grocery.grocerybackend.service;

import com.grocery.grocerybackend.dto.RestockCalculationResponse;
import com.grocery.grocerybackend.entity.InventoryMetrics;
import org.springframework.stereotype.Service;

@Service
public class BaselineRestockOptimizer {

    private static final double BASE_Z = 1.65; // 95% service level

    /**
     * Calculates the baseline restocking quantity using Standard ROP/EOQ Logic.
     * Does NOT account for Demand Momentum or Perishability Decay.
     *
     * @param metrics The inventory metrics for the SKU
     * @return RestockCalculationResponse containing the calculated baseline order
     *         quantity
     */
    public RestockCalculationResponse calculateBaseline(InventoryMetrics metrics) {
        RestockCalculationResponse response = new RestockCalculationResponse();
        response.setSkuId(metrics.getSkuId());

        // A. Standard Demand (No Momentum)
        double longTermAvg = metrics.getAvgSales30d() != null ? metrics.getAvgSales30d() : 0.0;
        double adjustedDemand = longTermAvg; // Purely historical, no trend adjustment

        // B. Standard Safety Stock (Static Z, no Volatility CV adjustment)
        double stdDev = metrics.getStdDev30d() != null ? metrics.getStdDev30d() : 0.0;
        double dynamicZ = BASE_Z; // Static Z score
        int leadTimeDays = metrics.getLeadTimeDays() != null ? metrics.getLeadTimeDays() : 0;
        double safetyStock = dynamicZ * stdDev * Math.sqrt(leadTimeDays);

        // C. No Perishability Decay Factor
        double decayFactor = 1.0; // Assume 100% viability (STATIC BASELINE)

        // D. Final ROP Restock Calculation (Q)
        int reviewPeriodDays = metrics.getReviewPeriodDays() != null ? metrics.getReviewPeriodDays() : 0;
        double targetStock = (adjustedDemand * reviewPeriodDays) + safetyStock;

        int currentStock = metrics.getCurrentStock() != null ? metrics.getCurrentStock() : 0;
        double netRequirement = targetStock - currentStock;

        double rawOrderQty = netRequirement * decayFactor;

        // Apply Constraints (Standard Min/Max)
        int finalOrderQty = 0;
        if (rawOrderQty > 0) {
            finalOrderQty = (int) Math.ceil(rawOrderQty);
            int moq = metrics.getSupplierMoq() != null ? metrics.getSupplierMoq() : 1;
            if (finalOrderQty < moq) {
                finalOrderQty = moq;
            }
        }

        // Populate Response for baseline debugging and tracking
        response.setMomentum(0.0); // Baseline ignores momentum
        response.setAdjustedDemand(adjustedDemand);
        response.setCv(0.0); // Baseline ignores CV
        response.setDynamicZ(dynamicZ);
        response.setSafetyStock(safetyStock);
        response.setDecayFactor(decayFactor);
        response.setTargetStock(targetStock);
        response.setNetRequirement(netRequirement);
        response.setRawOrderQty(rawOrderQty);
        response.setOrderQuantity(finalOrderQty);

        return response;
    }
}
