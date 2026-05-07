package com.grocery.grocerybackend.service;

import com.grocery.grocerybackend.dto.RestockCalculationResponse;
import com.grocery.grocerybackend.entity.InventoryMetrics;
import org.springframework.stereotype.Service;

@Service
public class RestockOptimizer {

    private static final double BETA = 0.8;
    private static final double BASE_Z = 1.65; // 95% service level

    /**
     * Calculates the optimal restocking quantity for a given SKU metrics.
     *
     * @param metrics The inventory metrics for the SKU
     * @return RestockCalculationResponse containing the calculated variables and
     *         final order quantity
     */
    public RestockCalculationResponse calculateRestock(InventoryMetrics metrics) {
        RestockCalculationResponse response = new RestockCalculationResponse();
        response.setSkuId(metrics.getSkuId());

        // ── Step A: Demand Momentum (M) ──
        double longTermAvg = metrics.getAvgSales30d() != null ? metrics.getAvgSales30d() : 0.0;
        double shortTermAvg = metrics.getAvgSales3d() != null ? metrics.getAvgSales3d() : 0.0;

        double momentum = 0.0;
        if (longTermAvg == 0 && shortTermAvg == 0) {
            momentum = 0.0;
        } else if (longTermAvg == 0 && shortTermAvg > 0) {
            momentum = 1.0;
        } else {
            momentum = (shortTermAvg - longTermAvg) / longTermAvg;
        }
        double mCapped = Math.min(Math.max(momentum, -1.0), 1.0);

        // ── Step B: Adjusted Demand (D_adj) ──
        double baseDemand = (longTermAvg == 0) ? shortTermAvg : longTermAvg;
        double seasonalityFactor = metrics.getSeasonalityFactor() != null ? metrics.getSeasonalityFactor() : 1.0;
        double adjustedDemand = baseDemand * (1 + (BETA * mCapped)) * seasonalityFactor;

        // ── Step C: Volatility-Based Safety Stock (SS) ──
        double stdDev = metrics.getStdDev30d() != null ? metrics.getStdDev30d() : 0.0;
        double cv = 0.0;
        if (baseDemand > 0) {
            cv = stdDev / baseDemand;
        }

        double dynamicZ = BASE_Z * (1 + cv);
        dynamicZ = Math.min(Math.max(dynamicZ, 1.28), 2.33);

        int leadTimeDays = metrics.getLeadTimeDays() != null ? metrics.getLeadTimeDays() : 0;
        int reviewPeriodDays = metrics.getReviewPeriodDays() != null ? metrics.getReviewPeriodDays() : 0;
        double safetyStock = dynamicZ * stdDev * Math.sqrt(leadTimeDays + reviewPeriodDays);

        // ── Step D: Adaptive Target Stock ──
        double targetStock = (adjustedDemand * (leadTimeDays + reviewPeriodDays)) + safetyStock;

        // ── Step E: Perishability Constraint ──
        int usableShelfLifeDays = metrics.getShelfLifeDays() != null ? metrics.getShelfLifeDays() : 365;
        double maxSellableQty = adjustedDemand * usableShelfLifeDays;
        double safeTargetStock = Math.min(targetStock, maxSellableQty);

        // ── Step F: Net Requirement ──
        int currentStock = metrics.getCurrentStock() != null ? metrics.getCurrentStock() : 0;
        int incomingStock = metrics.getIncomingStock() != null ? metrics.getIncomingStock() : 0;
        int reservedStock = 0; // Not tracked in DB currently

        double inventoryPosition = currentStock + incomingStock - reservedStock;
        double netRequirement = Math.max(0, safeTargetStock - inventoryPosition);

        // ── Step G: Decay Adjustment ──
        double lambda = metrics.getWasteLambda() != null ? metrics.getWasteLambda() : 0.0;
        int expectedStorageDays = Math.min(leadTimeDays + reviewPeriodDays, usableShelfLifeDays);
        double decayFactor = Math.exp(-lambda * expectedStorageDays);

        double rawOrderQty = netRequirement * decayFactor;

        // ── Step H: Practical Ordering Constraints ──
        int finalOrderQty = 0;
        boolean moqRiskFlag = false;

        if (rawOrderQty > 0) {
            // Round up to nearest unit
            finalOrderQty = (int) Math.ceil(rawOrderQty);

            // Round up to nearest Case Size
            int caseSize = metrics.getCaseSize() != null ? metrics.getCaseSize() : 1;
            if (caseSize > 1 && finalOrderQty % caseSize != 0) {
                finalOrderQty = (int) (Math.ceil((double) finalOrderQty / caseSize) * caseSize);
            }

            // Apply Supplier MOQ with risk flag
            int moq = metrics.getSupplierMoq() != null ? metrics.getSupplierMoq() : 1;
            if (finalOrderQty < moq) {
                if (moq <= netRequirement) {
                    // MOQ is within the net requirement — safe to apply
                    finalOrderQty = moq;
                } else {
                    // MOQ exceeds net requirement — keep current qty but flag risk
                    moqRiskFlag = true;
                }
            }
        }

        // ── Mapping Results to Response ──
        response.setMomentum(mCapped);
        response.setAdjustedDemand(adjustedDemand);
        response.setCv(cv);
        response.setDynamicZ(dynamicZ);
        response.setSafetyStock(safetyStock);
        response.setMaxSellableQty(maxSellableQty);
        response.setSafeTargetStock(safeTargetStock);
        response.setTargetStock(targetStock);
        response.setNetRequirement(netRequirement);
        response.setRawOrderQty(rawOrderQty);
        response.setDecayFactor(decayFactor);
        response.setCurrentStock(currentStock);
        response.setIncomingStock(incomingStock);
        response.setCaseSize(metrics.getCaseSize() != null ? metrics.getCaseSize() : 1);
        response.setSeasonalityFactor(seasonalityFactor);
        response.setOrderQuantity(finalOrderQty);
        response.setMoqRiskFlag(moqRiskFlag);

        return response;
    }
}
