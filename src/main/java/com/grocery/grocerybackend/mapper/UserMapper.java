package com.grocery.grocerybackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grocery.grocerybackend.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    // Use the correct column name based on your database schema
    @Update("""
        UPDATE user 
        SET password = #{hash}, updated_at = NOW()
        WHERE id = #{id}
        """)
    int updatePassword(@Param("id") Long id, @Param("hash") String hash);

}