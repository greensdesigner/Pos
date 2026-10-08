export type UserRole = 'ADMIN' | 'MANAGER' | 'CASHIER';

export interface UserRoleInfo {
  role: UserRole;
  titleEn: string;
  titleBn: string;
  canViewProfitMargins: boolean;
  canViewFinancialReports: boolean;
  canDeleteRecords: boolean;
  canAdjustStock: boolean;
  canManageSettings: boolean;
}

export const USER_ROLES: Record<UserRole, UserRoleInfo> = {
  ADMIN: {
    role: 'ADMIN',
    titleEn: 'Owner / Admin',
    titleBn: 'মালিক / অ্যাডমিন',
    canViewProfitMargins: true,
    canViewFinancialReports: true,
    canDeleteRecords: true,
    canAdjustStock: true,
    canManageSettings: true,
  },
  MANAGER: {
    role: 'MANAGER',
    titleEn: 'Manager',
    titleBn: 'ম্যানেজার',
    canViewProfitMargins: true,
    canViewFinancialReports: true,
    canDeleteRecords: false,
    canAdjustStock: true,
    canManageSettings: false,
  },
  CASHIER: {
    role: 'CASHIER',
    titleEn: 'Cashier / Staff',
    titleBn: 'ক্যাশিয়ার / স্টাফ',
    canViewProfitMargins: false,
    canViewFinancialReports: false,
    canDeleteRecords: false,
    canAdjustStock: false,
    canManageSettings: false,
  },
};

export type PaymentMethod =
  | 'CASH'
  | 'CARD'
  | 'BKASH'
  | 'NAGAD'
  | 'ROCKET'
  | 'UPAY'
  | 'CREDIT_DUE';

export interface PaymentMethodInfo {
  key: PaymentMethod;
  labelEn: string;
  labelBn: string;
  isMobileBanking?: boolean;
}

export const PAYMENT_METHODS: PaymentMethodInfo[] = [
  { key: 'CASH', labelEn: 'Cash', labelBn: 'নগদ' },
  { key: 'CARD', labelEn: 'Card (POS)', labelBn: 'কার্ড' },
  { key: 'BKASH', labelEn: 'bKash', labelBn: 'বিকাশ', isMobileBanking: true },
  { key: 'NAGAD', labelEn: 'Nagad', labelBn: 'নগদ ওয়ালেট', isMobileBanking: true },
  { key: 'ROCKET', labelEn: 'Rocket', labelBn: 'রকেট', isMobileBanking: true },
  { key: 'UPAY', labelEn: 'Upay', labelBn: 'উপায়', isMobileBanking: true },
  { key: 'CREDIT_DUE', labelEn: 'Credit Due (বাকি)', labelBn: 'বাকি খাতা' },
];

export type ExpenseCategory =
  | 'RENT'
  | 'SALARY'
  | 'UTILITIES'
  | 'TRANSPORT'
  | 'SUPPLIES'
  | 'MAINTENANCE'
  | 'OTHERS';

export interface ExpenseCategoryInfo {
  key: ExpenseCategory;
  labelEn: string;
  labelBn: string;
}

export const EXPENSE_CATEGORIES: ExpenseCategoryInfo[] = [
  { key: 'RENT', labelEn: 'Shop Rent', labelBn: 'দোকান ভাড়া' },
  { key: 'SALARY', labelEn: 'Employee Salary', labelBn: 'কর্মচারীর বেতন' },
  { key: 'UTILITIES', labelEn: 'Electricity / Utility', labelBn: 'বিদ্যুৎ ও গ্যাস বিল' },
  { key: 'TRANSPORT', labelEn: 'Transport / Delivery', labelBn: 'পরিবহন ও ডেলিভারি' },
  { key: 'SUPPLIES', labelEn: 'Packaging & Supplies', labelBn: 'প্যাকেজিং ও আনুষঙ্গিক' },
  { key: 'MAINTENANCE', labelEn: 'Maintenance & Repair', labelBn: 'মেরামত ও রক্ষণাবেক্ষণ' },
  { key: 'OTHERS', labelEn: 'Miscellaneous', labelBn: 'অন্যান্য খরচ' },
];

export type ReturnType = 'FULL_RETURN' | 'EXCHANGE';

export type DateFilter = 'TODAY' | 'DAYS_7' | 'DAYS_30' | 'CUSTOM';

export interface CartItem {
  productId: number;
  barcode: string;
  name: string;
  banglaName: string;
  buyingPrice: number;
  unitPrice: number;
  quantity: number;
  unit: string;
  discountPercent?: number;
}

export interface ProductEntity {
  id: number;
  barcode: string;
  name: string;
  banglaName: string;
  category: string;
  buyingPrice: number;
  sellingPrice: number;
  wholesalePrice: number;
  stockQuantity: number;
  minStockLevel: number;
  unit: string;
  updatedAt: number;
}

export interface SaleEntity {
  id: number;
  invoiceNumber: string;
  timestamp: number;
  customerId: number | null;
  customerName: string;
  subtotal: number;
  discountAmount: number;
  vatAmount: number;
  netPayable: number;
  paidAmount: number;
  dueAmount: number;
  paymentMethod: string;
  cashierRole: string;
  status: 'COMPLETED' | 'RETURNED' | 'PARTIALLY_RETURNED';
  notes: string;
}

export interface SaleItemEntity {
  id: number;
  saleId: number;
  productId: number;
  productName: string;
  quantity: number;
  costPrice: number;
  unitPrice: number;
  subtotal: number;
  discount: number;
  profitOrLoss: number;
}

export interface CustomerEntity {
  id: number;
  name: string;
  banglaName: string;
  phone: string;
  address: string;
  totalSpent: number;
  outstandingDue: number;
  lastTransactionTime: number;
}

export interface SupplierEntity {
  id: number;
  name: string;
  companyName: string;
  phone: string;
  address: string;
  totalPurchased: number;
  outstandingPayable: number;
}

export interface ExpenseEntity {
  id: number;
  title: string;
  category: ExpenseCategory;
  amount: number;
  timestamp: number;
  paymentMethod: string;
  notes: string;
}

export interface ReturnEntity {
  id: number;
  originalSaleId: number;
  invoiceNumber: string;
  productId: number;
  productName: string;
  returnType: ReturnType;
  returnedQuantity: number;
  refundAmount: number;
  priceDifference: number; // Extra collected (>0) or refunded (<0)
  reason: string;
  timestamp: number;
}

export interface AuthUser {
  userId: string;
  email: string;
  name: string;
  role: UserRole;
}

export interface StoreConfigEntity {
  id: number;
  storeName: string;
  banglaStoreName: string;
  phone: string;
  address: string;
  receiptFooter: string;
  vatPercent: number;
  isBangla: boolean;
  activeRole: UserRole;
  terminalPin: string;
  isTerminalLocked: boolean;

  // Cloud Integration (Managed automatically on backend, never exposed)
  userId: string;
  userEmail: string;
  outletName: string;
  isCloudAutoConnected: boolean;
  lastSyncTimestamp: number;

  // Auto Update
  autoCheckUpdates: boolean;
  lastUpdateCheckTimestamp: number;
}

export interface FinancialMetrics {
  grossRevenue: number;
  grossSalesProfit: number;
  grossSalesLoss: number;
  currentProfit: number;
  currentLoss: number;
  isLossCovered: boolean;
  netProfit: number;
  totalExpenses: number;
  totalRefunds: number;
  totalOrders: number;
  totalOutstandingDue: number;
}

export type ScreenNav =
  | 'dashboard'
  | 'pos'
  | 'inventory'
  | 'returns'
  | 'expenses'
  | 'ledger'
  | 'reports'
  | 'settings';

export interface AppUpdateInfo {
  latestVersionCode: number;
  latestVersionName: string;
  releaseNotes: string;
  apkUrl: string;
  fileSizeBytes: number;
  mandatory: boolean;
  releaseDate: string;
}

export type CloudSyncState = 'IDLE' | 'SYNCING' | 'SUCCESS' | 'ERROR';

export interface SyncResult {
  state: CloudSyncState;
  message: string;
  syncedSales?: number;
  syncedProducts?: number;
}

export interface DatabaseInspectionResponse {
  success: boolean;
  message: string;
  detectedTables?: string[];
  databaseName?: string;
  serverVersion?: string;
}
