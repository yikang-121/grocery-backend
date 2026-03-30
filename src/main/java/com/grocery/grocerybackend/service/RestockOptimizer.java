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

        // A. Demand Momentum (M)
        double longTermAvg = metrics.getAvgSales30d() != null ? metrics.getAvgSales30d() : 0.0;
        double shortTermAvg = metrics.getAvgSales3d() != null ? metrics.getAvgSales3d() : 0.0;

        double momentum = 0.0;
        double forecast = longTermAvg;
        if (longTermAvg > 0) {
            momentum = (shortTermAvg - longTermAvg) / longTermAvg;
        } else if (shortTermAvg > 0) {
            // Handle division by zero if Long_Term_Avg is 0 but there are recent sales
            momentum = 1.0; // Assume 100% momentum boost as fallback
            forecast = shortTermAvg;
        }

        // Adjusted Demand = Forecast * (1 + (Beta * M)) * SeasonalityFactor
        double seasonalityFactor = metrics.getSeasonalityFactor() != null ? metrics.getSeasonalityFactor() : 1.0;
        double adjustedDemand = forecast * (1 + (BETA * momentum)) * seasonalityFactor;

        // B. Volatility-Adjusted Safety Stock (SS)
        double stdDev = metrics.getStdDev30d() != null ? metrics.getStdDev30d() : 0.0;
        double cv = 0.0;
        if (longTermAvg > 0) {
            cv = stdDev / longTermAvg;
        }

        double dynamicZ = BASE_Z * (1 + cv);
        int leadTimeDays = metrics.getLeadTimeDays() != null ? metrics.getLeadTimeDays() : 0;
        double safetyStock = dynamicZ * stdDev * Math.sqrt(leadTimeDays);

        // C. Perishability Cap (Max Sellable Before Spoilage)
        int shelfLifeDays = metrics.getShelfLifeDays() != null ? metrics.getShelfLifeDays() : 365; // Default long shelf life
        double maxSellableQty = adjustedDemand * shelfLifeDays;

        // D. Final Restock Calculation (Q)
        int reviewPeriodDays = metrics.getReviewPeriodDays() != null ? metrics.getReviewPeriodDays() : 0;
        double targetStock = (adjustedDemand * reviewPeriodDays) + safetyStock;
        
        // Cap target stock to avoid spoilage
        double safeTargetStock = Math.min(targetStock, maxSellableQty);

        int currentStock = metrics.getCurrentStock() != null ? metrics.getCurrentStock() : 0;
        int incomingStock = metrics.getIncomingStock() != null ? metrics.getIncomingStock() : 0;
        
        double netRequirement = safeTargetStock - (currentStock + incomingStock);
        // E. Final Order Calculation (incorporating Exponential Decay for Perishables)
        // Formula: Q = (T - Stock) * e^(-Lambda * ShelfLife)
        double lambda = metrics.getWasteLambda() != null ? metrics.getWasteLambda() : 0.0;
        double decayFactor = Math.exp(-lambda * shelfLifeDays);
        
        double rawOrderQty = netRequirement * decayFactor;

        // Apply Constraints (Rounding to Case Size & Supplier MOQ)
        int finalOrderQty = 0;
        if (rawOrderQty > 0) {
            // Round up to nearest unit
            finalOrderQty = (int) Math.ceil(rawOrderQty);
            
            // Round up to nearest Case Size
            int caseSize = metrics.getCaseSize() != null ? metrics.getCaseSize() : 1;
            if (finalOrderQty % caseSize != 0) {
                finalOrderQty += (caseSize - (finalOrderQty % caseSize));
            }
            
            // Apply Supplier MOQ
            int moq = metrics.getSupplierMoq() != null ? metrics.getSupplierMoq() : 1;
            if (finalOrderQty < moq) {
                finalOrderQty = moq;
            }
        }

        // F. Mapping Results to Response
        response.setMomentum(momentum);
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
        response.setIncomingStock(incomingStock);
        response.setCaseSize(metrics.getCaseSize() != null ? metrics.getCaseSize() : 1);
        response.setSeasonalityFactor(seasonalityFactor);
        response.setOrderQuantity(finalOrderQty);

        return response;
    }
}
