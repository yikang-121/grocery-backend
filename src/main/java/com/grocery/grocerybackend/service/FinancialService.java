package com.grocery.grocerybackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.grocery.grocerybackend.dto.MonthlySummary;
import com.grocery.grocerybackend.dto.SalesReportDTO;
import com.grocery.grocerybackend.dto.StockReportDTO;
import com.grocery.grocerybackend.entity.*;
import com.grocery.grocerybackend.mapper.BatchMapper;
import com.grocery.grocerybackend.mapper.OrderItemMapper;
import com.grocery.grocerybackend.mapper.OrderMapper;
import com.grocery.grocerybackend.mapper.ProductMapper;
import com.grocery.grocerybackend.mapper.SpoilageLogMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FinancialService {

        private final OrderMapper orderMapper;
        private final SpoilageLogMapper spoilageLogMapper;
        private final ProductMapper productMapper;
        private final BatchMapper batchMapper;
        private final OrderItemMapper orderItemMapper;

        public FinancialService(OrderMapper orderMapper, SpoilageLogMapper spoilageLogMapper,
                        ProductMapper productMapper, BatchMapper batchMapper,
                        OrderItemMapper orderItemMapper) {
                this.orderMapper = orderMapper;
                this.spoilageLogMapper = spoilageLogMapper;
                this.productMapper = productMapper;
                this.batchMapper = batchMapper;
                this.orderItemMapper = orderItemMapper;
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
                                .filter(l -> l.getTotalLoss() != null)
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

        public StockReportDTO getStockReport() {
                List<Product> products = productMapper.selectList(null);
                List<Batch> activeBatches = batchMapper.selectList(
                                new QueryWrapper<Batch>().gt("available_quantity", 0));

                StockReportDTO dto = new StockReportDTO();
                dto.setTotalProducts(products.size());

                int totalStock = products.stream()
                                .mapToInt(p -> p.getStockQuantity() == null ? 0 : p.getStockQuantity()).sum();
                dto.setTotalStockQuantity(totalStock);

                long lowStock = products.stream().filter(p -> p.getStockQuantity() != null && p.getStockQuantity() > 0
                                && p.getStockQuantity() < 10).count();
                dto.setLowStockCount((int) lowStock);

                long outOfStock = products.stream()
                                .filter(p -> p.getStockQuantity() == null || p.getStockQuantity() == 0).count();
                dto.setOutOfStockCount((int) outOfStock);

                LocalDate sevenDaysLater = LocalDate.now().plusDays(7);
                List<Batch> expiringSoon = activeBatches.stream()
                                .filter(b -> b.getExpiryDate() != null && !b.getExpiryDate().isBefore(LocalDate.now())
                                                && b.getExpiryDate().isBefore(sevenDaysLater))
                                .collect(Collectors.toList());
                dto.setExpiringSoonCount(expiringSoon.size());

                // Top Stocked Products
                List<StockReportDTO.TopStockedProduct> topStockedList = products.stream()
                                .sorted((p1, p2) -> {
                                        int s1 = p1.getStockQuantity() == null ? 0 : p1.getStockQuantity();
                                        int s2 = p2.getStockQuantity() == null ? 0 : p2.getStockQuantity();
                                        return Integer.compare(s2, s1);
                                })
                                .limit(10)
                                .map(p -> new StockReportDTO.TopStockedProduct(p.getId(), p.getName(),
                                                p.getStockQuantity() == null ? 0 : p.getStockQuantity(), p.getPrice()))
                                .collect(Collectors.toList());
                dto.setTopStockedProducts(topStockedList);

                // Low Stock Products
                List<StockReportDTO.LowStockProduct> lowStockList = products.stream()
                                .filter(p -> p.getStockQuantity() != null && p.getStockQuantity() < 10)
                                .map(p -> new StockReportDTO.LowStockProduct(p.getId(), p.getName(), p.getCategory(),
                                                p.getStockQuantity(), p.getPrice()))
                                .collect(Collectors.toList());
                dto.setLowStockProducts(lowStockList);

                // Expiring Soon Batches
                Map<Long, String> productNames = products.stream()
                                .collect(Collectors.toMap(
                                                Product::getId, 
                                                p -> p.getName() != null ? p.getName() : "Unknown", 
                                                (a, b) -> a
                                ));
                List<StockReportDTO.ExpiringSoonBatch> expiringList = expiringSoon.stream()
                                .map(b -> new StockReportDTO.ExpiringSoonBatch(
                                                b.getProductId(),
                                                productNames.getOrDefault(b.getProductId(), "Unknown"),
                                                b.getBatchNo(),
                                                b.getAvailableQuantity(),
                                                b.getExpiryDate()))
                                .collect(Collectors.toList());
                dto.setExpiringSoonBatches(expiringList);

                return dto;
        }

        public SalesReportDTO getSalesReport(int year, int month) {
                LocalDate start = LocalDate.of(year, month, 1);
                LocalDate end = start.plusMonths(1).minusDays(1);
                LocalDateTime startDt = start.atStartOfDay();
                LocalDateTime endDt = end.atTime(LocalTime.MAX);

                List<Order> orders = orderMapper.selectList(
                                new QueryWrapper<Order>()
                                                .between("created_at", startDt, endDt)
                                                .ne("status", "CANCELLED"));

                SalesReportDTO dto = new SalesReportDTO();
                BigDecimal totalRevenue = orders.stream()
                                .map(Order::getTotal)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                dto.setTotalRevenue(totalRevenue);
                dto.setTotalOrders(orders.size());

                if (orders.isEmpty()) {
                        dto.setTotalItemsSold(0);
                        dto.setAvgOrderValue(BigDecimal.ZERO);
                        dto.setDailySales(Collections.emptyList());
                        dto.setTopSellingProducts(Collections.emptyList());
                        dto.setCategorySales(Collections.emptyList());
                        dto.setRecentOrders(Collections.emptyList());
                        return dto;
                }

                dto.setAvgOrderValue(totalRevenue.divide(BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP));

                List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());
                List<OrderItem> items = orderItemMapper.selectList(
                                new QueryWrapper<OrderItem>().in("order_id", orderIds));

                int totalItemsSold = items.stream().mapToInt(OrderItem::getQuantity).sum();
                dto.setTotalItemsSold(totalItemsSold);

                // Daily Sales
                Map<LocalDate, List<Order>> ordersByDate = orders.stream()
                                .collect(Collectors.groupingBy(o -> o.getCreatedAt().toLocalDate()));

                List<SalesReportDTO.DailySales> dailySales = ordersByDate.entrySet().stream()
                                .map(e -> new SalesReportDTO.DailySales(
                                                e.getKey(),
                                                e.getValue().stream().map(Order::getTotal).reduce(BigDecimal.ZERO,
                                                                BigDecimal::add),
                                                e.getValue().size()))
                                .sorted(Comparator.comparing(SalesReportDTO.DailySales::getDate))
                                .collect(Collectors.toList());
                dto.setDailySales(dailySales);

                // Top Selling Products
                Map<Long, List<OrderItem>> itemsByProduct = items.stream()
                                .collect(Collectors.groupingBy(OrderItem::getProductId));
                List<SalesReportDTO.TopProduct> topProducts = itemsByProduct.entrySet().stream()
                                .map(e -> {
                                        String name = e.getValue().get(0).getProductName();
                                        int qty = e.getValue().stream().mapToInt(OrderItem::getQuantity).sum();
                                        BigDecimal rev = e.getValue().stream().map(OrderItem::getLineTotal)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        return new SalesReportDTO.TopProduct(e.getKey(), name, qty, rev);
                                })
                                .sorted(Comparator.comparing(SalesReportDTO.TopProduct::getQuantitySold).reversed())
                                .limit(10)
                                .collect(Collectors.toList());
                dto.setTopSellingProducts(topProducts);

                // Category Sales
                Map<Long, String> productCategories = productMapper.selectList(null).stream()
                                .collect(Collectors.toMap(Product::getId,
                                                p -> p.getCategory() == null ? "Uncategorized" : p.getCategory(),
                                                (a, b) -> a));

                Map<String, List<OrderItem>> itemsByCategory = items.stream()
                                .collect(Collectors.groupingBy(i -> productCategories.getOrDefault(i.getProductId(),
                                                "Uncategorized")));

                List<SalesReportDTO.CategorySales> categorySales = itemsByCategory.entrySet().stream()
                                .map(e -> {
                                        BigDecimal rev = e.getValue().stream().map(OrderItem::getLineTotal)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        int qty = e.getValue().stream().mapToInt(OrderItem::getQuantity).sum();
                                        Set<Long> catOrderIds = e.getValue().stream().map(OrderItem::getOrderId)
                                                        .collect(Collectors.toSet());
                                        return new SalesReportDTO.CategorySales(e.getKey(), rev, qty,
                                                        catOrderIds.size());
                                })
                                .collect(Collectors.toList());
                dto.setCategorySales(categorySales);

                // Recent Orders
                List<SalesReportDTO.OrderSummary> recent = orders.stream()
                                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                                .limit(10)
                                .map(o -> {
                                        int itemCount = (int) items.stream()
                                                        .filter(i -> i.getOrderId().equals(o.getId())).count();
                                        return new SalesReportDTO.OrderSummary(o.getOrderNo(),
                                                        o.getCreatedAt().toLocalDate(), o.getTotal(), o.getStatus(),
                                                        itemCount);
                                })
                                .collect(Collectors.toList());
                dto.setRecentOrders(recent);

                return dto;
        }

        public List<Product> getAllProducts() {
                return productMapper.selectList(null);
        }
}
