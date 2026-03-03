package com.grocery.grocerybackend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
public class SpoilageLogResponse {
    private Long id;
    private Long batchId;
    private Long productId;
    private String productName;
    private String sku;
    private Integer quantity;
    private BigDecimal costPrice;
    private BigDecimal totalLoss;
    private String reason;
    private Timestamp createdAt;
}
