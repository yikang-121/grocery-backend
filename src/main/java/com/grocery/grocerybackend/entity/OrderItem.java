// entity/OrderItem.java
package com.grocery.grocerybackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("order_items") // if you kept order_item, change the name here
public class OrderItem {
    @TableId(type = IdType.AUTO) private Long id;
    private Long orderId;
    private Long productId;
    private String productName;
    @TableField("unit_price") private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal lineTotal;

}
