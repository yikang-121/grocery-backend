package com.grocery.grocerybackend.controller;

import com.grocery.grocerybackend.dto.CancelOrderRequest;
import com.grocery.grocerybackend.dto.CreateOrderRequest;
import com.grocery.grocerybackend.dto.OrderResponse;
import com.grocery.grocerybackend.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public List<OrderResponse> list(@RequestParam Long userId) {
        return orderService.listOrders(userId); // service filters by user_id
    }


    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @PostMapping("/{id}/cancel")
    public void cancel(@PathVariable Long id) {
        orderService.cancel(id);
    }

    // controller/OrderController.java
    @PutMapping("/{orderNo}/cancel")
    public void cancelByOrderNo(
            @PathVariable String orderNo,
            @RequestBody CancelOrderRequest req
    ) {
        orderService.cancelByOrderNo(orderNo, req);
    }



}
