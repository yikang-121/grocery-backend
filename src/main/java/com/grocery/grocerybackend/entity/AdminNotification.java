package com.grocery.grocerybackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("admin_notifications")
public class AdminNotification {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String type;        // e.g. RESTOCK_PO_GENERATED
    private String title;
    private String message;
    private Long referenceId;   // links to purchase_orders.id
    private Boolean isRead;
    private Timestamp createdAt;
}
