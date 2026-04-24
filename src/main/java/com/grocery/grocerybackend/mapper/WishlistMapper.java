package com.grocery.grocerybackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grocery.grocerybackend.entity.Wishlist;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WishlistMapper extends BaseMapper<Wishlist> {
}
