// dto/OrderResponse.java
package com.grocery.grocerybackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public class OrderResponse {
    public String id; // orderNo
    public String date;
    public String status;
    public BigDecimal subtotal;
    public BigDecimal shippingFee;
    public BigDecimal discount;
    public BigDecimal total;
    public String paymentMethod;
    public List<Item> items;
    public ShippingAddress shippingAddress;

    public static class Item {
        public Long id;
        public String name;

        @JsonProperty("quantity") // <— UI expects "quantity"
        public Integer qty;

        public BigDecimal price;

        @JsonProperty("lineTotal") // optional but nice for UI
        public BigDecimal lineTotal;
    }

    public static class ShippingAddress {
        public String name;
        public String address;
        public String city;
        public String postal;
    }
}
