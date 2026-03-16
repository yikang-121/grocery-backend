package com.grocery.grocerybackend.dto;

import lombok.Data;

@Data
public class RestockCalculationResponse {
    private String skuId;
    private int orderQuantity;
    private double momentum;
    private double cv;
    private double dynamicZ;
    private double safetyStock;
    private double decayFactor;
    private double adjustedDemand;
    private double targetStock;
    private double netRequirement;
    private double rawOrderQty;
}
