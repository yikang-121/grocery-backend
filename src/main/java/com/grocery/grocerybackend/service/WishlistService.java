package com.grocery.grocerybackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.grocery.grocerybackend.entity.Product;
import com.grocery.grocerybackend.entity.Wishlist;
import com.grocery.grocerybackend.mapper.ProductMapper;
import com.grocery.grocerybackend.mapper.WishlistMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistService {

    private final WishlistMapper wishlistMapper;
    private final ProductMapper productMapper;

    public WishlistService(WishlistMapper wishlistMapper, ProductMapper productMapper) {
        this.wishlistMapper = wishlistMapper;
        this.productMapper = productMapper;
    }

    public void addToWishlist(Long userId, Long productId) {
        // Prevent duplicates
        Wishlist existing = wishlistMapper.selectOne(
                new QueryWrapper<Wishlist>().eq("user_id", userId).eq("product_id", productId));
        if (existing == null) {
            Wishlist w = new Wishlist();
            w.setUserId(userId);
            w.setProductId(productId);
            wishlistMapper.insert(w);
        }
    }

    public void removeFromWishlist(Long userId, Long productId) {
        wishlistMapper.delete(new QueryWrapper<Wishlist>().eq("user_id", userId).eq("product_id", productId));
    }

    public List<Product> getUserWishlist(Long userId) {
        List<Wishlist> wishlist = wishlistMapper.selectList(
                new QueryWrapper<Wishlist>().eq("user_id", userId));
        List<Long> productIds = wishlist.stream().map(Wishlist::getProductId).collect(Collectors.toList());
        if (productIds.isEmpty()) return List.of();
        return productMapper.selectBatchIds(productIds);
    }
}
