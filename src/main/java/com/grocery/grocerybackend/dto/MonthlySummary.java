package com.grocery.grocerybackend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MonthlySummary {
    private int year;
    private int month;
    private BigDecimal totalRevenue;
    private BigDecimal totalSpoilageLoss;
    private int ordersCount;
    private int spoilageCount;
}
