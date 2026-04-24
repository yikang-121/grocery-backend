package com.grocery.grocerybackend.service;

import com.grocery.grocerybackend.dto.PurchaseOrderDTO;
import com.grocery.grocerybackend.dto.RestockCalculationResponse;
import com.grocery.grocerybackend.entity.InventoryMetrics;
import com.grocery.grocerybackend.entity.Product;
import com.grocery.grocerybackend.mapper.InventoryMetricsMapper;
import com.grocery.grocerybackend.mapper.ProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AutomatedRestockScheduler {

    private static final Logger logger = LoggerFactory.getLogger(AutomatedRestockScheduler.class);
    
    private final PurchaseOrderService purchaseOrderService;
    private final NotificationService notificationService;
    private final RestockOptimizer restockOptimizer;
    private final InventoryMetricsMapper inventoryMetricsMapper;
    private final ProductMapper productMapper;

    public AutomatedRestockScheduler(PurchaseOrderService purchaseOrderService,
                                     NotificationService notificationService,
                                     RestockOptimizer restockOptimizer,
                                     InventoryMetricsMapper inventoryMetricsMapper,
                                     ProductMapper productMapper) {
        this.purchaseOrderService = purchaseOrderService;
        this.notificationService = notificationService;
        this.restockOptimizer = restockOptimizer;
        this.inventoryMetricsMapper = inventoryMetricsMapper;
        this.productMapper = productMapper;
    }

    /**
     * Run the auto-restock algorithm every day at 12:00 AM (midnight).
     * It checks all inventory metrics and generates a Purchase Order
     * if any products need restocking.
     */
    @Scheduled(cron = "0 0 0 * * ?") // Every day at midnight
    //@Scheduled(fixedRate = 60000) // For testing: run every 60 seconds
    public void generateDailyRestock() {
        logger.info("Running daily automated restock check...");
        try {
            PurchaseOrderDTO po = purchaseOrderService.autoGeneratePO();
            if (po != null) {
                logger.info("Successfully generated Purchase Order: {} with {} items.", 
                        po.getPoNumber(), po.getItems().size());
            } else {
                logger.info("No items require restocking today.");
            }
        } catch (Exception e) {
            logger.error("Error occurred during automated restock check", e);
        }

        // Also check for low stock alerts
        checkLowStockAlerts();
    }

    /**
     * Run low-stock alert check on application startup so that
     * existing low-stock items are immediately flagged.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        logger.info("Running startup low-stock alert check...");
        checkLowStockAlerts();
    }

    /**
     * Scan all inventory metrics. For any product where the restock algorithm
     * determines orderQuantity > 0 (stock is below safety level),
     * create a LOW_STOCK_ALERT notification.
     * Deduplication is handled inside NotificationService.
     */
    public void checkLowStockAlerts() {
        try {
            List<InventoryMetrics> allMetrics = inventoryMetricsMapper.selectList(null);
            Map<String, Product> skuMap = productMapper.selectList(null).stream()
                    .filter(p -> p.getSku() != null)
                    .collect(Collectors.toMap(Product::getSku, p -> p, (a, b) -> a));

            int alertCount = 0;
            for (InventoryMetrics metrics : allMetrics) {
                RestockCalculationResponse calc = restockOptimizer.calculateRestock(metrics);
                if (calc.getOrderQuantity() > 0) {
                    Product product = skuMap.get(metrics.getSkuId());
                    if (product != null) {
                        notificationService.createLowStockAlert(product, calc);
                        alertCount++;
                    }
                }
            }
            logger.info("Low-stock alert check complete. {} product(s) flagged.", alertCount);
        } catch (Exception e) {
            logger.error("Error during low-stock alert check", e);
        }
    }
}
