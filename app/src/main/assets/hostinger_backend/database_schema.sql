-- ==========================================================
-- GreensStock SaaS - Hostinger MySQL Database Schema
-- Multi-Tenant POS & ERP System
-- Supports Full Bengali UTF-8 (utf8mb4_unicode_ci)
-- ==========================================================

CREATE TABLE IF NOT EXISTS `saas_tenants` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(64) UNIQUE NOT NULL,
    `store_name` VARCHAR(255) NOT NULL,
    `bangla_store_name` VARCHAR(255) DEFAULT '',
    `api_key` VARCHAR(128) NOT NULL,
    `subscription_plan` ENUM('TRIAL', 'BASIC', 'PRO', 'ENTERPRISE') DEFAULT 'PRO',
    `subscription_status` ENUM('ACTIVE', 'EXPIRED', 'SUSPENDED') DEFAULT 'ACTIVE',
    `max_outlets` INT DEFAULT 5,
    `expiry_date` DATETIME NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `saas_outlets` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(64) NOT NULL,
    `outlet_code` VARCHAR(64) NOT NULL,
    `outlet_name` VARCHAR(255) NOT NULL,
    `phone` VARCHAR(64) DEFAULT '',
    `address` TEXT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_tenant_outlet` (`tenant_id`, `outlet_code`),
    FOREIGN KEY (`tenant_id`) REFERENCES `saas_tenants`(`tenant_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `products` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(64) NOT NULL,
    `local_id` BIGINT NOT NULL,
    `barcode` VARCHAR(64) NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `bangla_name` VARCHAR(255) DEFAULT '',
    `category` VARCHAR(128) NOT NULL,
    `buying_price` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `selling_price` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `wholesale_price` DECIMAL(12,2) DEFAULT 0.00,
    `stock_quantity` INT NOT NULL DEFAULT 0,
    `min_stock_level` INT NOT NULL DEFAULT 5,
    `unit` VARCHAR(32) DEFAULT 'pcs',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_tenant_product` (`tenant_id`, `barcode`),
    INDEX `idx_tenant_cat` (`tenant_id`, `category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `sales` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(64) NOT NULL,
    `invoice_number` VARCHAR(64) NOT NULL,
    `timestamp` BIGINT NOT NULL,
    `customer_name` VARCHAR(255) DEFAULT 'Walk-in Customer',
    `subtotal` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `discount_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `vat_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `net_payable` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `paid_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `due_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `payment_method` VARCHAR(32) NOT NULL DEFAULT 'CASH',
    `cashier_role` VARCHAR(32) DEFAULT 'ADMIN',
    `status` VARCHAR(32) DEFAULT 'COMPLETED',
    `notes` TEXT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_tenant_invoice` (`tenant_id`, `invoice_number`),
    INDEX `idx_tenant_time` (`tenant_id`, `timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `sale_items` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(64) NOT NULL,
    `invoice_number` VARCHAR(64) NOT NULL,
    `product_id` BIGINT NOT NULL,
    `product_name` VARCHAR(255) NOT NULL,
    `quantity` INT NOT NULL DEFAULT 1,
    `cost_price` DECIMAL(12,2) NOT NULL,
    `unit_price` DECIMAL(12,2) NOT NULL,
    `subtotal` DECIMAL(12,2) NOT NULL,
    `profit_or_loss` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    INDEX `idx_tenant_inv` (`tenant_id`, `invoice_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `expenses` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(64) NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `category` VARCHAR(64) NOT NULL,
    `amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `timestamp` BIGINT NOT NULL,
    `payment_method` VARCHAR(32) DEFAULT 'CASH',
    `notes` TEXT,
    INDEX `idx_tenant_exp` (`tenant_id`, `timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `customers` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(64) NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `bangla_name` VARCHAR(255) DEFAULT '',
    `phone` VARCHAR(64) NOT NULL,
    `address` TEXT,
    `total_spent` DECIMAL(12,2) DEFAULT 0.00,
    `outstanding_due` DECIMAL(12,2) DEFAULT 0.00,
    UNIQUE KEY `uk_tenant_cust` (`tenant_id`, `phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `saas_sync_logs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(64) NOT NULL,
    `device_id` VARCHAR(128) DEFAULT '',
    `synced_sales` INT DEFAULT 0,
    `synced_products` INT DEFAULT 0,
    `sync_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Sample Seed Tenant for Testing
INSERT INTO `saas_tenants` (`tenant_id`, `store_name`, `bangla_store_name`, `api_key`, `subscription_plan`, `subscription_status`, `expiry_date`)
VALUES ('GS-STORE-01', 'GreensStock Supermart', 'গ্রিনসস্টক সুপারমার্ট', 'gs_hostinger_secret_key', 'ENTERPRISE', 'ACTIVE', '2027-12-31 23:59:59')
ON DUPLICATE KEY UPDATE `store_name` = VALUES(`store_name`);
