// dto/CreateOrderRequest.java
package com.grocery.grocerybackend.dto;

import java.util.List;

public class CreateOrderRequest {
    public Long userId; // replace with auth user later
    public List<Line> items;
    public String shippingAddressJson;
    public String notes;
    public String paymentMethod; // enum name
    public String paymentDetails; // JSON string

    public static class Line {
        public Long productId;
        public Integer qty;
    }
}
