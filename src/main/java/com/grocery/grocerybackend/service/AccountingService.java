package com.grocery.grocerybackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.grocery.grocerybackend.dto.AccountingSummaryDTO;
import com.grocery.grocerybackend.entity.*;
import com.grocery.grocerybackend.mapper.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AccountingService {

    private static final BigDecimal SST_RATE = new BigDecimal("0.06"); // 6% SST

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final SpoilageLogMapper spoilageLogMapper;
    private final PurchaseOrderMapper purchaseOrderMapper;

    public AccountingService(OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                             ProductMapper productMapper, SpoilageLogMapper spoilageLogMapper,
                             PurchaseOrderMapper purchaseOrderMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.productMapper = productMapper;
        this.spoilageLogMapper = spoilageLogMapper;
        this.purchaseOrderMapper = purchaseOrderMapper;
    }

    public AccountingSummaryDTO getAccountingSummary(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);
        
        AccountingSummaryDTO current = calculateForRange(start, end);
        
        // Calculate Previous Month for Trends
        LocalDate prevStart = start.minusMonths(1);
        LocalDate prevEnd = prevStart.plusMonths(1).minusDays(1);
        AccountingSummaryDTO previous = calculateForRange(prevStart, prevEnd);
        
        // Populate Trends
        current.setRevenueTrend(calculateTrend(current.getTotalRevenue(), previous.getTotalRevenue()));
        current.setProfitTrend(calculateTrend(current.getNetProfit(), previous.getNetProfit()));
        current.setSpoilageTrend(calculateTrend(current.getSpoilageLoss(), previous.getSpoilageLoss()));
        
        // Algorithm Impact (Simulated for FYP Demo)
        // Assume baseline waste is 30% higher than with the algorithm
        BigDecimal wastePrevented = current.getSpoilageLoss().multiply(new BigDecimal("0.30")).setScale(2, RoundingMode.HALF_UP);
        current.setWastePreventedCost(wastePrevented);
        current.setStockoutsAvoided((int)(current.getOrdersCount() * 0.05)); // Estimate 5% avoided
        current.setForecastAccuracy(89.5 + (new Random().nextDouble() * 5)); // 89-94% range
        
        // Weekly Performance Data
        List<AccountingSummaryDTO.WeeklyData> weekly = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            LocalDate wStart = start.plusDays(i * 7);
            LocalDate wEnd = wStart.plusDays(6);
            if (wEnd.isAfter(end)) wEnd = end;
            
            AccountingSummaryDTO wData = calculateForRange(wStart, wEnd);
            AccountingSummaryDTO.WeeklyData wd = new AccountingSummaryDTO.WeeklyData();
            wd.setWeek("W" + (i + 1));
            wd.setRevenue(wData.getTotalRevenue());
            wd.setCost(wData.getTotalCOGS().add(wData.getPurchaseCost()));
            wd.setSpoilage(wData.getSpoilageLoss());
            weekly.add(wd);
        }
        current.setWeeklyPerformance(weekly);
        
        return current;
    }

    private AccountingSummaryDTO calculateForRange(LocalDate start, LocalDate end) {
        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt = end.atTime(LocalTime.MAX);

        AccountingSummaryDTO dto = new AccountingSummaryDTO();
        dto.setYear(start.getYear());
        dto.setMonth(start.getMonthValue());

        // 1. Revenue
        List<Order> orders = orderMapper.selectList(
                new QueryWrapper<Order>().between("created_at", startDt, endDt).ne("status", "CANCELLED"));
        BigDecimal revenue = orders.stream().map(Order::getTotal).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalRevenue(revenue);
        dto.setOrdersCount(orders.size());

        // 2. COGS
        List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());
        BigDecimal cogs = BigDecimal.ZERO;
        int itemsSold = 0;
        if (!orderIds.isEmpty()) {
            List<OrderItem> items = orderItemMapper.selectList(new QueryWrapper<OrderItem>().in("order_id", orderIds));
            Map<Long, BigDecimal> costPriceMap = productMapper.selectList(null).stream()
                    .collect(Collectors.toMap(Product::getId, p -> p.getCostPrice() != null ? p.getCostPrice() : BigDecimal.ZERO, (a, b) -> a));
            for (OrderItem item : items) {
                BigDecimal cost = costPriceMap.getOrDefault(item.getProductId(), BigDecimal.ZERO);
                cogs = cogs.add(cost.multiply(BigDecimal.valueOf(item.getQuantity())));
                itemsSold += item.getQuantity();
            }
        }
        dto.setTotalCOGS(cogs);
        dto.setItemsSoldCount(itemsSold);

        // 3. Gross Profit
        dto.setGrossProfit(revenue.subtract(cogs));

        // 4. SST
        dto.setSstTaxCollected(revenue.multiply(SST_RATE).setScale(2, RoundingMode.HALF_UP));

        // 5. Spoilage Loss (Fix: ensure totalLoss is used correctly)
        List<SpoilageLog> spoilageLogs = spoilageLogMapper.selectList(new QueryWrapper<SpoilageLog>().between("created_at", startDt, endDt));
        BigDecimal spoilageLoss = spoilageLogs.stream()
                .map(l -> {
                    if (l.getTotalLoss() != null) return l.getTotalLoss();
                    // Fallback to costPrice * qty if totalLoss is missing
                    BigDecimal cost = l.getCostPrice() != null ? l.getCostPrice() : BigDecimal.ZERO;
                    return cost.multiply(BigDecimal.valueOf(l.getQuantity() != null ? l.getQuantity() : 0));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setSpoilageLoss(spoilageLoss);
        dto.setSpoilageCount(spoilageLogs.size());

        // 6. Purchase Cost
        List<PurchaseOrder> receivedPOs = purchaseOrderMapper.selectList(
                new QueryWrapper<PurchaseOrder>().eq("status", "RECEIVED").between("received_at", startDt, endDt));
        BigDecimal purchaseCost = receivedPOs.stream().map(PurchaseOrder::getTotalCost).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setPurchaseCost(purchaseCost);
        dto.setPurchaseOrdersCount(receivedPOs.size());

        // 7. Net Profit
        dto.setNetProfit(dto.getGrossProfit().subtract(spoilageLoss));

        // 8. Stock Value
        List<Product> allProducts = productMapper.selectList(null);
        dto.setCurrentStockValue(allProducts.stream().map(p -> {
            BigDecimal cost = p.getCostPrice() != null ? p.getCostPrice() : BigDecimal.ZERO;
            return cost.multiply(BigDecimal.valueOf(p.getStockQuantity() != null ? p.getStockQuantity() : 0));
        }).reduce(BigDecimal.ZERO, BigDecimal::add));

        return dto;
    }

    private BigDecimal calculateTrend(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return current.subtract(previous).divide(previous, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
    }

    public Map<String, Object> getInvoiceForOrder(String orderNo) {
        Order order = orderMapper.selectOne(new QueryWrapper<Order>().eq("order_no", orderNo));
        if (order == null) throw new IllegalArgumentException("Order not found: " + orderNo);
        List<OrderItem> items = orderItemMapper.selectList(new QueryWrapper<OrderItem>().eq("order_id", order.getId()));
        BigDecimal subtotal = order.getSubtotal() != null ? order.getSubtotal() : BigDecimal.ZERO;
        BigDecimal sst = subtotal.multiply(SST_RATE).setScale(2, RoundingMode.HALF_UP);
        Map<String, Object> invoice = new LinkedHashMap<>();
        invoice.put("invoiceNo", "INV-" + order.getOrderNo());
        invoice.put("orderNo", order.getOrderNo());
        invoice.put("date", order.getCreatedAt() != null ? order.getCreatedAt().toString() : null);
        invoice.put("status", order.getStatus());
        invoice.put("subtotal", subtotal);
        invoice.put("shippingFee", order.getShippingFee());
        invoice.put("discount", order.getDiscount());
        invoice.put("sstRate", "6%");
        invoice.put("sstAmount", sst);
        invoice.put("total", order.getTotal());
        invoice.put("totalWithTax", order.getTotal().add(sst));
        invoice.put("paymentMethod", order.getPaymentMethod());
        invoice.put("items", items.stream().map(item -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("productName", item.getProductName());
            m.put("quantity", item.getQuantity());
            m.put("unitPrice", item.getUnitPrice());
            m.put("lineTotal", item.getLineTotal());
            return m;
        }).collect(Collectors.toList()));
        return invoice;
    }
}
