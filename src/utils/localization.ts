const banglaDigits: Record<string, string> = {
  '0': '০',
  '1': '১',
  '2': '২',
  '3': '৩',
  '4': '৪',
  '5': '৫',
  '6': '৬',
  '7': '৭',
  '8': '৮',
  '9': '৯',
};

export const Localization = {
  toBanglaDigits(input: string | number): string {
    const str = String(input);
    let res = '';
    for (let i = 0; i < str.length; i++) {
      const ch = str[i];
      res += banglaDigits[ch] || ch;
    }
    return res;
  },

  formatNumber(number: number, isBangla: boolean): string {
    const formatted = new Intl.NumberFormat('en-US', {
      maximumFractionDigits: 0,
    }).format(number);
    return isBangla ? this.toBanglaDigits(formatted) : formatted;
  },

  formatCurrency(amount: number, isBangla: boolean): string {
    const formatted = new Intl.NumberFormat('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(amount);
    return isBangla ? `৳ ${this.toBanglaDigits(formatted)}` : `৳ ${formatted}`;
  },

  formatCompactCurrency(amount: number, isBangla: boolean): string {
    const formatted = new Intl.NumberFormat('en-US', {
      maximumFractionDigits: 0,
    }).format(amount);
    return isBangla ? `৳${this.toBanglaDigits(formatted)}` : `৳${formatted}`;
  },

  formatDate(timestamp: number, isBangla: boolean): string {
    const d = new Date(timestamp);
    const dateStr = d.toLocaleDateString('en-GB', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
    const timeStr = d.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: true,
    });
    const combined = `${dateStr} ${timeStr}`;
    return isBangla ? this.toBanglaDigits(combined) : combined;
  },
};

export interface AppStrings {
  appName: string;
  dashboard: string;
  pos: string;
  inventory: string;
  returns: string;
  expenses: string;
  ledger: string;
  reports: string;
  settings: string;

  netRevenue: string;
  currentProfit: string;
  currentLoss: string;
  netProfit: string;
  totalExpenses: string;
  totalRefunds: string;
  lossCoveredBadge: string;
  uncoveredLossBadge: string;
  estimatedMargin: string;
  grossSalesProfit: string;
  grossSalesLoss: string;

  searchProduct: string;
  allCategories: string;
  cart: string;
  emptyCart: string;
  checkout: string;
  paymentMethod: string;
  subtotal: string;
  discount: string;
  taxVat: string;
  grandTotal: string;
  paidAmount: string;
  changeAmount: string;
  dueAmount: string;
  customer: string;
  walkInCustomer: string;
  barcodeScanner: string;
  scanOrEnterSku: string;
  completeSale: string;

  invoice: string;
  thermalPrint58: string;
  thermalPrint80: string;
  shareInvoice: string;
  printReceipt: string;
  newSale: string;

  addProduct: string;
  editProduct: string;
  stockQuantity: string;
  buyingPrice: string;
  sellingPrice: string;
  wholesalePrice: string;
  lowStockAlert: string;
  outOfStock: string;
  inventoryValuation: string;
  assetCost: string;
  expectedRetailValue: string;
  potentialProfit: string;
  adjustStock: string;

  roleAdmin: string;
  roleManager: string;
  roleCashier: string;
  terminalLock: string;
  unlockTerminal: string;
  enterPin: string;
  switchRole: string;

  save: string;
  cancel: string;
  delete: string;
  confirm: string;
  filterToday: string;
  filter7Days: string;
  filter30Days: string;
  filterCustom: string;
  filterAll: string;
}

export const EnglishStrings: AppStrings = {
  appName: 'GreensStock POS & ERP',
  dashboard: 'Dashboard',
  pos: 'Point of Sale',
  inventory: 'Inventory',
  returns: 'Returns & Exchange',
  expenses: 'Expenses',
  ledger: 'Due Ledger',
  reports: 'Financial Reports',
  settings: 'Settings',

  netRevenue: 'Net Revenue',
  currentProfit: 'Current Profit',
  currentLoss: 'Current Loss',
  netProfit: 'Final Net Profit',
  totalExpenses: 'Total Expenses',
  totalRefunds: 'Total Refunds',
  lossCoveredBadge: 'Loss Covered ✓',
  uncoveredLossBadge: 'Uncovered Loss',
  estimatedMargin: 'Est. Profit Margin',
  grossSalesProfit: 'Gross Sales Profit',
  grossSalesLoss: 'Gross Sales Loss',

  searchProduct: 'Search by product name, SKU or barcode...',
  allCategories: 'All Categories',
  cart: 'Current Order Cart',
  emptyCart: 'Cart is empty. Click items to add.',
  checkout: 'Proceed to Checkout',
  paymentMethod: 'Payment Method',
  subtotal: 'Subtotal',
  discount: 'Discount',
  taxVat: 'VAT / Tax',
  grandTotal: 'Total Payable',
  paidAmount: 'Received Amount',
  changeAmount: 'Change Due',
  dueAmount: 'Customer Outstanding Due',
  customer: 'Select Customer',
  walkInCustomer: 'Walk-in Customer (নগদ খরিদ্দার)',
  barcodeScanner: 'Scan Barcode / QR',
  scanOrEnterSku: 'Scan or type barcode / SKU',
  completeSale: 'Confirm & Print Invoice',

  invoice: 'Sales Invoice',
  thermalPrint58: '58mm ESC/POS',
  thermalPrint80: '80mm ESC/POS',
  shareInvoice: 'Share via WhatsApp / SMS',
  printReceipt: 'Thermal Print Receipt',
  newSale: 'Start New Sale',

  addProduct: 'Add New Product',
  editProduct: 'Edit Product',
  stockQuantity: 'In Stock',
  buyingPrice: 'Cost Price (ক্রয়)',
  sellingPrice: 'Retail Price (বিক্রয়)',
  wholesalePrice: 'Wholesale Price (পাইকারি)',
  lowStockAlert: 'Low Stock Alert',
  outOfStock: 'Out of Stock',
  inventoryValuation: 'Inventory Valuation',
  assetCost: 'Total Asset Cost',
  expectedRetailValue: 'Total Retail Value',
  potentialProfit: 'Potential Profit',
  adjustStock: 'Quick Restock / Adjust',

  roleAdmin: 'Business Owner / Admin',
  roleManager: 'Manager',
  roleCashier: 'Cashier / Staff',
  terminalLock: 'Terminal Locked',
  unlockTerminal: 'Unlock Terminal',
  enterPin: 'Enter Terminal PIN',
  switchRole: 'Switch Active Role',

  save: 'Save',
  cancel: 'Cancel',
  delete: 'Delete',
  confirm: 'Confirm',
  filterToday: 'Today',
  filter7Days: '7 Days',
  filter30Days: '30 Days',
  filterCustom: 'Custom Date',
  filterAll: 'All Time',
};

export const BanglaStrings: AppStrings = {
  appName: 'গ্রিনসস্টক পিওএস ও ইআরপি',
  dashboard: 'ড্যাশবোর্ড',
  pos: 'বিক্রয় (পিওএস)',
  inventory: 'মজুদ / স্টক',
  returns: 'ফেরত ও বদল',
  expenses: 'খরচ হিসাব',
  ledger: 'বাকি খাতা',
  reports: 'আর্থিক রিপোর্ট',
  settings: 'সেটিংস',

  netRevenue: 'নিট রাজস্ব (বিক্রয়)',
  currentProfit: 'বর্তমান লাভ',
  currentLoss: 'বর্তমান ক্ষতি',
  netProfit: 'নিট লাভ (চূড়ান্ত)',
  totalExpenses: 'মোট খরচ',
  totalRefunds: 'মোট ফেরত / রিফান্ড',
  lossCoveredBadge: 'ক্ষতি কাভার্ড ✓',
  uncoveredLossBadge: 'অবশিষ্ট ক্ষতি',
  estimatedMargin: 'আনুমানিক লাভের হার',
  grossSalesProfit: 'মোট বিক্রয় লাভ',
  grossSalesLoss: 'মোট বিক্রয় ক্ষতি',

  searchProduct: 'পণ্যের নাম, বারকোড বা এসকেইউ খুঁজুন...',
  allCategories: 'সকল ক্যাটাগরি',
  cart: 'অর্ডার কার্ট',
  emptyCart: 'কার্ট খালি। পণ্য যুক্ত করতে ক্লিক করুন।',
  checkout: 'পেমেন্ট ও চেকআউট',
  paymentMethod: 'মূল্য পরিশোধ পদ্ধতি',
  subtotal: 'মোট মূল্য',
  discount: 'ছাড় / ডিসকাউন্ট',
  taxVat: 'ভ্যাট / ট্যাক্স',
  grandTotal: 'সর্বমোট প্রদেয়',
  paidAmount: 'গৃহীত টাকা',
  changeAmount: 'ফেরত টাকা',
  dueAmount: 'বাকি টাকার পরিমাণ',
  customer: 'খরিদ্দার নির্বাচন',
  walkInCustomer: 'নগদ খরিদ্দার (ওয়াক-ইন)',
  barcodeScanner: 'বারকোড স্ক্যানার',
  scanOrEnterSku: 'বারকোড স্ক্যান বা ইনপুট করুন',
  completeSale: 'বিক্রয় নিশ্চিত ও ইনভয়েস',

  invoice: 'বিক্রয় ইনভয়েস / রশিদ',
  thermalPrint58: '৫৮ মিমি থার্মাল',
  thermalPrint80: '৮০ মিমি থার্মাল',
  shareInvoice: 'হোয়াটসঅ্যাপ / এসএমএসে পাঠান',
  printReceipt: 'থার্মাল প্রিন্ট করুন',
  newSale: 'নতুন বিক্রয় শুরু',

  addProduct: 'নতুন পণ্য যুক্ত করুন',
  editProduct: 'পণ্য সম্পাদনা',
  stockQuantity: 'মজুদ পরিমাণ',
  buyingPrice: 'কেনা মূল্য (খরচ)',
  sellingPrice: 'বিক্রয় মূল্য',
  wholesalePrice: 'পাইকারি মূল্য',
  lowStockAlert: 'স্বল্প স্টক সতর্কতা',
  outOfStock: 'স্টক শেষ',
  inventoryValuation: 'মোট ইনভেন্টরি মূল্যায়ন',
  assetCost: 'কেনা মূলধন',
  expectedRetailValue: 'সম্ভাব্য বিক্রয় মূল্য',
  potentialProfit: 'প্রত্যাশিত মোট লাভ',
  adjustStock: 'স্টক সমন্বয় / বৃদ্ধি',

  roleAdmin: 'মালিক / অ্যাডমিন',
  roleManager: 'ম্যানেজার',
  roleCashier: 'ক্যাশিয়ার / স্টাফ',
  terminalLock: 'টার্মিনাল লক করা',
  unlockTerminal: 'টার্মিনাল আনলক',
  enterPin: 'পিন নম্বর দিন',
  switchRole: 'রোল পরিবর্তন করুন',

  save: 'সংরক্ষণ',
  cancel: 'বাতিল',
  delete: 'মুছে ফেলুন',
  confirm: 'নিশ্চিত করুন',
  filterToday: 'আজকের',
  filter7Days: '৭ দিন',
  filter30Days: '৩০ দিন',
  filterCustom: 'কাস্টম ডেট',
  filterAll: 'সর্বমোট',
};
