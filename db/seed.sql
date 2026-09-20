-- seed.sql
-- RohithMart Seed Data
-- Note: Default password for seeded users is 'Password@123'
-- BCrypt hash: $2a$10$wN9K84sZ2fR.p7iT4gWreOMJ7kO8vRkI/g9wD6oG8rF6eU2pLq46a

-- 1. Seed Users (Admin, Seller, Buyer)
INSERT INTO users (id, name, email, password_hash, role) VALUES
(1, 'System Administrator', 'admin@rohithmart.com', '$2a$10$wN9K84sZ2fR.p7iT4gWreOMJ7kO8vRkI/g9wD6oG8rF6eU2pLq46a', 'ADMIN'),
(2, 'Rohith Seller', 'seller@rohithmart.com', '$2a$10$wN9K84sZ2fR.p7iT4gWreOMJ7kO8vRkI/g9wD6oG8rF6eU2pLq46a', 'SELLER'),
(3, 'John Buyer', 'buyer@rohithmart.com', '$2a$10$wN9K84sZ2fR.p7iT4gWreOMJ7kO8vRkI/g9wD6oG8rF6eU2pLq46a', 'BUYER');

-- 2. Seed Products
INSERT INTO products (id, seller_id, name, description, price, stock_qty, category, image_url) VALUES
(1, 2, 'Mechanical Gaming Keyboard', 'RGB mechanical keyboard with tactile blue switches and aluminum frame.', 79.99, 50, 'Electronics', 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=500'),
(2, 2, 'Wireless Ergonomic Mouse', 'High precision wireless mouse with rechargeable battery and silent click.', 29.99, 100, 'Electronics', 'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=500'),
(3, 2, 'Noise-Cancelling Headphones', 'Over-ear wireless headphones with active noise cancellation and 30h battery.', 149.99, 30, 'Electronics', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500'),
(4, 2, 'Organic Cotton T-Shirt', 'Premium 100% organic ring-spun cotton everyday crewneck t-shirt.', 19.99, 75, 'Fashion', 'https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=500'),
(5, 2, 'Classic Leather Wallet', 'Handcrafted genuine leather bifold wallet with RFID protection.', 39.99, 45, 'Fashion', 'https://images.unsplash.com/photo-1627123424574-724758594e93?w=500');

-- 3. Seed Orders & Order Items
INSERT INTO orders (id, buyer_id, status, total_amount) VALUES
(1, 3, 'DELIVERED', 109.98);

INSERT INTO order_items (id, order_id, product_id, quantity, unit_price) VALUES
(1, 1, 1, 1, 79.99),
(2, 1, 2, 1, 29.99);

-- 4. Seed Reviews
INSERT INTO reviews (id, product_id, user_id, rating, comment) VALUES
(1, 1, 3, 5, 'Exceptional tactile feedback and sturdy build quality. Highly recommended!'),
(2, 2, 3, 4, 'Very comfortable grip for long hours of coding.');

-- 5. Seed Cart Items
INSERT INTO cart_items (id, user_id, product_id, quantity) VALUES
(1, 3, 3, 1);
