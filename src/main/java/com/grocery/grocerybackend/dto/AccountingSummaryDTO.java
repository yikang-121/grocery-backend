package com.grocery.grocerybackend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class AccountingSummaryDTO {
    private int year;
    private int month;
    private BigDecimal totalRevenue;
    private BigDecimal totalCOGS;
    private BigDecimal grossProfit;
    private BigDecimal sstTaxCollected;
    private BigDecimal spoilageLoss;
    private BigDecimal purchaseCost;
    private BigDecimal netProfit;
    private int ordersCount;
    private int itemsSoldCount;
    private int spoilageCount;
    private int purchaseOrdersCount;
    private BigDecimal currentStockValue;

    // --- Trend Data (% change vs previous month) ---
    private BigDecimal revenueTrend; 
    private BigDecimal profitTrend;
    private BigDecimal spoilageTrend;

    // --- FYP Specific Algorithm Metrics ---
    private BigDecimal wastePreventedCost;      // RM saved by algorithm
    private int stockoutsAvoided;           // count of predicted hits
    private double forecastAccuracy;        // % accuracy of the model

    // --- Visualization Data (RM per week) ---
    private List<WeeklyData> weeklyPerformance;

    @Data
    public static class WeeklyData {
        private String week; // "W1", "W2", etc.
        private BigDecimal revenue;
        private BigDecimal cost;
        private BigDecimal spoilage;
    }
}
