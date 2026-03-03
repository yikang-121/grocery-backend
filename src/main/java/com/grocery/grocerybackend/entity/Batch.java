package com.grocery.grocerybackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDate;

@Data
@TableName("batch")
public class Batch {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;
    private String batchNo;
    private LocalDate expiryDate;
    private Integer availableQuantity;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
