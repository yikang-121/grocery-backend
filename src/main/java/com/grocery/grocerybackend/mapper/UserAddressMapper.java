// src/main/java/com/grocery/grocerybackend/mapper/UserAddressMapper.java
package com.grocery.grocerybackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grocery.grocerybackend.entity.UserAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddress> {

    @Update("UPDATE user_address SET is_default = 0 WHERE user_id = #{userId} AND is_default = 1")
    int clearDefault(Long userId);
}
