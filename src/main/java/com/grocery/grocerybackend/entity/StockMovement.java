package com.grocery.grocerybackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("stock_movements")
public class StockMovement {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;
    private Long batchId;
    private String movementType;  // STOCK_IN, STOCK_OUT, SPOILAGE, RESTOCK, ORDER_DEDUCT, CANCEL_RETURN
    private Integer quantity;
    private String referenceType; // ORDER, BATCH, SPOILAGE_LOG, PURCHASE_ORDER, CSV_UPLOAD
    private Long referenceId;
    private String notes;
    private Timestamp createdAt;
}
