-- c:\FYP\grocery-backend\src\main\resources\V4_user_vouchers.sql
-- Create user_vouchers table for stored discounts

CREATE TABLE IF NOT EXISTS user_vouchers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    discount_amount DECIMAL(10, 2) NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    expiry_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX (user_id)
);
