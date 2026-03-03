package com.grocery.grocerybackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String category;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stockQuantity;
    private String imageUrl;
    private Double rating;
    private Integer ratingCount;
    private String productUrl;
    private BigDecimal costPrice;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String sku;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private List<Batch> batches;
}