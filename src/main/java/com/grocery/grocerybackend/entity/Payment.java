// entity/Payment.java
package com.grocery.grocerybackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payments")
public class Payment {
    @TableId(type = IdType.AUTO) private Long id;
    private Long orderId;
    private String providerRef;
    private BigDecimal amount;
    private String status;
    private String method;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // getters/setters
}
