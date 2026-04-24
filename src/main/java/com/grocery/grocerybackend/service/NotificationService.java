package com.grocery.grocerybackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.grocery.grocerybackend.dto.RestockCalculationResponse;
import com.grocery.grocerybackend.entity.AdminNotification;
import com.grocery.grocerybackend.entity.Product;
import com.grocery.grocerybackend.entity.PurchaseOrder;
import com.grocery.grocerybackend.entity.PurchaseOrderItem;
import com.grocery.grocerybackend.mapper.AdminNotificationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final AdminNotificationMapper notificationMapper;

    public NotificationService(AdminNotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    /**
     * Create a notification when a draft Purchase Order is auto-generated.
     */
    public void createRestockNotification(PurchaseOrder po, List<PurchaseOrderItem> items) {
        try {
            String productSummary = items.stream()
                    .map(i -> i.getProductName() + " (x" + i.getQuantityOrdered() + ")")
                    .collect(Collectors.joining(", "));

            // Truncate if too long
            if (productSummary.length() > 500) {
                productSummary = productSummary.substring(0, 497) + "...";
            }

            AdminNotification notification = new AdminNotification();
            notification.setType("RESTOCK_PO_GENERATED");
            notification.setTitle("New Restock PO: " + po.getPoNumber());
            notification.setMessage("A draft purchase order has been auto-generated with " 
                    + items.size() + " item(s): " + productSummary 
                    + ". Total cost: RM " + po.getTotalCost().setScale(2).toPlainString() 
                    + ". Please review and approve.");
            notification.setReferenceId(po.getId());
            notification.setIsRead(false);
            notification.setCreatedAt(new Timestamp(System.currentTimeMillis()));

            notificationMapper.insert(notification);
            logger.info("Created restock notification for PO: {}", po.getPoNumber());
        } catch (Exception e) {
            // Don't fail the PO creation if notification fails
            logger.error("Failed to create notification for PO: {}", po.getPoNumber(), e);
        }
    }

    /**
     * Create a LOW_STOCK_ALERT notification when the restock algorithm detects
     * that a product's stock has dropped below its calculated safety stock level.
     * Includes deduplication: won't create a duplicate if an unread alert for
     * the same product (by productId) already exists.
     */
    public void createLowStockAlert(Product product, RestockCalculationResponse calc) {
        try {
            // Deduplication: skip if an unread LOW_STOCK_ALERT for this product already exists
            Long existing = notificationMapper.selectCount(
                    new QueryWrapper<AdminNotification>()
                            .eq("type", "LOW_STOCK_ALERT")
                            .eq("reference_id", product.getId())
                            .eq("is_read", false));
            if (existing > 0) {
                return; // already alerted, don't spam
            }

            AdminNotification notification = new AdminNotification();
            notification.setType("LOW_STOCK_ALERT");
            notification.setTitle("Low Stock: " + product.getName());
            notification.setMessage(
                    product.getName() + " (SKU: " + product.getSku() + ") is running low. "
                    + "Current stock: " + product.getStockQuantity() + " units. "
                    + "Safety stock level: " + (int) calc.getSafetyStock() + " units. "
                    + "Suggested restock quantity: " + calc.getOrderQuantity() + " units. "
                    + "Please review and place a restock order.");
            notification.setReferenceId(product.getId());
            notification.setIsRead(false);
            notification.setCreatedAt(new Timestamp(System.currentTimeMillis()));

            notificationMapper.insert(notification);
            logger.info("Created low stock alert for product: {} (stock: {})",
                    product.getName(), product.getStockQuantity());
        } catch (Exception e) {
            logger.error("Failed to create low stock alert for product: {}", product.getName(), e);
        }
    }

    /**
     * Get all notifications, newest first.
     */
    public List<AdminNotification> getAllNotifications() {
        return notificationMapper.selectList(
                new QueryWrapper<AdminNotification>().orderByDesc("created_at"));
    }

    /**
     * Get unread notifications count.
     */
    public Long getUnreadCount() {
        return notificationMapper.selectCount(
                new QueryWrapper<AdminNotification>().eq("is_read", false));
    }

    /**
     * Mark a single notification as read.
     */
    public void markAsRead(Long id) {
        AdminNotification notification = notificationMapper.selectById(id);
        if (notification != null) {
            notification.setIsRead(true);
            notificationMapper.updateById(notification);
        }
    }

    /**
     * Mark all notifications as read.
     */
    public void markAllAsRead() {
        AdminNotification update = new AdminNotification();
        update.setIsRead(true);
        notificationMapper.update(update,
                new UpdateWrapper<AdminNotification>().eq("is_read", false));
    }
}
