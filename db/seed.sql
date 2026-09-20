-- ============================================================================
-- RohithMart Seed Data
-- Multi-Seller E-Commerce Marketplace
-- Seed Accounts Default Password: 'Password@123'
-- BCrypt Hash: $2a$10$aehh47.ojjgw70orjtBMte/dSbRoGjNiqYHvjvrjpKK6aCYGSHrHG
-- ============================================================================

-- 1. SEED USERS
-- Admin (Seed account per Capstone F1 specification: no separate signup flow)
INSERT INTO users (id, name, email, password_hash, role, created_at) VALUES
(1, 'System Administrator', 'admin@rohithmart.com', '$2a$10$aehh47.ojjgw70orjtBMte/dSbRoGjNiqYHvjvrjpKK6aCYGSHrHG', 'ADMIN', CURRENT_TIMESTAMP),
(2, 'Apex Tech Solutions', 'techseller@rohithmart.com', '$2a$10$aehh47.ojjgw70orjtBMte/dSbRoGjNiqYHvjvrjpKK6aCYGSHrHG', 'SELLER', CURRENT_TIMESTAMP),
(3, 'Heritage Apparel & Living', 'fashionseller@rohithmart.com', '$2a$10$aehh47.ojjgw70orjtBMte/dSbRoGjNiqYHvjvrjpKK6aCYGSHrHG', 'SELLER', CURRENT_TIMESTAMP),
(4, 'John Buyer', 'john.buyer@example.com', '$2a$10$aehh47.ojjgw70orjtBMte/dSbRoGjNiqYHvjvrjpKK6aCYGSHrHG', 'BUYER', CURRENT_TIMESTAMP),
(5, 'Alice Smith', 'alice.smith@example.com', '$2a$10$aehh47.ojjgw70orjtBMte/dSbRoGjNiqYHvjvrjpKK6aCYGSHrHG', 'BUYER', CURRENT_TIMESTAMP);

-- 2. SEED PRODUCTS
INSERT INTO products (id, seller_id, name, description, price, stock_qty, category, image_url, created_at) VALUES
-- Electronics (Seller ID: 2)
(1, 2, 'ProTactile RGB Mechanical Keyboard', 'Ultra-responsive mechanical keyboard with blue tactile clicky switches, durable aircraft-grade aluminum top plate, and customizable per-key RGB backlighting.', 89.99, 45, 'Electronics', 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=600', CURRENT_TIMESTAMP),
(2, 2, 'ErgoMaster Wireless Precision Mouse', 'Ergonomic vertical wireless mouse designed to reduce wrist strain, featuring dual Bluetooth/2.4GHz connectivity, 4000 DPI sensor, and silent tactile clicks.', 34.50, 80, 'Electronics', 'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=600', CURRENT_TIMESTAMP),
(3, 2, 'AcousticPure Active Noise Cancelling Headphones', 'Premium over-ear wireless headphones with hybrid active noise cancellation, high-resolution 40mm dynamic drivers, and 35-hour continuous playback battery life.', 179.00, 25, 'Electronics', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600', CURRENT_TIMESTAMP),
(4, 2, 'UltraClear 27-inch 4K UHD Monitor', '27-inch IPS UHD (3840x2160) desktop display with HDR400, 99% sRGB color gamut coverage, and single-cable 65W USB-C power delivery.', 329.99, 15, 'Electronics', 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=600', CURRENT_TIMESTAMP),

-- Fashion (Seller ID: 3)
(5, 3, 'Organic Ring-Spun Cotton T-Shirt', 'Eco-friendly, lightweight 100% certified organic cotton crewneck tee. Soft combed yarn with double-stitched hem for premium everyday comfort.', 24.00, 120, 'Fashion', 'https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=600', CURRENT_TIMESTAMP),
(6, 3, 'Handcrafted Full-Grain Leather Wallet', 'Slim bifold pocket wallet made of genuine full-grain vegetable-tanned cowhide with RFID-blocking lining, 8 card slots, and dual currency compartments.', 45.00, 60, 'Fashion', 'https://images.unsplash.com/photo-1627123424574-724758594e93?w=600', CURRENT_TIMESTAMP),
(7, 3, 'Polarized Aviator Sunglasses', 'Classic lightweight stainless-steel wire frame sunglasses with UV400 polarized scratch-resistant gradient lenses and adjustable silicone nose pads.', 55.00, 40, 'Fashion', 'https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=600', CURRENT_TIMESTAMP),
(8, 3, 'Waxed Canvas Utility Travel Backpack', 'Water-resistant 20L heavy-duty waxed canvas daypack with padded 15.6-inch laptop compartment, antique brass hardware, and ergonomic shoulder straps.', 85.00, 30, 'Fashion', 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600', CURRENT_TIMESTAMP),

-- Home & Living (Seller ID: 3)
(9, 3, 'Ceramic Artisan Pour-Over Dripper Set', 'Hand-glazed ceramic coffee dripper cone with heat-resistant borosilicate glass server (600ml) and natural unbleached spiral paper filters.', 38.50, 50, 'Home & Living', 'https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=600', CURRENT_TIMESTAMP),
(10, 3, 'Minimalist Dimmable LED Desk Lamp', 'Sleek anodized aluminum desk reading lamp with touch-sensitive multi-temperature brightness controls, flexible goose-neck, and integrated USB-A charging port.', 49.99, 35, 'Home & Living', 'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=600', CURRENT_TIMESTAMP),

-- Books (Seller ID: 2)
(11, 2, 'Clean Code Architecture in Java', 'Comprehensive guide to software craftsmanship, SOLID design patterns, enterprise layered web application architectures, and robust unit testing in Java.', 42.00, 70, 'Books', 'https://images.unsplash.com/photo-1532012164546-f432f2e3777a?w=600', CURRENT_TIMESTAMP),
(12, 2, 'Designing Scalable Distributed Systems', 'Essential principles for data engineering, database reliability, event streams, microservices communication, and cloud infrastructure patterns.', 54.90, 40, 'Books', 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600', CURRENT_TIMESTAMP);

-- 3. SEED ORDERS
-- Order 1: Placed by Buyer 4, Delivered
INSERT INTO orders (id, buyer_id, status, total_amount, created_at) VALUES
(1, 4, 'DELIVERED', 124.49, TIMESTAMPADD('DAY', -7, CURRENT_TIMESTAMP)),
(2, 4, 'SHIPPED', 179.00, TIMESTAMPADD('DAY', -2, CURRENT_TIMESTAMP)),
(3, 5, 'CONFIRMED', 123.50, TIMESTAMPADD('DAY', -1, CURRENT_TIMESTAMP)),
(4, 5, 'PENDING', 329.99, CURRENT_TIMESTAMP);

-- 4. SEED ORDER ITEMS
-- Items for Order 1
INSERT INTO order_items (id, order_id, product_id, quantity, unit_price, created_at) VALUES
(1, 1, 1, 1, 89.99, TIMESTAMPADD('DAY', -7, CURRENT_TIMESTAMP)),
(2, 1, 2, 1, 34.50, TIMESTAMPADD('DAY', -7, CURRENT_TIMESTAMP));

-- Items for Order 2
INSERT INTO order_items (id, order_id, product_id, quantity, unit_price, created_at) VALUES
(3, 2, 3, 1, 179.00, TIMESTAMPADD('DAY', -2, CURRENT_TIMESTAMP));

-- Items for Order 3
INSERT INTO order_items (id, order_id, product_id, quantity, unit_price, created_at) VALUES
(4, 3, 6, 1, 45.00, TIMESTAMPADD('DAY', -1, CURRENT_TIMESTAMP)),
(5, 3, 9, 1, 38.50, TIMESTAMPADD('DAY', -1, CURRENT_TIMESTAMP)),
(6, 3, 11, 1, 40.00, TIMESTAMPADD('DAY', -1, CURRENT_TIMESTAMP));

-- Items for Order 4
INSERT INTO order_items (id, order_id, product_id, quantity, unit_price, created_at) VALUES
(7, 4, 4, 1, 329.99, CURRENT_TIMESTAMP);

-- 5. SEED CART ITEMS (Active shopping carts for demonstration)
INSERT INTO cart_items (id, user_id, product_id, quantity, created_at) VALUES
(1, 4, 5, 2, CURRENT_TIMESTAMP),
(2, 4, 10, 1, CURRENT_TIMESTAMP),
(3, 5, 1, 1, CURRENT_TIMESTAMP);

-- 6. SEED PRODUCT REVIEWS & RATINGS (Ratings 1 to 5)
INSERT INTO reviews (id, product_id, user_id, rating, comment, created_at) VALUES
(1, 1, 4, 5, 'Outstanding tactile switches! Excellent build quality and responsive actuation for programming sessions.', TIMESTAMPADD('DAY', -5, CURRENT_TIMESTAMP)),
(2, 2, 4, 4, 'Very comfortable grip that drastically lessened my wrist pain during extended shifts. Silent clicks are fantastic.', TIMESTAMPADD('DAY', -4, CURRENT_TIMESTAMP)),
(3, 3, 5, 5, 'Noise cancellation is astonishingly good in noisy train commutes. Battery holds up beyond advertised expectations.', TIMESTAMPADD('DAY', -1, CURRENT_TIMESTAMP));
