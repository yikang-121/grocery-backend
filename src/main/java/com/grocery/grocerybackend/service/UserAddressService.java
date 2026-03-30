// src/main/java/com/grocery/grocerybackend/service/UserAddressService.java
package com.grocery.grocerybackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.grocery.grocerybackend.dto.AddressRequest;
import com.grocery.grocerybackend.dto.AddressResponse;
import com.grocery.grocerybackend.entity.UserAddress;
import com.grocery.grocerybackend.mapper.UserAddressMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserAddressService {
    private final UserAddressMapper mapper;

    public UserAddressService(UserAddressMapper mapper) {
        this.mapper = mapper;
    }

    public List<UserAddress> listByUser(Long userId) {
        return mapper.selectList(new QueryWrapper<UserAddress>()
                .eq("user_id", userId)
                .orderByDesc("is_default").orderByDesc("updated_at"));
    }

    @Transactional
    public UserAddress create(AddressRequest r) {
        if (Boolean.TRUE.equals(r.isDefault))
            mapper.clearDefault(r.userId);
        UserAddress ua = new UserAddress();
        ua.setUserId(r.userId);
        ua.setLabel(r.label);
        ua.setName(r.name);
        ua.setPhone(r.phone);
        ua.setAddressLine(r.addressLine);
        ua.setCity(r.city);
        ua.setState(r.state);
        ua.setPostal(r.postal);
        ua.setIsDefault(Boolean.TRUE.equals(r.isDefault) ? 1 : 0);
        mapper.insert(ua);
        return mapper.selectById(ua.getId());
    }

    @Transactional
    public UserAddress update(Long id, AddressRequest r) {
        UserAddress exist = mapper.selectById(id);
        if (exist == null || !exist.getUserId().equals(r.userId))
            throw new IllegalArgumentException("Address not found");

        if (Boolean.TRUE.equals(r.isDefault))
            mapper.clearDefault(r.userId);

        exist.setLabel(r.label);
        exist.setName(r.name);
        exist.setPhone(r.phone);
        exist.setAddressLine(r.addressLine);
        exist.setCity(r.city);
        exist.setState(r.state);
        exist.setPostal(r.postal);
        if (r.isDefault != null)
            exist.setIsDefault(r.isDefault ? 1 : 0);

        mapper.updateById(exist);
        return mapper.selectById(id);
    }

    @Transactional
    public void setDefault(Long userId, Long id) {
        mapper.clearDefault(userId);
        UserAddress ua = mapper.selectById(id);
        if (ua == null || !ua.getUserId().equals(userId))
            throw new IllegalArgumentException("Address not found");
        ua.setIsDefault(1);
        mapper.updateById(ua);
    }

    public void delete(Long userId, Long id) {
        mapper.delete(new QueryWrapper<UserAddress>()
                .eq("user_id", userId).eq("id", id));
    }
}
