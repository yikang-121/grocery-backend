-- V3_wishlist_points_tables.sql
-- Create tables for Wishlist and Loyalty Points system

USE grocery_db;

-- 1. Wishlist Table: Tracks products favorited by users
CREATE TABLE IF NOT EXISTS wishlist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY (user_id, product_id) -- Prevent duplicate entries for the same product per user
);

-- 2. Loyalty Points Table: Stores users' current points balance
CREATE TABLE IF NOT EXISTS loyalty_points (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    balance INT DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 3. Point History Table: Detailed log of points earned or spent
CREATE TABLE IF NOT EXISTS point_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount INT NOT NULL,
    description VARCHAR(255),
    type ENUM('EARNED', 'SPENT') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
