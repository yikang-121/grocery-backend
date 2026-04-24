package com.grocery.grocerybackend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockMovementDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String sku;
    private Long batchId;
    private String movementType;
    private Integer quantity;
    private String referenceType;
    private Long referenceId;
    private String notes;
    private String createdAt;
}
