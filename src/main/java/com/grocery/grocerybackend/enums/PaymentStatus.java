// enums/PaymentStatus.java
package com.grocery.grocerybackend.enums;


public enum PaymentStatus {
    INIT,              // Created, not yet paid
    CAPTURED,          // Paid successfully
    FAILED,            // Payment attempt failed
    REFUND_PENDING,    // Refund requested, not processed yet
    REFUNDED,          // Refund completed
    CANCELLED          // Payment voided due to order cancellation before capture
}

