package com.grocery.grocerybackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.grocery.grocerybackend.entity.UserVoucher;
import com.grocery.grocerybackend.mapper.UserVoucherMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class VoucherService {

    private final UserVoucherMapper voucherMapper;
    private final PointsService pointsService;

    public VoucherService(UserVoucherMapper voucherMapper, PointsService pointsService) {
        this.voucherMapper = voucherMapper;
        this.pointsService = pointsService;
    }

    @Transactional
    public UserVoucher redeemVoucher(Long userId, String tier) {
        int pointsNeeded;
        BigDecimal discount;

        switch (tier) {
            case "RM5":
                pointsNeeded = 500;
                discount = new BigDecimal("5.00");
                break;
            case "RM10":
                pointsNeeded = 1000;
                discount = new BigDecimal("10.00");
                break;
            case "RM25":
                pointsNeeded = 2000;
                discount = new BigDecimal("25.00");
                break;
            default:
                throw new IllegalArgumentException("Invalid voucher tier");
        }

        // Deduct points
        pointsService.redeemPoints(userId, pointsNeeded, "Redeemed for " + tier + " Voucher");

        // Create Voucher
        UserVoucher v = new UserVoucher();
        v.setUserId(userId);
        v.setCode("VOUCH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        v.setDiscountAmount(discount);
        v.setIsUsed(false);
        v.setExpiryAt(LocalDateTime.now().plusDays(30)); // 30 days expiry
        voucherMapper.insert(v);

        return v;
    }

    public List<UserVoucher> getActiveVouchers(Long userId) {
        return voucherMapper.selectList(new QueryWrapper<UserVoucher>()
                .eq("user_id", userId)
                .eq("is_used", false)
                .gt("expiry_at", LocalDateTime.now()));
    }

    public UserVoucher getVoucher(Long id) {
        return voucherMapper.selectById(id);
    }

    @Transactional
    public void markAsUsed(Long id) {
        UserVoucher v = voucherMapper.selectById(id);
        if (v != null) {
            v.setIsUsed(true);
            voucherMapper.updateById(v);
        }
    }
}
