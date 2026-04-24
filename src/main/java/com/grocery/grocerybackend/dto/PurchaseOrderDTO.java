package com.grocery.grocerybackend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PurchaseOrderDTO {
    private Long id;
    private String poNumber;
    private String supplierName;
    private String status;
    private BigDecimal totalCost;
    private BigDecimal taxAmount;
    private String notes;
    private String createdAt;
    private String approvedAt;
    private String receivedAt;
    private List<ItemDTO> items;

    @Data
    public static class ItemDTO {
        private Long id;
        private Long productId;
        private String sku;
        private String productName;
        private Integer quantityOrdered;
        private Integer quantityReceived;
        private BigDecimal unitCost;
        private BigDecimal lineTotal;
    }
}
