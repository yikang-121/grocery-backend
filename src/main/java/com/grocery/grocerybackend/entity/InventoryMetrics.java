package com.grocery.grocerybackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("inventory_metrics")
public class InventoryMetrics {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String skuId;
    private Integer currentStock;
    private Integer leadTimeDays;
    private Integer reviewPeriodDays;
    private Integer shelfLifeDays;
    private Integer supplierMoq;
    @com.baomidou.mybatisplus.annotation.TableField("waste_lambda")
    private Double wasteLambda;

    @com.baomidou.mybatisplus.annotation.TableField("avg_sales_3d")
    private Double avgSales3d;

    @com.baomidou.mybatisplus.annotation.TableField("avg_sales_30d")
    private Double avgSales30d;

    @com.baomidou.mybatisplus.annotation.TableField("std_dev_30d")
    private Double stdDev30d;

    @com.baomidou.mybatisplus.annotation.TableField("seasonality_factor")
    private Double seasonalityFactor;

    @com.baomidou.mybatisplus.annotation.TableField("incoming_stock")
    private Integer incomingStock;

    @com.baomidou.mybatisplus.annotation.TableField("case_size")
    private Integer caseSize;
}
