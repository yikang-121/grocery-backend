package com.grocery.grocerybackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class StockReportDTO {
    private int totalProducts;
    private int totalStockQuantity;
    private int lowStockCount;
    private int outOfStockCount;
    private int expiringSoonCount;
    private List<TopStockedProduct> topStockedProducts;
    private List<LowStockProduct> lowStockProducts;
    private List<ExpiringSoonBatch> expiringSoonBatches;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TopStockedProduct {
        private Long productId;
        private String productName;
        private int currentStock;
        private BigDecimal price;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LowStockProduct {
        private Long productId;
        private String productName;
        private String category;
        private int currentStock;
        private BigDecimal price;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ExpiringSoonBatch {
        private Long productId;
        private String productName;
        private String batchNo;
        private int availableQuantity;
        private LocalDate expiryDate;
    }
}
