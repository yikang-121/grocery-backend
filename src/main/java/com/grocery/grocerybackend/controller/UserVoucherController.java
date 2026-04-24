package com.grocery.grocerybackend.controller;

import com.grocery.grocerybackend.entity.UserVoucher;
import com.grocery.grocerybackend.service.VoucherService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@CrossOrigin(origins = "http://localhost:3000")
public class UserVoucherController {

    private final VoucherService voucherService;

    public UserVoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @GetMapping("/my-vouchers")
    public List<UserVoucher> getMyVouchers(@RequestParam Long userId) {
        return voucherService.getActiveVouchers(userId);
    }

    @PostMapping("/redeem")
    public UserVoucher redeem(@RequestParam Long userId, @RequestParam String tier) {
        return voucherService.redeemVoucher(userId, tier);
    }
}
