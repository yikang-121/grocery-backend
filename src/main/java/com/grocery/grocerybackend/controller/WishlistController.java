package com.grocery.grocerybackend.controller;

import com.grocery.grocerybackend.entity.Product;
import com.grocery.grocerybackend.service.WishlistService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@CrossOrigin(origins = "http://localhost:3000")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping("/{userId}")
    public List<Product> getWishlist(@PathVariable Long userId) {
        return wishlistService.getUserWishlist(userId);
    }

    @PostMapping("/{userId}/{productId}")
    public void add(@PathVariable Long userId, @PathVariable Long productId) {
        wishlistService.addToWishlist(userId, productId);
    }

    @DeleteMapping("/{userId}/{productId}")
    public void remove(@PathVariable Long userId, @PathVariable Long productId) {
        wishlistService.removeFromWishlist(userId, productId);
    }
}
