package com.grocery.grocerybackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.grocery.grocerybackend.entity.LoyaltyPoint;
import com.grocery.grocerybackend.entity.PointHistory;
import com.grocery.grocerybackend.mapper.LoyaltyPointMapper;
import com.grocery.grocerybackend.mapper.PointHistoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PointsService {

    private final LoyaltyPointMapper loyaltyPointMapper;
    private final PointHistoryMapper pointHistoryMapper;

    public PointsService(LoyaltyPointMapper loyaltyPointMapper, PointHistoryMapper pointHistoryMapper) {
        this.loyaltyPointMapper = loyaltyPointMapper;
        this.pointHistoryMapper = pointHistoryMapper;
    }

    public LoyaltyPoint getBalance(Long userId) {
        LoyaltyPoint p = loyaltyPointMapper.selectOne(
                new QueryWrapper<LoyaltyPoint>().eq("user_id", userId));
        if (p == null) {
            p = new LoyaltyPoint();
            p.setUserId(userId);
            p.setBalance(0);
            loyaltyPointMapper.insert(p);
        }
        return p;
    }

    public List<PointHistory> getHistory(Long userId) {
        return pointHistoryMapper.selectList(
                new QueryWrapper<PointHistory>().eq("user_id", userId).orderByDesc("id"));
    }

    @Transactional
    public void earnPoints(Long userId, BigDecimal amount, String description) {
        int earned = amount.intValue(); // 1 point for every 1 RM
        if (earned <= 0) return;

        LoyaltyPoint lp = getBalance(userId);
        lp.setBalance(lp.getBalance() + earned);
        loyaltyPointMapper.updateById(lp);

        PointHistory history = new PointHistory();
        history.setUserId(userId);
        history.setAmount(earned);
        history.setDescription(description);
        history.setType("EARNED");
        pointHistoryMapper.insert(history);
    }

    @Transactional
    public void redeemPoints(Long userId, int points, String description) {
        if (points <= 0) return;

        LoyaltyPoint lp = getBalance(userId);
        if (lp.getBalance() < points) {
            throw new RuntimeException("Insufficient points balance");
        }

        lp.setBalance(lp.getBalance() - points);
        loyaltyPointMapper.updateById(lp);

        PointHistory history = new PointHistory();
        history.setUserId(userId);
        history.setAmount(points);
        history.setDescription(description);
        history.setType("SPENT");
        pointHistoryMapper.insert(history);
    }
}
