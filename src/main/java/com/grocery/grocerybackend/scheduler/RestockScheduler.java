package com.grocery.grocerybackend.scheduler;

import com.grocery.grocerybackend.dto.PurchaseOrderDTO;
import com.grocery.grocerybackend.service.PurchaseOrderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RestockScheduler {

    private final PurchaseOrderService purchaseOrderService;

    public RestockScheduler(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    /**
     * Automatically triggers the restock generation process.
     * Scheduled to run every day at 1:00 AM.
     * Cron expression: 0 0 1 * * ? (Seconds Minutes Hours Day-of-month Month Day-of-week)
     * For testing, we can also use a fixed rate, but cron is better for production.
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void scheduleAutoRestock() {
        System.out.println("Starting automated restock check at: " + LocalDateTime.now());
        
        try {
            PurchaseOrderDTO po = purchaseOrderService.autoGeneratePO();
            if (po != null) {
                System.out.println("Automated restock generated Purchase Order: " + po.getPoNumber() + " with " + po.getItems().size() + " items.");
            } else {
                System.out.println("Automated restock check completed: No items need restocking.");
            }
        } catch (Exception e) {
            System.err.println("Error during automated restock check: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
