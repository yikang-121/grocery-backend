// entity/UserAddress.java
package com.grocery.grocerybackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("user_address")
public class UserAddress {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("user_id")
    private Long userId;
    private String label;          // e.g. Home, Office
    private String name;           // receiver/display name
    private String phone;
    @TableField("address_line")
    private String addressLine;
    private String city;
    private String state;
    private String postal;
    @TableField("is_default")
    private Integer isDefault;     // 0/1
    @TableField("created_at")
    private java.time.LocalDateTime createdAt;
    @TableField("updated_at")
    private java.time.LocalDateTime updatedAt;
}
