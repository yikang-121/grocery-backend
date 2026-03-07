package com.grocery.grocerybackend.controller;

import com.grocery.grocerybackend.dto.CreatePaymentIntentRequest;
import com.grocery.grocerybackend.service.PaymentService;
import com.stripe.exception.StripeException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-payment-intent")
    public Map<String, Object> createPaymentIntent(@RequestBody CreatePaymentIntentRequest request) {
        try {
            return (Map<String, Object>) (Map) paymentService.createPaymentIntent(request.getAmount(),
                    request.getCurrency());
        } catch (StripeException e) {
            System.err.println("Stripe Intent Creation Failed: " + e.getMessage());
            return Map.of("error", e.getMessage());
        } catch (Exception e) {
            System.err.println("General Error: " + e.getMessage());
            return Map.of("error", "Internal server error: " + e.getMessage());
        }
    }
}
