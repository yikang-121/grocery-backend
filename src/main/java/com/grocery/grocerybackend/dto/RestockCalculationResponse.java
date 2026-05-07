package com.grocery.grocerybackend.dto;

import lombok.Data;

@Data
public class RestockCalculationResponse {
    private String skuId;
    private String productName;
    private String category;
    private int orderQuantity;
    private double momentum;
    private double cv;
    private double dynamicZ;
    private double safetyStock;
    private double decayFactor;
    private double adjustedDemand;
    private double targetStock;
    private double maxSellableQty;
    private double safeTargetStock;
    private double netRequirement;
    private double rawOrderQty;
    private double seasonalityFactor;
    private int currentStock;
    private int incomingStock;
    private int caseSize;
    private boolean moqRiskFlag;
}
