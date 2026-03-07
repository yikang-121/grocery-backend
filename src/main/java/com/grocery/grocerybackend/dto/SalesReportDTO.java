package com.grocery.grocerybackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class SalesReportDTO {
    private BigDecimal totalRevenue;
    private int totalOrders;
    private int totalItemsSold;
    private BigDecimal avgOrderValue;
    private List<DailySales> dailySales;
    private List<TopProduct> topSellingProducts;
    private List<CategorySales> categorySales;
    private List<OrderSummary> recentOrders;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DailySales {
        private LocalDate date;
        private BigDecimal revenue;
        private int orderCount;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TopProduct {
        private Long productId;
        private String productName;
        private int quantitySold;
        private BigDecimal revenue;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategorySales {
        private String category;
        private BigDecimal revenue;
        private int quantitySold;
        private int orderCount;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderSummary {
        private String orderNo;
        private LocalDate date;
        private BigDecimal total;
        private String status;
        private int itemCount;
    }
}
