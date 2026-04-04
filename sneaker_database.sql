-- ============================================
-- Database: sneaker
-- ============================================

CREATE DATABASE IF NOT EXISTS sneaker CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sneaker;

-- ============================================
-- Table: roles
-- ============================================
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(255)
);

-- ============================================
-- Table: users
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone VARCHAR(255),
    email VARCHAR(255),
    fullname VARCHAR(255),
    password VARCHAR(255),
    token TEXT,
    address VARCHAR(500),
    created_at DATETIME,
    active INT,
    verify_code VARCHAR(255),
    create_code_at TIMESTAMP NULL,
    role_id BIGINT,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- ============================================
-- Table: brand
-- ============================================
CREATE TABLE IF NOT EXISTS brand (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255)
);

-- ============================================
-- Table: category
-- ============================================
CREATE TABLE IF NOT EXISTS category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cate_name VARCHAR(255)
);

-- ============================================
-- Table: product
-- ============================================
CREATE TABLE IF NOT EXISTS product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    price BIGINT,
    description TEXT,
    gender VARCHAR(50),
    image TEXT,
    created_at DATETIME,
    category_id BIGINT,
    brand_id BIGINT,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category(id),
    CONSTRAINT fk_product_brand FOREIGN KEY (brand_id) REFERENCES brand(id)
);

-- ============================================
-- Table: product_size
-- ============================================
CREATE TABLE IF NOT EXISTS product_size (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    size INT,
    stock INT,
    product_id BIGINT,
    CONSTRAINT fk_product_size_product FOREIGN KEY (product_id) REFERENCES product(id)
);

-- ============================================
-- Table: cart
-- ============================================
CREATE TABLE IF NOT EXISTS cart (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME,
    total_amout BIGINT,
    user_id BIGINT UNIQUE,
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ============================================
-- Table: cart_item
-- ============================================
CREATE TABLE IF NOT EXISTS cart_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quantity INT,
    price BIGINT,
    size INT,
    created_at DATETIME,
    cart_id BIGINT,
    product_id BIGINT,
    CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES cart(id),
    CONSTRAINT fk_cart_item_product FOREIGN KEY (product_id) REFERENCES product(id)
);

-- ============================================
-- Table: orders
-- ============================================
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    status INT,
    total_amount BIGINT,
    full_name VARCHAR(255),
    phone_number VARCHAR(50),
    email VARCHAR(255),
    shipping_method VARCHAR(255),
    created_at DATETIME,
    address VARCHAR(500),
    delivery_time VARCHAR(255),
    note TEXT,
    user_id BIGINT,
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ============================================
-- Table: order_item (composite PK: order_id + product_id)
-- ============================================
CREATE TABLE IF NOT EXISTS order_item (
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT,
    price BIGINT,
    size INT,
    created_at DATETIME,
    PRIMARY KEY (order_id, product_id),
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES product(id)
);

-- ============================================
-- Table: contact
-- ============================================
CREATE TABLE IF NOT EXISTS contact (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME
);

-- ============================================
-- Table: questions
-- ============================================
CREATE TABLE IF NOT EXISTS questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question TEXT NOT NULL,
    answer TEXT NOT NULL
);

-- ============================================
-- Liquibase tracking tables (required by Spring Boot Liquibase)
-- ============================================
CREATE TABLE IF NOT EXISTS DATABASECHANGELOG (
    ID VARCHAR(255) NOT NULL,
    AUTHOR VARCHAR(255) NOT NULL,
    FILENAME VARCHAR(255) NOT NULL,
    DATEEXECUTED DATETIME NOT NULL,
    ORDEREXECUTED INT NOT NULL,
    EXECTYPE VARCHAR(10) NOT NULL,
    MD5SUM VARCHAR(35),
    DESCRIPTION VARCHAR(255),
    COMMENTS VARCHAR(255),
    TAG VARCHAR(255),
    LIQUIBASE VARCHAR(20),
    CONTEXTS VARCHAR(255),
    LABELS VARCHAR(255),
    DEPLOYMENT_ID VARCHAR(10)
);

CREATE TABLE IF NOT EXISTS DATABASECHANGELOGLOCK (
    ID INT NOT NULL PRIMARY KEY,
    LOCKED TINYINT(1) NOT NULL,
    LOCKGRANTED DATETIME,
    LOCKEDBY VARCHAR(255)
);

-- ============================================
-- Seed data: roles
-- ============================================
INSERT INTO roles (role_name) VALUES ('ADMIN'), ('USER');

-- ============================================
-- Seed data: admin user (password: 123456 - BCrypt)
-- ============================================
INSERT INTO users (phone, email, fullname, password, active, role_id, created_at)
VALUES (
    '0900000000',
    'admin@sneaker.com',
    'Admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    1,
    1,
    NOW()
);
