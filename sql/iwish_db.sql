-- ==========================================================
-- i-Wish Project Database Schema & Seed Data
-- Compatible with MySQL 5.7+ / MySQL 8.0+ / MariaDB
-- ==========================================================

DROP DATABASE IF EXISTS iwish_db;
CREATE DATABASE iwish_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE iwish_db;

-- ----------------------------------------------------------
-- 1. Table: users
-- ----------------------------------------------------------
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    balance DECIMAL(10, 2) NOT NULL DEFAULT 1000.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 2. Table: products (Global Catalog managed by Server / Admin)
-- ----------------------------------------------------------
CREATE TABLE products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    category VARCHAR(50) DEFAULT 'General',
    image_url VARCHAR(255) DEFAULT 'default_product.png',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 3. Table: friend_requests & friendships
-- ----------------------------------------------------------
CREATE TABLE friend_requests (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    status ENUM('PENDING', 'ACCEPTED', 'DECLINED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY uq_friend_pair (sender_id, receiver_id)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 4. Table: wish_list_items
-- ----------------------------------------------------------
CREATE TABLE wish_list_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    status ENUM('AVAILABLE', 'COMPLETED') NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 5. Table: contributions
-- ----------------------------------------------------------
CREATE TABLE contributions (
    contribution_id INT AUTO_INCREMENT PRIMARY KEY,
    item_id INT NOT NULL,
    contributor_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    contributed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES wish_list_items(item_id) ON DELETE CASCADE,
    FOREIGN KEY (contributor_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 6. Table: notifications
-- ----------------------------------------------------------
CREATE TABLE notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    type VARCHAR(50) NOT NULL, -- 'GIFT_COMPLETED_BUYER', 'GIFT_BOUGHT_RECEIVER', 'FRIEND_REQUEST', 'FRIEND_ACCEPTED'
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ==========================================================
-- SEED DATA FOR DEMO & TESTING
-- ==========================================================

-- Seed Users (default password is '123456' hashed or plain text for demo)
INSERT INTO users (user_id, username, email, password_hash, full_name, balance) VALUES
(1, 'ahmed', 'ahmed@iwish.com', '123456', 'Ahmed Hassan', 1500.00),
(2, 'mohamed', 'mohamed@iwish.com', '123456', 'Mohamed Ali', 1200.00),
(3, 'sara', 'sara@iwish.com', '123456', 'Sara Mahmoud', 2000.00),
(4, 'omar', 'omar@iwish.com', '123456', 'Omar Khaled', 800.00);

-- Seed Products Catalog (electronics, gaming, gadgets)
INSERT INTO products (product_id, name, description, price, category, image_url) VALUES
(1, 'Sony WH-1000XM5 Wireless Headphones', 'Industry-leading noise canceling wireless over-ear headphones with premium microphone.', 350.00, 'Audio', 'headphones.png'),
(2, 'PlayStation 5 Digital Edition', 'Next-gen gaming console with ultra-high speed SSD and ray tracing support.', 450.00, 'Gaming', 'ps5.png'),
(3, 'Apple Watch Series 9 GPS', 'Smartwatch with advanced health sensors, S9 chip, and Always-On Retina display.', 399.00, 'Wearables', 'smartwatch.png'),
(4, 'Keychron K2 Mechanical Keyboard', 'Wireless Bluetooth compact 75% mechanical keyboard with Gateron Brown switches.', 95.00, 'Accessories', 'keyboard.png'),
(5, 'Kindle Paperwhite (16 GB)', '6.8 inch display with adjustable warm light and up to 10 weeks of battery life.', 140.00, 'Books', 'kindle.png'),
(6, 'Logitech MX Master 3S Mouse', 'Ergonomic performance wireless mouse with quiet clicks and 8K DPI tracking.', 99.00, 'Accessories', 'mouse.png'),
(7, 'Nintendo Switch OLED Model', '7-inch vibrant OLED screen, enhanced audio, and 64 GB internal storage.', 320.00, 'Gaming', 'switch.png'),
(8, 'Fujifilm Instax Mini 12 Camera', 'Instant camera with automatic exposure and close-up selfie mode.', 75.00, 'Photography', 'camera.png');

-- Seed Friendships (Ahmed and Mohamed are friends; Sara sent request to Ahmed)
INSERT INTO friend_requests (sender_id, receiver_id, status) VALUES
(1, 2, 'ACCEPTED'), -- Ahmed & Mohamed are friends
(2, 1, 'ACCEPTED'), -- Bidirectional helper
(3, 1, 'PENDING'),  -- Sara wants to add Ahmed
(4, 2, 'ACCEPTED'), -- Omar & Mohamed are friends
(2, 4, 'ACCEPTED');

-- Seed Wishlist Items for Mohamed (User 2)
INSERT INTO wish_list_items (item_id, user_id, product_id, status) VALUES
(1, 2, 1, 'AVAILABLE'), -- Sony Headphones ($350)
(2, 2, 4, 'COMPLETED'); -- Mechanical Keyboard ($95)

-- Seed Contributions for Mohamed's Wishlist:
-- Item 1 (Headphones $350): Ahmed contributed $150 (Remaining: $200)
INSERT INTO contributions (item_id, contributor_id, amount) VALUES
(1, 1, 150.00);

-- Item 2 (Keyboard $95): Ahmed paid $50, Omar paid $45 -> Total $95 (Completed!)
INSERT INTO contributions (item_id, contributor_id, amount) VALUES
(2, 1, 50.00),
(2, 4, 45.00);

-- Seed Notifications
INSERT INTO notifications (user_id, type, message, is_read) VALUES
(2, 'GIFT_BOUGHT_RECEIVER', '🎉 Congratulations! Your wish item "Keychron K2 Mechanical Keyboard" was fully funded by Ahmed Hassan and Omar Khaled!', FALSE),
(1, 'GIFT_COMPLETED_BUYER', '🎁 Great news! The gift "Keychron K2 Mechanical Keyboard" for Mohamed Ali has been fully funded!', FALSE),
(4, 'GIFT_COMPLETED_BUYER', '🎁 Great news! The gift "Keychron K2 Mechanical Keyboard" for Mohamed Ali has been fully funded!', FALSE);
