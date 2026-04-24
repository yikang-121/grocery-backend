package com.grocery.grocerybackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_vouchers")
public class UserVoucher {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String code;
    private BigDecimal discountAmount;
    private Boolean isUsed;
    private LocalDateTime expiryAt;
    private LocalDateTime createdAt;
}
