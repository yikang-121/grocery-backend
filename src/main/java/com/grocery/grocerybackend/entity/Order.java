// entity/Order.java
package com.grocery.grocerybackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {
    @TableId(type = IdType.AUTO) private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal discount;
    @TableField("total_amount") private BigDecimal total; // maps to your column
    private String status;         // use enums as String
    private String paymentMethod;
    private String shippingAddress; // JSON/TEXT
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField("cancel_reason")
    private String cancelReason;

    @TableField("cancel_resolution")
    private String cancelResolution;

}
