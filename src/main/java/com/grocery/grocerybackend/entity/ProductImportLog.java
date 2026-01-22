package com.grocery.grocerybackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product_import_log")
public class ProductImportLog {
    @TableId(type = IdType.AUTO) private Long id;

    private Long productId;
    private String supplierSku;

    private Integer oldStock;
    private Integer addedStock;
    private Integer newStock;

    private BigDecimal costPrice;
    private BigDecimal sellingPrice;

    private LocalDateTime importedAt;
}
