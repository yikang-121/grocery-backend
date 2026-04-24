package com.grocery.grocerybackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.grocery.grocerybackend.dto.CancelOrderRequest;
import com.grocery.grocerybackend.dto.CreateOrderRequest;
import com.grocery.grocerybackend.dto.OrderResponse;
import com.grocery.grocerybackend.entity.*;
import com.grocery.grocerybackend.enums.*;
import com.grocery.grocerybackend.mapper.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final PaymentMapper paymentMapper;
    private final ProductMapper productMapper;
    private final InventoryService inventoryService;
    private final PointsService pointsService;
    private final VoucherService voucherService;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private StockMovementService stockMovementService;

    public OrderService(OrderMapper orderMapper, OrderItemMapper orderItemMapper,
            PaymentMapper paymentMapper, ProductMapper productMapper,
            InventoryService inventoryService, PointsService pointsService,
            VoucherService voucherService, UserMapper userMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.paymentMapper = paymentMapper;
        this.productMapper = productMapper;
        this.inventoryService = inventoryService;
        this.pointsService = pointsService;
        this.voucherService = voucherService;
        this.userMapper = userMapper;
    }

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setStockMovementService(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest req) {
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (var line : req.items) {
            Product p = productMapper.selectById(line.productId);
            if (p == null)
                throw new IllegalArgumentException("Product not found: " + line.productId);
            if (p.getStockQuantity() < line.qty)
                throw new IllegalStateException("Insufficient stock: " + p.getName());

            BigDecimal unit = p.getPrice();
            BigDecimal lineTotal = unit.multiply(BigDecimal.valueOf(line.qty));
            subtotal = subtotal.add(lineTotal);

            OrderItem oi = new OrderItem();
            oi.setProductId(p.getId());
            oi.setProductName(p.getName());
            oi.setUnitPrice(unit);
            oi.setQuantity(line.qty);
            oi.setLineTotal(lineTotal);
            items.add(oi);
        }

        BigDecimal shippingFee = (subtotal.compareTo(new BigDecimal("100")) >= 0) ? BigDecimal.ZERO : new BigDecimal("8");
        BigDecimal total = subtotal.add(shippingFee);

        Order order = new Order();
        order.setOrderNo(UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
        order.setUserId(req.userId);

         // Handle points discount
        BigDecimal pointsDiscount = BigDecimal.ZERO;
        if (req.usePoints != null && req.usePoints && req.pointsToUse != null && req.pointsToUse > 0) {
            LoyaltyPoint lp = pointsService.getBalance(req.userId);
            int pointsToDeduct = Math.min(lp.getBalance(), req.pointsToUse);
            pointsDiscount = new BigDecimal(pointsToDeduct).divide(new BigDecimal("100"));
            if (pointsToDeduct > 0) {
                pointsService.redeemPoints(req.userId, pointsToDeduct, "Used for order: " + order.getOrderNo());
            }
        }

        // Handle voucher discount
        BigDecimal voucherDiscount = BigDecimal.ZERO;
        if (req.userVoucherId != null) {
            UserVoucher v = voucherService.getVoucher(req.userVoucherId);
            if (v != null && !v.getIsUsed() && v.getUserId().equals(req.userId)) {
                voucherDiscount = v.getDiscountAmount();
                voucherService.markAsUsed(v.getId());
            }
        }

        BigDecimal discount = pointsDiscount.add(voucherDiscount);

        // Cap discount to order total
        if (discount.compareTo(total) > 0) {
            discount = total;
        }

        total = total.subtract(discount);

        order.setSubtotal(subtotal);
        order.setShippingFee(shippingFee);
        order.setDiscount(discount);
        order.setTotal(total);
        order.setStatus("PAID"); 
        order.setPaymentMethod(req.paymentMethod);
        order.setPaymentDetails(req.paymentDetails);
        order.setShippingAddress(req.shippingAddressJson);
        order.setNotes(req.notes);
        orderMapper.insert(order);

        for (OrderItem oi : items) {
            oi.setOrderId(order.getId());
            orderItemMapper.insert(oi);
            // Use FEFO deduction which updates both batch and product tables
            inventoryService.deductStockFEFO(oi.getProductId(), oi.getQuantity());
        }

        Payment pay = new Payment();
        pay.setOrderId(order.getId());
        pay.setAmount(total);
        pay.setMethod(req.paymentMethod);
        pay.setStatus(PaymentStatus.INIT.name());
        paymentMapper.insert(pay);

        // Award points: 1 pt per RM 1
        try {
            pointsService.earnPoints(req.userId, total, "Points earned from order: " + order.getOrderNo());
        } catch (Exception e) {
            // Log error but don't block order creation
            System.err.println("Error awarding points: " + e.getMessage());
        }

        return toFrontendResponse(order, items);
    }

    public List<OrderResponse> listOrders(Long userId) {
        QueryWrapper<Order> qw = new QueryWrapper<>();
        if (userId != null) {
            qw.eq("user_id", userId);
        }
        qw.orderByDesc("created_at");
        List<Order> orders = orderMapper.selectList(qw);

        return orders.stream().map(o -> {
            List<OrderItem> items = orderItemMapper.selectList(
                    new QueryWrapper<OrderItem>().eq("order_id", o.getId()));
            return toFrontendResponse(o, items);
        }).collect(Collectors.toList());
    }

    public OrderResponse getOrder(Long id) {
        Order o = orderMapper.selectById(id);
        if (o == null)
            throw new IllegalArgumentException("Order not found");
        List<OrderItem> items = orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", id));
        return toFrontendResponse(o, items);
    }

    @Transactional
    public void cancel(Long id) {
        Order o = orderMapper.selectById(id);
        if (o == null)
            throw new IllegalArgumentException("Order not found");
        if (!OrderStatus.PENDING.name().equals(o.getStatus()))
            throw new IllegalStateException("Only PENDING orders can be cancelled");

        o.setStatus(OrderStatus.CANCELLED.name());
        orderMapper.updateById(o);

        List<OrderItem> items = orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", id));
        for (OrderItem it : items) {
            productMapper.incrementStock(it.getProductId(), it.getQuantity());
            // Log cancel return movement
            if (stockMovementService != null) {
                stockMovementService.logMovement(
                        it.getProductId(), null, "CANCEL_RETURN",
                        it.getQuantity(), "ORDER", o.getId(),
                        "Order cancelled: " + o.getOrderNo());
            }
        }
    }

    private OrderResponse toFrontendResponse(Order o, List<OrderItem> items) {
        OrderResponse r = new OrderResponse();
        r.id = o.getOrderNo();
        r.date = o.getCreatedAt() != null ? o.getCreatedAt().toString() : null;
        r.status = o.getStatus();
        r.subtotal = o.getSubtotal();
        r.shippingFee = o.getShippingFee();
        r.discount = o.getDiscount();
        r.total = o.getTotal();
        r.paymentMethod = o.getPaymentMethod();
        r.userId = o.getUserId();

        // Enrich with customer info
        if (o.getUserId() != null) {
            try {
                User user = userMapper.selectById(o.getUserId());
                if (user != null) {
                    r.customerName = user.getName();
                    r.customerEmail = user.getEmail();
                }
            } catch (Exception ignored) {}
        }

        r.items = items.stream().map(oi -> {
            OrderResponse.Item i = new OrderResponse.Item();
            i.id = oi.getProductId(); // or oi.getId() if you prefer item id
            i.name = oi.getProductName();
            i.qty = oi.getQuantity();
            i.price = oi.getUnitPrice();
            i.lineTotal = oi.getLineTotal() != null
                    ? oi.getLineTotal()
                    : oi.getUnitPrice().multiply(new BigDecimal(oi.getQuantity()));
            return i;
        }).collect(Collectors.toList());

        // Parse shipping address JSON regardless of key variants
        if (o.getShippingAddress() != null) {
            try {
                var node = objectMapper.readTree(o.getShippingAddress());
                OrderResponse.ShippingAddress sa = new OrderResponse.ShippingAddress();
                sa.name = node.path("name").asText("");
                sa.address = node.path("address").asText(node.path("addressLine").asText(""));
                sa.city = node.path("city").asText("");
                sa.postal = node.path("postal").asText(node.path("postalCode").asText(""));
                r.shippingAddress = sa;
            } catch (Exception ignored) {
                r.shippingAddress = null;
            }
        }
        return r;
    }

    // service/OrderService.java (add/replace cancel method)
    @Transactional
    public void cancelByOrderNo(String orderNo, CancelOrderRequest req) {
        // 1) Find order by order_no
        Order o = orderMapper.selectOne(
                new QueryWrapper<Order>().eq("order_no", orderNo));
        if (o == null) {
            throw new IllegalArgumentException("Order not found");
        }
        if (!OrderStatus.PENDING.name().equals(o.getStatus())) {
            throw new IllegalStateException("Only PENDING orders can be cancelled");
        }

        // 2) Mark as cancelled
        o.setStatus(OrderStatus.CANCELLED.name());

        // 3) Store structured cancel data
        o.setCancelReason(req.getReason()); // e.g. "duplicate_order"
        o.setCancelResolution(req.getAction().name()); // "REFUND" | "SUBSTITUTE"

        // 4) Keep only extra free-text in notes (preserve existing notes)
        if (req.getExtraNotes() != null && !req.getExtraNotes().isBlank()) {
            String existing = (o.getNotes() == null ? "" : o.getNotes() + "\n");
            o.setNotes(existing + "[CANCELLED NOTES] " + req.getExtraNotes());
        }
        orderMapper.updateById(o);

        // 5) Restock items
        List<OrderItem> items = orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", o.getId()));
        for (OrderItem it : items) {
            productMapper.incrementStock(it.getProductId(), it.getQuantity());
            // Log cancel return movement
            if (stockMovementService != null) {
                stockMovementService.logMovement(
                        it.getProductId(), null, "CANCEL_RETURN",
                        it.getQuantity(), "ORDER", o.getId(),
                        "Order cancelled: " + o.getOrderNo() + " (" + req.getReason() + ")");
            }
        }

        // 6) Payment status according to your enum
        Payment payment = paymentMapper.selectOne(
                new QueryWrapper<Payment>().eq("order_id", o.getId()));
        if (payment != null) {
            String status = payment.getStatus();
            // INIT -> FAILED (never captured)
            if (PaymentStatus.INIT.name().equals(status)) {
                payment.setStatus(PaymentStatus.FAILED.name());
            }
            // CAPTURED -> REFUNDED if the user requested a refund
            else if (PaymentStatus.CAPTURED.name().equals(status)) {
                if (req.getAction() == CancelOrderRequest.Action.REFUND) {
                    payment.setStatus(PaymentStatus.REFUNDED.name());
                }
                // SUBSTITUTE: keep CAPTURED (no status change)
            }
            // FAILED / REFUNDED: leave as-is
            paymentMapper.updateById(payment);
        }
    }

    /**
     * Admin: update order status by orderNo.
     */
    @Transactional
    public void updateOrderStatus(String orderNo, String newStatus) {
        Order o = orderMapper.selectOne(
                new QueryWrapper<Order>().eq("order_no", orderNo));
        if (o == null) {
            throw new IllegalArgumentException("Order not found: " + orderNo);
        }

        // Validate status
        try {
            OrderStatus.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + newStatus);
        }

        o.setStatus(newStatus);
        orderMapper.updateById(o);
    }

}
