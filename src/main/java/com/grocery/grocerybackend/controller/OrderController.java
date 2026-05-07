package com.grocery.grocerybackend.controller;

import com.grocery.grocerybackend.dto.CancelOrderRequest;
import com.grocery.grocerybackend.dto.CreateOrderRequest;
import com.grocery.grocerybackend.dto.OrderResponse;
import com.grocery.grocerybackend.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:3000")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderResponse create(@RequestBody CreateOrderRequest req) {
        return orderService.createOrder(req);
    }

    @GetMapping
    public List<OrderResponse> list(@RequestParam(required = false) Long userId) {
        return orderService.listOrders(userId);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @PostMapping("/{id}/cancel")
    public void cancel(@PathVariable Long id) {
        orderService.cancel(id);
    }

    @PutMapping("/{orderNo}/cancel")
    public void cancelByOrderNo(
            @PathVariable String orderNo,
            @RequestBody CancelOrderRequest req
    ) {
        orderService.cancelByOrderNo(orderNo, req);
    }

    // ===== Admin Endpoints =====

    @PutMapping("/{orderNo}/status")
    public Map<String, String> updateStatus(
            @PathVariable String orderNo,
            @RequestBody Map<String, String> body
    ) {
        String newStatus = body.get("status");
        orderService.updateOrderStatus(orderNo, newStatus);
        return Map.of("status", "ok", "newStatus", newStatus);
    }

    @PutMapping("/bulk-status")
    public Map<String, Object> bulkUpdateStatus(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> orderNos = (List<String>) body.get("orderNos");
        String newStatus = (String) body.get("status");
        int updated = 0;
        for (String orderNo : orderNos) {
            try {
                orderService.updateOrderStatus(orderNo, newStatus);
                updated++;
            } catch (Exception e) {
                System.err.println("Failed to update order " + orderNo + ": " + e.getMessage());
            }
        }
        return Map.of("status", "ok", "updated", updated, "total", orderNos.size());
    }
}
