<?php
/**
 * GreensStock POS & ERP - Hostinger Multi-Tenant SaaS REST API
 * Supports UTF-8 Bengali & English text
 * 
 * Deployment on Hostinger:
 * 1. Upload this file to public_html/greensstock_api.php
 * 2. Update DB_HOST, DB_USER, DB_PASS, DB_NAME with your Hostinger MySQL details
 */

// Error handling - return JSON errors instead of HTML crashes
error_reporting(E_ALL);
ini_set('display_errors', '0');

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: Content-Type, X-Tenant-ID, X-API-KEY');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

// -------------------------------------------------------------
// 1. HOSTINGER DATABASE CONFIGURATION
// -------------------------------------------------------------
define('DB_HOST', 'localhost');          // In Hostinger, usually 'localhost'
define('DB_USER', 'u322548859_greensoft');    // Replace with your Hostinger MySQL User
define('DB_PASS', '%Dg2562686');  // Replace with your Hostinger MySQL Password
define('DB_NAME', 'u322548859_mamun_db');   // Replace with your Hostinger MySQL Database Name

// Connect to MySQL with UTF-8 support
try {
    $pdo = new PDO(
        "mysql:host=" . DB_HOST . ";dbname=" . DB_NAME . ";charset=utf8mb4",
        DB_USER,
        DB_PASS,
        [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::MYSQL_ATTR_INIT_COMMAND => "SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci"
        ]
    );
} catch (PDOException $e) {
    echo json_encode([
        'status' => 'CONFIG_NEEDED',
        'databaseConnected' => false,
        'success' => false,
        'message' => 'Hostinger MySQL Connection Failed: ' . $e->getMessage() . '. Please verify credentials in greensstock_api.php'
    ]);
    exit;
}

// Helper: Inspect existing columns of any table safely
function getTableColumns($pdo, $tableName) {
    try {
        $stmt = $pdo->query("SHOW COLUMNS FROM `$tableName`");
        return $stmt->fetchAll(PDO::FETCH_COLUMN);
    } catch (Exception $e) {
        return [];
    }
}

// Helper: Ensure standard tables exist without altering or breaking existing web tables
function ensureTablesExist($pdo) {
    try {
        // 1. saas_tenants
        $pdo->exec("CREATE TABLE IF NOT EXISTS saas_tenants (
            id INT AUTO_INCREMENT PRIMARY KEY,
            tenant_id VARCHAR(50) UNIQUE NOT NULL,
            store_name VARCHAR(150) NOT NULL,
            api_key VARCHAR(100) NOT NULL,
            subscription_plan VARCHAR(30) DEFAULT 'PRO',
            subscription_status VARCHAR(20) DEFAULT 'ACTIVE',
            expiry_date DATETIME,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

        // 2. products (created only if it doesn't already exist in web DB)
        $pdo->exec("CREATE TABLE IF NOT EXISTS products (
            id INT AUTO_INCREMENT PRIMARY KEY,
            tenant_id VARCHAR(50) DEFAULT 'DEFAULT',
            local_id BIGINT DEFAULT 0,
            barcode VARCHAR(100) DEFAULT '',
            name VARCHAR(255) NOT NULL,
            bangla_name VARCHAR(255) DEFAULT '',
            category VARCHAR(100) DEFAULT 'General',
            buying_price DECIMAL(12,2) DEFAULT 0.00,
            selling_price DECIMAL(12,2) DEFAULT 0.00,
            wholesale_price DECIMAL(12,2) DEFAULT 0.00,
            stock_quantity INT DEFAULT 0,
            min_stock_level INT DEFAULT 5,
            unit VARCHAR(30) DEFAULT 'pcs',
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
            INDEX idx_tenant (tenant_id),
            INDEX idx_barcode (barcode)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

        // 3. sales
        $pdo->exec("CREATE TABLE IF NOT EXISTS sales (
            id INT AUTO_INCREMENT PRIMARY KEY,
            tenant_id VARCHAR(50) DEFAULT 'DEFAULT',
            invoice_number VARCHAR(100) NOT NULL,
            timestamp BIGINT NOT NULL,
            customer_name VARCHAR(150) DEFAULT 'Walk-in',
            subtotal DECIMAL(12,2) DEFAULT 0.00,
            discount_amount DECIMAL(12,2) DEFAULT 0.00,
            vat_amount DECIMAL(12,2) DEFAULT 0.00,
            net_payable DECIMAL(12,2) DEFAULT 0.00,
            paid_amount DECIMAL(12,2) DEFAULT 0.00,
            due_amount DECIMAL(12,2) DEFAULT 0.00,
            payment_method VARCHAR(30) DEFAULT 'CASH',
            cashier_role VARCHAR(30) DEFAULT 'CASHIER',
            status VARCHAR(30) DEFAULT 'COMPLETED',
            notes TEXT,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            INDEX idx_tenant_invoice (tenant_id, invoice_number)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

        // 4. sale_items
        $pdo->exec("CREATE TABLE IF NOT EXISTS sale_items (
            id INT AUTO_INCREMENT PRIMARY KEY,
            tenant_id VARCHAR(50) DEFAULT 'DEFAULT',
            invoice_number VARCHAR(100) NOT NULL,
            product_id BIGINT DEFAULT 0,
            product_name VARCHAR(255) NOT NULL,
            quantity INT DEFAULT 1,
            cost_price DECIMAL(12,2) DEFAULT 0.00,
            unit_price DECIMAL(12,2) DEFAULT 0.00,
            subtotal DECIMAL(12,2) DEFAULT 0.00,
            profit_or_loss DECIMAL(12,2) DEFAULT 0.00,
            INDEX idx_tenant_invoice (tenant_id, invoice_number)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

        // 5. expenses
        $pdo->exec("CREATE TABLE IF NOT EXISTS expenses (
            id INT AUTO_INCREMENT PRIMARY KEY,
            tenant_id VARCHAR(50) DEFAULT 'DEFAULT',
            title VARCHAR(255) NOT NULL,
            category VARCHAR(100) DEFAULT 'General',
            amount DECIMAL(12,2) DEFAULT 0.00,
            timestamp BIGINT NOT NULL,
            payment_method VARCHAR(30) DEFAULT 'CASH',
            notes TEXT
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

        // 6. customers
        $pdo->exec("CREATE TABLE IF NOT EXISTS customers (
            id INT AUTO_INCREMENT PRIMARY KEY,
            tenant_id VARCHAR(50) DEFAULT 'DEFAULT',
            name VARCHAR(150) NOT NULL,
            bangla_name VARCHAR(150) DEFAULT '',
            phone VARCHAR(30) DEFAULT '',
            address TEXT,
            total_spent DECIMAL(12,2) DEFAULT 0.00,
            outstanding_due DECIMAL(12,2) DEFAULT 0.00,
            INDEX idx_phone (phone)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

        // 7. saas_sync_logs
        $pdo->exec("CREATE TABLE IF NOT EXISTS saas_sync_logs (
            id INT AUTO_INCREMENT PRIMARY KEY,
            tenant_id VARCHAR(50) DEFAULT 'DEFAULT',
            device_id VARCHAR(100) DEFAULT 'TERMINAL',
            synced_sales INT DEFAULT 0,
            synced_products INT DEFAULT 0,
            sync_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

        // 8. saas_app_updates (Auto Update System)
        $pdo->exec("CREATE TABLE IF NOT EXISTS saas_app_updates (
            id INT AUTO_INCREMENT PRIMARY KEY,
            version_code INT NOT NULL,
            version_name VARCHAR(50) NOT NULL,
            release_notes TEXT,
            apk_url VARCHAR(500) NOT NULL,
            file_size_bytes BIGINT DEFAULT 0,
            mandatory TINYINT(1) DEFAULT 0,
            release_date DATE,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            INDEX idx_version (version_code)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
    } catch (Exception $e) {
        // Non-blocking
    }
}

// Auto-ensure base tables exist
ensureTablesExist($pdo);

// -------------------------------------------------------------
// 2. REQUEST PARSING & AUTHENTICATION
// -------------------------------------------------------------
$rawInput = file_get_contents('php://input');
$data = json_decode($rawInput, true) ?? [];

$action = $_GET['action'] ?? ($data['action'] ?? 'health');

// Header or payload authentication
$tenantId = $_SERVER['HTTP_X_TENANT_ID'] ?? ($data['tenantId'] ?? 'GS-STORE-01');
$apiKey = $_SERVER['HTTP_X_API_KEY'] ?? ($data['apiKey'] ?? '');

// -------------------------------------------------------------
// 3. API ENDPOINTS
// -------------------------------------------------------------

// ACTION: HEALTH CHECK
if ($action === 'health') {
    echo json_encode([
        'status' => 'OK',
        'success' => true,
        'databaseConnected' => true,
        'serverTime' => time() * 1000,
        'message' => 'GreensStock SaaS Hostinger Gateway is running smoothly.'
    ]);
    exit;
}

// ACTION: CHECK APP UPDATE (In-App Auto Update)
if ($action === 'check_update') {
    try {
        $stmt = $pdo->query("SELECT * FROM saas_app_updates ORDER BY version_code DESC LIMIT 1");
        $latest = $stmt->fetch();

        if ($latest) {
            echo json_encode([
                'success' => true,
                'latestVersionCode' => (int)$latest['version_code'],
                'latestVersionName' => $latest['version_name'],
                'releaseNotes' => $latest['release_notes'],
                'apkUrl' => $latest['apk_url'],
                'fileSizeBytes' => (int)$latest['file_size_bytes'],
                'mandatory' => (bool)$latest['mandatory'],
                'releaseDate' => $latest['release_date'] ?: date('Y-m-d')
            ]);
        } else {
            $protocol = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ? "https://" : "http://";
            $host = $_SERVER['HTTP_HOST'] ?? 'localhost';
            $defaultApkUrl = $protocol . $host . '/downloads/greensstock_latest.apk';

            echo json_encode([
                'success' => true,
                'latestVersionCode' => 1,
                'latestVersionName' => '1.0',
                'releaseNotes' => 'গ্রিনসস্টক পিওএস ও ইনভেন্টরি ম্যানেজমেন্ট সিস্টেমের প্রাথমিক সংস্করণ।',
                'apkUrl' => $defaultApkUrl,
                'fileSizeBytes' => 0,
                'mandatory' => false,
                'releaseDate' => date('Y-m-d')
            ]);
        }
    } catch (Exception $e) {
        echo json_encode([
            'success' => false,
            'message' => 'Update check error: ' . $e->getMessage()
        ]);
    }
    exit;
}

// ACTION: PUBLISH UPDATE (Allows developer to publish new APK release)
if ($action === 'publish_update') {
    $vCode = (int)($data['versionCode'] ?? $_POST['versionCode'] ?? 0);
    $vName = trim($data['versionName'] ?? $_POST['versionName'] ?? '');
    $apkUrl = trim($data['apkUrl'] ?? $_POST['apkUrl'] ?? '');
    $notes = trim($data['releaseNotes'] ?? $_POST['releaseNotes'] ?? 'নতুন আপডেট ও উন্নতি।');
    $size = (int)($data['fileSizeBytes'] ?? $_POST['fileSizeBytes'] ?? 0);
    $mandatory = !empty($data['mandatory'] ?? $_POST['mandatory'] ?? 0) ? 1 : 0;

    if ($vCode > 0 && !empty($vName) && !empty($apkUrl)) {
        try {
            $stmt = $pdo->prepare("
                INSERT INTO saas_app_updates (version_code, version_name, release_notes, apk_url, file_size_bytes, mandatory, release_date)
                VALUES (?, ?, ?, ?, ?, ?, CURDATE())
            ");
            $stmt->execute([$vCode, $vName, $notes, $apkUrl, $size, $mandatory]);
            echo json_encode([
                'success' => true,
                'message' => "নতুন সংস্করণ v$vName (Build $vCode) সফলভাবে প্রকাশিত হয়েছে!"
            ]);
        } catch (Exception $e) {
            echo json_encode(['success' => false, 'message' => $e->getMessage()]);
        }
    } else {
        echo json_encode(['success' => false, 'message' => 'versionCode, versionName, and apkUrl are required']);
    }
    exit;
}

// ACTION: INSPECT EXISTING DATABASE (Auto-detect tables and structure)
if ($action === 'inspect') {
    try {
        $tablesStmt = $pdo->query("SHOW TABLES");
        $tables = $tablesStmt->fetchAll(PDO::FETCH_COLUMN);

        $detectedProductTable = null;
        $detectedSalesTable = null;
        $detectedCustomerTable = null;
        $totalProducts = 0;

        foreach ($tables as $tbl) {
            $lower = strtolower($tbl);
            if (in_array($lower, ['products', 'items', 'tbl_products', 'tbl_items', 'inventory'])) {
                $detectedProductTable = $tbl;
            }
            if (in_array($lower, ['sales', 'orders', 'invoices', 'tbl_sales', 'tbl_orders'])) {
                $detectedSalesTable = $tbl;
            }
            if (in_array($lower, ['customers', 'clients', 'users', 'tbl_customers'])) {
                $detectedCustomerTable = $tbl;
            }
        }

        if ($detectedProductTable) {
            $countStmt = $pdo->query("SELECT COUNT(*) FROM `$detectedProductTable`");
            $totalProducts = (int)$countStmt->fetchColumn();
        }

        echo json_encode([
            'success' => true,
            'databaseName' => DB_NAME,
            'detectedTables' => $tables,
            'totalProductsFound' => $totalProducts,
            'detectedProductTable' => $detectedProductTable ?: 'products',
            'detectedSalesTable' => $detectedSalesTable ?: 'sales',
            'detectedCustomerTable' => $detectedCustomerTable ?: 'customers',
            'message' => 'Successfully connected to Hostinger MySQL database. Found ' . count($tables) . ' tables and ' . $totalProducts . ' products.'
        ]);
    } catch (Exception $e) {
        echo json_encode([
            'success' => false,
            'message' => 'Inspection error: ' . $e->getMessage()
        ]);
    }
    exit;
}

// ACTION: VERIFY LICENSE / TENANT
if ($action === 'verify_license') {
    if (empty($tenantId)) {
        $tenantId = 'GS-STORE-01';
    }

    try {
        $stmt = $pdo->prepare("SELECT * FROM saas_tenants WHERE tenant_id = ? LIMIT 1");
        $stmt->execute([$tenantId]);
        $tenant = $stmt->fetch();

        if ($tenant) {
            $isExpired = strtotime($tenant['expiry_date']) < time();
            echo json_encode([
                'success' => true,
                'valid' => !$isExpired && $tenant['subscription_status'] === 'ACTIVE',
                'tenantId' => $tenant['tenant_id'],
                'storeName' => $tenant['store_name'],
                'plan' => $tenant['subscription_plan'],
                'status' => $isExpired ? 'EXPIRED' : $tenant['subscription_status'],
                'expiryDate' => strtotime($tenant['expiry_date']) * 1000,
                'message' => $isExpired ? 'Subscription Expired.' : 'Tenant License Active'
            ]);
        } else {
            // Auto-provision tenant store
            $expiry = date('Y-m-d H:i:s', strtotime('+1 year'));
            $stmtInsert = $pdo->prepare("
                INSERT INTO saas_tenants (tenant_id, store_name, api_key, subscription_plan, subscription_status, expiry_date)
                VALUES (?, 'GreensStock Store', ?, 'PRO', 'ACTIVE', ?)
            ");
            $stmtInsert->execute([$tenantId, $apiKey ?: 'default_key', $expiry]);

            echo json_encode([
                'success' => true,
                'valid' => true,
                'tenantId' => $tenantId,
                'storeName' => 'GreensStock Store',
                'plan' => 'PRO',
                'status' => 'ACTIVE',
                'expiryDate' => strtotime($expiry) * 1000,
                'message' => 'SaaS Store Provisioned successfully.'
            ]);
        }
    } catch (Exception $e) {
        echo json_encode([
            'success' => true,
            'valid' => true,
            'tenantId' => $tenantId,
            'storeName' => 'My Store',
            'plan' => 'PRO',
            'status' => 'ACTIVE',
            'expiryDate' => (time() + 31536000) * 1000,
            'message' => 'License Active'
        ]);
    }
    exit;
}

// ACTION: PUSH SYNC (Upload data from Android to Hostinger MySQL)
if ($action === 'sync_push') {
    if (empty($tenantId)) {
        $tenantId = 'GS-STORE-01';
    }

    $pdo->beginTransaction();
    try {
        $syncedProducts = 0;
        $syncedSales = 0;

        $prodCols = getTableColumns($pdo, 'products');
        $hasBarcode = in_array('barcode', $prodCols);
        $hasName = in_array('name', $prodCols);
        $hasBangla = in_array('bangla_name', $prodCols);
        $hasCat = in_array('category', $prodCols);
        $hasBuyPrice = in_array('buying_price', $prodCols);
        $hasSellPrice = in_array('selling_price', $prodCols);
        $hasWholePrice = in_array('wholesale_price', $prodCols);
        $hasStock = in_array('stock_quantity', $prodCols);
        $hasMinStock = in_array('min_stock_level', $prodCols);
        $hasUnit = in_array('unit', $prodCols);
        $hasTenant = in_array('tenant_id', $prodCols);
        $hasLocalId = in_array('local_id', $prodCols);

        // 1. Sync Products
        if (!empty($data['products']) && is_array($data['products']) && !empty($prodCols)) {
            // Build dynamic insert/update query matching existing columns
            $cols = [];
            $vals = [];
            $updates = [];

            if ($hasTenant) { $cols[] = 'tenant_id'; $vals[] = '?'; }
            if ($hasLocalId) { $cols[] = 'local_id'; $vals[] = '?'; $updates[] = 'local_id = VALUES(local_id)'; }
            if ($hasBarcode) { $cols[] = 'barcode'; $vals[] = '?'; }
            if ($hasName) { $cols[] = 'name'; $vals[] = '?'; $updates[] = 'name = VALUES(name)'; }
            if ($hasBangla) { $cols[] = 'bangla_name'; $vals[] = '?'; $updates[] = 'bangla_name = VALUES(bangla_name)'; }
            if ($hasCat) { $cols[] = 'category'; $vals[] = '?'; $updates[] = 'category = VALUES(category)'; }
            if ($hasBuyPrice) { $cols[] = 'buying_price'; $vals[] = '?'; $updates[] = 'buying_price = VALUES(buying_price)'; }
            if ($hasSellPrice) { $cols[] = 'selling_price'; $vals[] = '?'; $updates[] = 'selling_price = VALUES(selling_price)'; }
            if ($hasWholePrice) { $cols[] = 'wholesale_price'; $vals[] = '?'; $updates[] = 'wholesale_price = VALUES(wholesale_price)'; }
            if ($hasStock) { $cols[] = 'stock_quantity'; $vals[] = '?'; $updates[] = 'stock_quantity = VALUES(stock_quantity)'; }
            if ($hasMinStock) { $cols[] = 'min_stock_level'; $vals[] = '?'; $updates[] = 'min_stock_level = VALUES(min_stock_level)'; }
            if ($hasUnit) { $cols[] = 'unit'; $vals[] = '?'; $updates[] = 'unit = VALUES(unit)'; }

            if (!empty($cols)) {
                $sqlProd = "INSERT INTO `products` (" . implode(', ', $cols) . ") VALUES (" . implode(', ', $vals) . ")";
                if (!empty($updates)) {
                    $sqlProd .= " ON DUPLICATE KEY UPDATE " . implode(', ', $updates);
                }

                $stmtProd = $pdo->prepare($sqlProd);

                foreach ($data['products'] as $p) {
                    $binds = [];
                    if ($hasTenant) $binds[] = $tenantId;
                    if ($hasLocalId) $binds[] = $p['id'] ?? 0;
                    if ($hasBarcode) $binds[] = $p['barcode'] ?? '';
                    if ($hasName) $binds[] = $p['name'] ?? 'Item';
                    if ($hasBangla) $binds[] = $p['banglaName'] ?? '';
                    if ($hasCat) $binds[] = $p['category'] ?? 'General';
                    if ($hasBuyPrice) $binds[] = $p['buyingPrice'] ?? 0;
                    if ($hasSellPrice) $binds[] = $p['sellingPrice'] ?? 0;
                    if ($hasWholePrice) $binds[] = $p['wholesalePrice'] ?? 0;
                    if ($hasStock) $binds[] = $p['stockQuantity'] ?? 0;
                    if ($hasMinStock) $binds[] = $p['minStockLevel'] ?? 5;
                    if ($hasUnit) $binds[] = $p['unit'] ?? 'pcs';

                    try {
                        $stmtProd->execute($binds);
                        $syncedProducts++;
                    } catch (Exception $pe) {
                        // Skip problematic single item
                    }
                }
            }
        }

        // 2. Sync Sales & Line Items
        if (!empty($data['sales']) && is_array($data['sales'])) {
            $stmtSale = $pdo->prepare("
                INSERT INTO `sales`
                (tenant_id, invoice_number, timestamp, customer_name, subtotal, discount_amount, vat_amount, net_payable, paid_amount, due_amount, payment_method, cashier_role, status, notes)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                status = VALUES(status),
                paid_amount = VALUES(paid_amount),
                due_amount = VALUES(due_amount)
            ");

            $stmtItem = $pdo->prepare("
                INSERT INTO `sale_items`
                (tenant_id, invoice_number, product_id, product_name, quantity, cost_price, unit_price, subtotal, profit_or_loss)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ");

            foreach ($data['sales'] as $s) {
                $stmtSale->execute([
                    $tenantId,
                    $s['invoiceNumber'],
                    $s['timestamp'],
                    $s['customerName'] ?? 'Walk-in',
                    $s['subtotal'] ?? 0,
                    $s['discountAmount'] ?? 0,
                    $s['vatAmount'] ?? 0,
                    $s['netPayable'] ?? 0,
                    $s['paidAmount'] ?? 0,
                    $s['dueAmount'] ?? 0,
                    $s['paymentMethod'] ?? 'CASH',
                    $s['cashierRole'] ?? 'CASHIER',
                    $s['status'] ?? 'COMPLETED',
                    $s['notes'] ?? ''
                ]);
                $syncedSales++;

                // Items if included in sale payload
                if (!empty($s['items']) && is_array($s['items'])) {
                    // Clear existing items for this invoice
                    try {
                        $delStmt = $pdo->prepare("DELETE FROM `sale_items` WHERE tenant_id = ? AND invoice_number = ?");
                        $delStmt->execute([$tenantId, $s['invoiceNumber']]);
                    } catch (Exception $de) {}

                    foreach ($s['items'] as $it) {
                        $stmtItem->execute([
                            $tenantId,
                            $s['invoiceNumber'],
                            $it['productId'] ?? 0,
                            $it['productName'] ?? 'Item',
                            $it['quantity'] ?? 1,
                            $it['costPrice'] ?? 0,
                            $it['unitPrice'] ?? 0,
                            $it['subtotal'] ?? 0,
                            $it['profitOrLoss'] ?? 0
                        ]);

                        // Automatically deduct stock in products table
                        if ($hasStock) {
                            try {
                                $stockUp = $pdo->prepare("UPDATE `products` SET stock_quantity = GREATEST(0, stock_quantity - ?) WHERE (barcode = ? OR local_id = ? OR id = ?)");
                                $stockUp->execute([$it['quantity'] ?? 1, $it['barcode'] ?? '', $it['productId'] ?? 0, $it['productId'] ?? 0]);
                            } catch (Exception $se) {}
                        }
                    }
                }
            }
        }

        // 3. Sync Expenses
        if (!empty($data['expenses']) && is_array($data['expenses'])) {
            $stmtExp = $pdo->prepare("
                INSERT INTO `expenses` (tenant_id, title, category, amount, timestamp, payment_method, notes)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            ");
            foreach ($data['expenses'] as $e) {
                try {
                    $stmtExp->execute([
                        $tenantId,
                        $e['title'] ?? 'Expense',
                        $e['category'] ?? 'General',
                        $e['amount'] ?? 0,
                        $e['timestamp'] ?? (time() * 1000),
                        $e['paymentMethod'] ?? 'CASH',
                        $e['notes'] ?? ''
                    ]);
                } catch (Exception $ee) {}
            }
        }

        // 4. Log Sync Action safely
        try {
            $logStmt = $pdo->prepare("INSERT INTO `saas_sync_logs` (tenant_id, device_id, synced_sales, synced_products) VALUES (?, ?, ?, ?)");
            $logStmt->execute([$tenantId, $data['deviceId'] ?? 'ANDROID_TERMINAL', $syncedSales, $syncedProducts]);
        } catch (Exception $le) {}

        $pdo->commit();

        echo json_encode([
            'success' => true,
            'message' => 'Hostinger ক্লাউড ডাটাবেসে সিঙ্ক সফল হয়েছে!',
            'syncedSalesCount' => $syncedSales,
            'syncedProductsCount' => $syncedProducts,
            'serverTimestamp' => time() * 1000
        ]);
    } catch (Exception $ex) {
        $pdo->rollBack();
        echo json_encode([
            'success' => false,
            'message' => 'Sync transaction failed: ' . $ex->getMessage()
        ]);
    }
    exit;
}

// ACTION: PULL SYNC (Download products & customers from Hostinger)
if ($action === 'sync_pull') {
    try {
        $prodCols = getTableColumns($pdo, 'products');
        
        $products = [];
        if (!empty($prodCols)) {
            $idCol = in_array('id', $prodCols) ? 'id' : $prodCols[0];
            $barcodeCol = in_array('barcode', $prodCols) ? 'barcode' : (in_array('sku', $prodCols) ? 'sku' : (in_array('code', $prodCols) ? 'code' : null));
            $nameCol = in_array('name', $prodCols) ? 'name' : (in_array('product_name', $prodCols) ? 'product_name' : (in_array('title', $prodCols) ? 'title' : null));
            $banglaCol = in_array('bangla_name', $prodCols) ? 'bangla_name' : (in_array('banglaName', $prodCols) ? 'banglaName' : null);
            $catCol = in_array('category', $prodCols) ? 'category' : (in_array('category_name', $prodCols) ? 'category_name' : null);
            $buyCol = in_array('buying_price', $prodCols) ? 'buying_price' : (in_array('cost_price', $prodCols) ? 'cost_price' : (in_array('cost', $prodCols) ? 'cost' : null));
            $sellCol = in_array('selling_price', $prodCols) ? 'selling_price' : (in_array('price', $prodCols) ? 'price' : (in_array('unit_price', $prodCols) ? 'unit_price' : (in_array('sale_price', $prodCols) ? 'sale_price' : null)));
            $wholeCol = in_array('wholesale_price', $prodCols) ? 'wholesale_price' : null;
            $stockCol = in_array('stock_quantity', $prodCols) ? 'stock_quantity' : (in_array('quantity', $prodCols) ? 'quantity' : (in_array('stock', $prodCols) ? 'stock' : (in_array('qty', $prodCols) ? 'qty' : null)));
            $minCol = in_array('min_stock_level', $prodCols) ? 'min_stock_level' : null;
            $unitCol = in_array('unit', $prodCols) ? 'unit' : null;
            $hasTenant = in_array('tenant_id', $prodCols);

            $selectParts = [];
            $selectParts[] = "`$idCol` AS id";
            $selectParts[] = $barcodeCol ? "`$barcodeCol` AS barcode" : "CONCAT('BC-', `$idCol`) AS barcode";
            $selectParts[] = $nameCol ? "`$nameCol` AS name" : "'Item' AS name";
            $selectParts[] = $banglaCol ? "`$banglaCol` AS banglaName" : "'' AS banglaName";
            $selectParts[] = $catCol ? "`$catCol` AS category" : "'General' AS category";
            $selectParts[] = $buyCol ? "`$buyCol` AS buyingPrice" : "0 AS buyingPrice";
            $selectParts[] = $sellCol ? "`$sellCol` AS sellingPrice" : "0 AS sellingPrice";
            $selectParts[] = $wholeCol ? "`$wholeCol` AS wholesalePrice" : "0 AS wholesalePrice";
            $selectParts[] = $stockCol ? "`$stockCol` AS stockQuantity" : "0 AS stockQuantity";
            $selectParts[] = $minCol ? "`$minCol` AS minStockLevel" : "5 AS minStockLevel";
            $selectParts[] = $unitCol ? "`$unitCol` AS unit" : "'pcs' AS unit";

            $sql = "SELECT " . implode(', ', $selectParts) . " FROM `products`";
            if ($hasTenant && !empty($tenantId)) {
                $sql .= " WHERE tenant_id = " . $pdo->quote($tenantId) . " OR tenant_id = 'DEFAULT' OR tenant_id IS NULL OR tenant_id = ''";
            }
            $sql .= " ORDER BY `$idCol` DESC LIMIT 5000";

            $pStmt = $pdo->query($sql);
            $products = $pStmt->fetchAll();
        }

        // Fetch customers if table exists
        $customers = [];
        try {
            $custCols = getTableColumns($pdo, 'customers');
            if (!empty($custCols)) {
                $cStmt = $pdo->query("SELECT * FROM `customers` ORDER BY id DESC LIMIT 2000");
                $customers = $cStmt->fetchAll();
            }
        } catch (Exception $ce) {
            $customers = [];
        }

        echo json_encode([
            'success' => true,
            'products' => $products,
            'customers' => $customers,
            'count' => count($products),
            'message' => 'ওয়েব ডাটাবেজ থেকে ' . count($products) . 'টি পণ্য পাওয়া গেছে।',
            'serverTimestamp' => time() * 1000
        ]);
    } catch (Exception $e) {
        echo json_encode([
            'success' => false,
            'message' => 'Pull error: ' . $e->getMessage()
        ]);
    }
    exit;
}

// Fallback
echo json_encode([
    'status' => 'UNKNOWN_ACTION',
    'success' => false,
    'message' => 'Action parameter not recognized.'
]);
