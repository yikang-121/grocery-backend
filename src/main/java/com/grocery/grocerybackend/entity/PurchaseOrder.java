package com.grocery.grocerybackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Data
@TableName("purchase_orders")
public class PurchaseOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String poNumber;
    private String supplierName;
    private String status; // PENDING_APPROVAL, APPROVED, RECEIVED, CANCELLED
    private BigDecimal totalCost;
    private BigDecimal taxAmount;
    private String notes;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp approvedAt;
    private Timestamp receivedAt;

    @TableField(exist = false)
    private List<PurchaseOrderItem> items;
}
