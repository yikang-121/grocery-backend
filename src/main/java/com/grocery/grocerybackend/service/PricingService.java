package com.grocery.grocerybackend.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;


@Service
public class PricingService {

    public BigDecimal calculateSelling(BigDecimal cost) {
        if (cost == null || cost.signum() <= 0) return BigDecimal.ZERO;

        BigDecimal markup;
        if (cost.compareTo(new BigDecimal("5")) < 0) {
            markup = new BigDecimal("0.45");
        } else if (cost.compareTo(new BigDecimal("20")) < 0) {
            markup = new BigDecimal("0.40");
        } else if (cost.compareTo(new BigDecimal("50")) < 0) {
            markup = new BigDecimal("0.35");
        } else {
            markup = new BigDecimal("0.30");
        }

        BigDecimal base = cost.multiply(BigDecimal.ONE.add(markup));

        // Enforce minimal gross profit 20%
        BigDecimal min = cost.multiply(new BigDecimal("1.20"));
        if (base.compareTo(min) < 0) base = min;

        return roundToPsychological(base);
    }

    private BigDecimal roundToPsychological(BigDecimal amount) {
        // Round up to nearest 0.10 then set .99
        BigDecimal up = amount
                .divide(new BigDecimal("0.10"), 0, RoundingMode.UP)
                .multiply(new BigDecimal("0.10"));
        BigDecimal as99 = up.setScale(0, RoundingMode.DOWN).add(new BigDecimal("0.99"));

        if (as99.compareTo(up) < 0) {
            as99 = as99.add(BigDecimal.ONE); // ensure not below rounded-up
        }
        return as99.setScale(2, RoundingMode.HALF_UP);
    }
}
