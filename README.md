# GreensStock - Point of Sale (POS) & Business Financial ERP

GreensStock is a Point of Sale (POS), Inventory Management, and Business Financial ERP application built with React, Vite, TypeScript, and Tailwind CSS.

## Features

- **Automated Profit & Loss Coverage Engine**: Dynamic real-time calculation of Gross Sales Profit, Gross Sales Loss, Current Profit, Loss Coverage status, Operating Expenses, Customer Refunds, and Final Net Profit.
- **Point of Sale (POS)**:
  - Product catalog with instant category filtering and real-time search
  - Barcode and SKU scanning
  - Shopping cart with quantity adjustment and unit margin previews
  - Customer selection (Walk-in or registered accounts)
  - Discount and VAT/Tax computation
  - Multiple payment methods: Cash, Card (POS), bKash, Nagad, Rocket, Upay, Credit Due (বাকি খাতা)
- **Thermal Receipt & Invoicing**:
  - Monospace thermal printer formatted receipts (58mm POS & 80mm standard ESC/POS)
  - Print receipt directly to thermal printers, copy formatted text, or share via WhatsApp
- **Inventory & Valuation**:
  - Total Asset Cost, Expected Retail Value, and Potential Profit calculations
  - Low stock and out-of-stock alerts
  - Product CRUD and quick restock / delta stock adjustment
- **Returns & Exchange**:
  - Full returns (cash refund) and item-for-item replacements with automated price difference calculation
  - Real-time stock restoration
- **Expense Tracker**:
  - Category breakdown: Shop Rent, Salary, Utilities, Transport, Packaging, Maintenance, Miscellaneous
- **Customer & Supplier Due Ledger (বাকি খাতা)**:
  - Customer outstanding credit tracking and due balance collection
  - Supplier vendor ledger and outstanding payables
- **Audited Financial Income Statements**:
  - Full income statement reports with date filtering (Today, 7 Days, 30 Days, Custom)
  - Printable and exportable audit reports
- **Security & Role-Based Access Control (RBAC)**:
  - Admin (মালিক / অ্যাডমিন), Manager (ম্যানেজার), Cashier (ক্যাশিয়ার / স্টাফ)
  - Quick PIN-protected POS Terminal Lock screen (Default PIN: 1234)
- **Bilingual Support**: Instant toggle between English and Bengali (বাংলা).
- **Hostinger Multi-Tenant SaaS Cloud Sync**:
  - Endpoint connection testing, MySQL table inspection, data pull, and two-way sync
- **In-App Auto Update System**:
  - Update version check, simulation, and reload application installer
