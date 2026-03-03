package com.grocery.grocerybackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.grocery.grocerybackend.dto.MonthlySummary;
import com.grocery.grocerybackend.entity.Order;
import com.grocery.grocerybackend.entity.SpoilageLog;
import com.grocery.grocerybackend.mapper.OrderMapper;
import com.grocery.grocerybackend.mapper.SpoilageLogMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class FinancialService {

    private final OrderMapper orderMapper;
    private final SpoilageLogMapper spoilageLogMapper;

    public FinancialService(OrderMapper orderMapper, SpoilageLogMapper spoilageLogMapper) {
        this.orderMapper = orderMapper;
        this.spoilageLogMapper = spoilageLogMapper;
    }

    public MonthlySummary getMonthlySummary(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt = end.atTime(LocalTime.MAX);

        // Revenue from Orders (assume only COMPLETED or PENDING orders count?
        // Let's take all except CANCELLED)
        List<Order> orders = orderMapper.selectList(
                new QueryWrapper<Order>()
                        .between("created_at", startDt, endDt)
                        .ne("status", "CANCELLED"));

        BigDecimal revenue = orders.stream()
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Losses from Spoilage
        List<SpoilageLog> logs = spoilageLogMapper.selectList(
                new QueryWrapper<SpoilageLog>()
                        .between("created_at", startDt, endDt));

        BigDecimal loss = logs.stream()
                .map(SpoilageLog::getTotalLoss)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        MonthlySummary summary = new MonthlySummary();
        summary.setYear(year);
        summary.setMonth(month);
        summary.setTotalRevenue(revenue);
        summary.setTotalSpoilageLoss(loss);
        summary.setOrdersCount(orders.size());
        summary.setSpoilageCount(logs.size());

        return summary;
    }
}
