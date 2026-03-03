package com.grocery.grocerybackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@TableName("spoilage_log")
public class SpoilageLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long batchId;
    private Long productId;
    private Integer quantity;
    private BigDecimal costPrice;
    private BigDecimal totalLoss;
    private String reason;
    private Timestamp createdAt;
}
