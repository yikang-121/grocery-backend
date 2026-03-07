package com.grocery.grocerybackend.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    private String stripeSecretKey = "sk_test_51T7dTdAOBZ0b6qbnSLrkIwYmeu0UlolySZNYRd6qYC1t8leYxjQOfkaiOyrduIrQJYjUm0bgsKwuP0O2l8CVEphK00hDVlt0vL";

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
        System.out.println("Stripe Initialized with key: " + stripeSecretKey.substring(0, 10) + "...");
    }

    public Map<String, String> createPaymentIntent(BigDecimal amount, String currency) throws StripeException {
        try {
            // Stripe expects amount in cents (e.g. RM 1.00 = 100 cents)
            long amountInCents = amount.multiply(new BigDecimal("100")).longValue();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(currency != null ? currency.toLowerCase() : "myr")
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            Map<String, String> responseData = new HashMap<>();
            responseData.put("clientSecret", intent.getClientSecret());
            return responseData;
        } catch (StripeException e) {
            System.err.println("Stripe Error: " + e.getMessage());
            throw e;
        }
    }
}
