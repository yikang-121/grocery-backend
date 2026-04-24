package com.grocery.grocerybackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.grocery.grocerybackend.dto.StockMovementDTO;
import com.grocery.grocerybackend.entity.Product;
import com.grocery.grocerybackend.entity.StockMovement;
import com.grocery.grocerybackend.mapper.ProductMapper;
import com.grocery.grocerybackend.mapper.StockMovementMapper;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StockMovementService {

    private final StockMovementMapper stockMovementMapper;
    private final ProductMapper productMapper;

    public StockMovementService(StockMovementMapper stockMovementMapper, ProductMapper productMapper) {
        this.stockMovementMapper = stockMovementMapper;
        this.productMapper = productMapper;
    }

    /**
     * Log a stock movement event.
     */
    public void logMovement(Long productId, Long batchId, String movementType,
                            int quantity, String referenceType, Long referenceId, String notes) {
        StockMovement sm = new StockMovement();
        sm.setProductId(productId);
        sm.setBatchId(batchId);
        sm.setMovementType(movementType);
        sm.setQuantity(quantity);
        sm.setReferenceType(referenceType);
        sm.setReferenceId(referenceId);
        sm.setNotes(notes);
        sm.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        stockMovementMapper.insert(sm);
    }

    /**
     * Get stock movements with optional filters.
     */
    public List<StockMovementDTO> getMovements(Long productId, String movementType,
                                                LocalDate from, LocalDate to) {
        QueryWrapper<StockMovement> qw = new QueryWrapper<>();

        if (productId != null) {
            qw.eq("product_id", productId);
        }
        if (movementType != null && !movementType.isBlank()) {
            qw.eq("movement_type", movementType);
        }
        if (from != null) {
            qw.ge("created_at", from.atStartOfDay());
        }
        if (to != null) {
            qw.le("created_at", to.atTime(LocalTime.MAX));
        }
        qw.orderByDesc("created_at");

        List<StockMovement> movements = stockMovementMapper.selectList(qw);

        // Build product lookup
        Map<Long, Product> productMap = productMapper.selectList(null).stream()
                .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));

        return movements.stream().map(sm -> {
            StockMovementDTO dto = new StockMovementDTO();
            dto.setId(sm.getId());
            dto.setProductId(sm.getProductId());
            dto.setBatchId(sm.getBatchId());
            dto.setMovementType(sm.getMovementType());
            dto.setQuantity(sm.getQuantity());
            dto.setReferenceType(sm.getReferenceType());
            dto.setReferenceId(sm.getReferenceId());
            dto.setNotes(sm.getNotes());
            dto.setCreatedAt(sm.getCreatedAt() != null ? sm.getCreatedAt().toString() : null);

            Product p = productMap.get(sm.getProductId());
            if (p != null) {
                dto.setProductName(p.getName());
                dto.setSku(p.getSku());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * Get aggregated stock in vs out summary for a date range.
     */
    public Map<String, Object> getStockInOutSummary(LocalDate from, LocalDate to) {
        QueryWrapper<StockMovement> qw = new QueryWrapper<>();
        if (from != null) qw.ge("created_at", from.atStartOfDay());
        if (to != null) qw.le("created_at", to.atTime(LocalTime.MAX));

        List<StockMovement> all = stockMovementMapper.selectList(qw);

        int totalIn = all.stream()
                .filter(m -> "STOCK_IN".equals(m.getMovementType())
                        || "RESTOCK".equals(m.getMovementType())
                        || "CANCEL_RETURN".equals(m.getMovementType()))
                .mapToInt(StockMovement::getQuantity).sum();

        int totalOut = all.stream()
                .filter(m -> "ORDER_DEDUCT".equals(m.getMovementType())
                        || "SPOILAGE".equals(m.getMovementType())
                        || "STOCK_OUT".equals(m.getMovementType()))
                .mapToInt(StockMovement::getQuantity).sum();

        return Map.of(
                "totalIn", totalIn,
                "totalOut", totalOut,
                "netChange", totalIn - totalOut,
                "totalMovements", all.size()
        );
    }
}
