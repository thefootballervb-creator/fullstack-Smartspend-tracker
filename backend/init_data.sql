-- Initialize Categories and Admin User for Expense Tracker
-- Database: expenses_tracker

-- Note: Make sure the backend has started at least once to create tables
-- Note: TransactionType and Role tables are auto-seeded by the application

USE expenses_tracker;

-- Insert Expense Categories (transaction_type_id = 1 for TYPE_EXPENSE)
INSERT INTO category (category_name, transaction_type_id, enabled) VALUES
('Food', 1, true),
('Leisure', 1, true),
('Household', 1, true),
('Clothing', 1, true),
('Education', 1, true),
('Healthcare', 1, true),
('Transport', 1, true),
('Utilities', 1, true),
('Entertainment', 1, true),
('Other', 1, true)
ON DUPLICATE KEY UPDATE category_name = category_name;

-- Insert Income Categories (transaction_type_id = 2 for TYPE_INCOME)
INSERT INTO category (category_name, transaction_type_id, enabled) VALUES
('Salary', 2, true),
('Sales', 2, true),
('Interest', 2, true),
('Awards', 2, true),
('Bonus', 2, true),
('Freelance', 2, true),
('Investment', 2, true),
('Rental', 2, true),
('Gift', 2, true),
('Other', 2, true)
ON DUPLICATE KEY UPDATE category_name = category_name;

-- Insert Admin User
-- Email: admin@gmail.com
-- Password: admin123 (BCrypt hash)
-- Note: This BCrypt hash is for password "admin123"
INSERT INTO users (username, email, password, enabled, verification_code, verification_code_expiry_time) VALUES
('admin', 'admin@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pJLu', true, NULL, NULL)
ON DUPLICATE KEY UPDATE username = username;

-- Link Admin User to ROLE_ADMIN
-- Note: role_id = 2 is typically ROLE_ADMIN (role_id = 1 is ROLE_USER)
-- This assumes roles table has been seeded by the application
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u, roles r
WHERE u.email = 'admin@gmail.com' AND r.name = 'ROLE_ADMIN'
ON DUPLICATE KEY UPDATE user_id = user_id;

-- Verify inserted data
SELECT 'Categories inserted:' as Status;
SELECT category_id, category_name, transaction_type_id, enabled FROM category ORDER BY transaction_type_id, category_name;

SELECT 'Admin user inserted:' as Status;
SELECT id, username, email, enabled FROM users WHERE email = 'admin@gmail.com';

SELECT 'Admin roles assigned:' as Status;
SELECT u.username, r.name as role FROM users u 
JOIN user_roles ur ON u.id = ur.user_id 
JOIN roles r ON ur.role_id = r.id 
WHERE u.email = 'admin@gmail.com';
