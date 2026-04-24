package com.grocery.grocerybackend.controller;

import com.grocery.grocerybackend.entity.LoyaltyPoint;
import com.grocery.grocerybackend.entity.PointHistory;
import com.grocery.grocerybackend.service.PointsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/points")
@CrossOrigin(origins = "http://localhost:3000")
public class PointsController {

    private final PointsService pointsService;

    public PointsController(PointsService pointsService) {
        this.pointsService = pointsService;
    }

    @GetMapping("/balance/{userId}")
    public LoyaltyPoint getBalance(@PathVariable Long userId) {
        return pointsService.getBalance(userId);
    }

    @GetMapping("/history/{userId}")
    public List<PointHistory> getHistory(@PathVariable Long userId) {
        return pointsService.getHistory(userId);
    }
}
