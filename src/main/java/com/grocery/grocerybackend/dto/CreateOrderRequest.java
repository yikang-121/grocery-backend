// dto/CreateOrderRequest.java
package com.grocery.grocerybackend.dto;

import java.util.List;

public class CreateOrderRequest {
    public Long userId;
    public List<Line> items;
    public String shippingAddressJson;
    public String notes;
    public String paymentMethod;
    public String paymentDetails;
    public Boolean usePoints;
    public Integer pointsToUse;
    public Long userVoucherId;

    public static class Line {
        public Long productId;
        public Integer qty;
    }
}
