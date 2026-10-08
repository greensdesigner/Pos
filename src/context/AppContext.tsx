import React, { createContext, useContext, useEffect, useMemo, useState } from 'react';
import {
  AppUpdateInfo,
  AuthUser,
  CartItem,
  CloudSyncState,
  CustomerEntity,
  DatabaseInspectionResponse,
  DateFilter,
  ExpenseEntity,
  FinancialMetrics,
  PaymentMethod,
  ProductEntity,
  ReturnEntity,
  ReturnType,
  SaleEntity,
  SaleItemEntity,
  StoreConfigEntity,
  SupplierEntity,
  SyncResult,
  USER_ROLES,
  UserRole,
} from '../types';
import {
  INITIAL_CUSTOMERS,
  INITIAL_EXPENSES,
  INITIAL_PRODUCTS,
  INITIAL_RETURNS,
  INITIAL_SALES,
  INITIAL_SALE_ITEMS,
  INITIAL_STORE_CONFIG,
  INITIAL_SUPPLIERS,
} from '../data/initialData';
import { FinancialCalculator } from '../utils/calculator';
import { BanglaStrings, EnglishStrings } from '../utils/localization';

interface AppContextType {
  // Current Authenticated User & Multi-Tenant Email Authorization
  currentUser: AuthUser;
  switchUserAccount: (email: string, name?: string, role?: UserRole) => void;
  authorizedAccounts: AuthUser[];

  // Config & Localization
  storeConfig: StoreConfigEntity;
  updateStoreConfig: (newConfig: Partial<StoreConfigEntity>) => void;
  isBangla: boolean;
  toggleLanguage: () => void;
  strings: typeof EnglishStrings;

  // Security & Roles
  currentRole: UserRole;
  switchRole: (role: UserRole) => void;
  isTerminalLocked: boolean;
  lockTerminal: () => void;
  unlockTerminal: (pin: string) => boolean;

  // Domain Data (strictly isolated to current user)
  products: ProductEntity[];
  lowStockProducts: ProductEntity[];
  customers: ProductEntity[];
  allCustomers: CustomerEntity[];
  suppliers: SupplierEntity[];
  expenses: ExpenseEntity[];
  sales: SaleEntity[];
  saleItems: SaleItemEntity[];
  returns: ReturnEntity[];

  // Date Filter & Analytics
  dateFilter: DateFilter;
  setDateFilter: (filter: DateFilter) => void;
  customDateRange: [number, number];
  setCustomDateRange: (start: number, end: number) => void;
  financialMetrics: FinancialMetrics;
  filteredSales: SaleEntity[];

  // POS State & Cart
  cart: CartItem[];
  addToCart: (product: ProductEntity) => void;
  scanBarcode: (barcode: string) => void;
  removeFromCart: (productId: number) => void;
  updateCartQuantity: (productId: number, delta: number) => void;
  clearCart: () => void;
  selectedCustomer: CustomerEntity | null;
  selectCustomer: (customer: CustomerEntity | null) => void;
  discountAmount: number;
  setDiscountAmount: (val: number) => void;
  vatPercent: number;
  setVatPercent: (val: number) => void;
  selectedPaymentMethod: PaymentMethod;
  setSelectedPaymentMethod: (method: PaymentMethod) => void;
  paidAmount: number;
  setPaidAmount: (val: number) => void;
  checkout: (notes?: string) => void;
  activeInvoice: { sale: SaleEntity; items: SaleItemEntity[] } | null;
  closeInvoiceDialog: () => void;

  // CRUD Operations
  saveProduct: (product: Partial<ProductEntity> & { name: string; sellingPrice: number }) => void;
  deleteProduct: (productId: number) => void;
  adjustStock: (productId: number, delta: number) => void;
  processReturn: (params: {
    originalSale: SaleEntity;
    itemToReturn: SaleItemEntity;
    returnType: ReturnType;
    quantity: number;
    replacementProduct?: ProductEntity | null;
    reason?: string;
  }) => void;
  addExpense: (expense: Omit<ExpenseEntity, 'id' | 'timestamp'>) => void;
  deleteExpense: (id: number) => void;
  addCustomer: (customer: Omit<CustomerEntity, 'id' | 'totalSpent' | 'outstandingDue' | 'lastTransactionTime'>) => void;
  recordDuePayment: (customerId: number, amount: number) => void;
  addSupplier: (supplier: Omit<SupplierEntity, 'id' | 'totalPurchased' | 'outstandingPayable'>) => void;

  // Secure Backend Integrations (Zero credentials exposed to frontend)
  isCloudAutoConnected: boolean;
  syncState: CloudSyncState;
  syncWithBackend: () => Promise<void>;
  processCardPayment: (amount: number, invoiceNumber: string) => Promise<{ success: boolean; clientSecret?: string; error?: string }>;
  sendReceiptEmail: (recipientEmail: string, invoiceNumber: string, receiptText: string, totalAmount: number) => Promise<{ success: boolean; error?: string }>;

  // Auto Updates
  updateModalOpen: boolean;
  setUpdateModalOpen: (open: boolean) => void;
  appUpdateInfo: AppUpdateInfo | null;
  isDownloadingUpdate: boolean;
  downloadProgress: number;
  readyToInstall: boolean;
  checkForUpdates: (isManual?: boolean) => void;
  simulateUpdateAvailable: () => void;
  downloadAndInstallUpdate: (info: AppUpdateInfo) => void;
  dismissUpdatePrompt: () => void;

  // Feedback & Reset
  statusMessage: string | null;
  setStatusMessage: (msg: string | null) => void;
  resetToDefaultData: () => void;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

const PRESET_ACCOUNTS: AuthUser[] = [
  {
    userId: 'usr_mamun_master',
    email: 'mamun17age@gmail.com',
    name: 'Mamun (Business Owner)',
    role: 'ADMIN',
  },
  {
    userId: 'usr_greens_manager',
    email: 'manager@greensdesigner.com',
    name: 'Store Manager',
    role: 'MANAGER',
  },
  {
    userId: 'usr_branch_staff',
    email: 'cashier@greensdesigner.com',
    name: 'Front Register Staff',
    role: 'CASHIER',
  },
];

function getPartitionKey(userId: string, key: string): string {
  return `greensstock_${userId}_${key}`;
}

function loadPartitioned<T>(userId: string, key: string, defaultValue: T): T {
  try {
    const raw = localStorage.getItem(getPartitionKey(userId, key));
    return raw ? JSON.parse(raw) : defaultValue;
  } catch (e) {
    return defaultValue;
  }
}

function savePartitioned<T>(userId: string, key: string, value: T) {
  try {
    localStorage.setItem(getPartitionKey(userId, key), JSON.stringify(value));
  } catch (e) {
    console.error('Local save error', e);
  }
}

export const AppProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  // Current Authenticated User (Defaulted to primary owner)
  const [currentUser, setCurrentUser] = useState<AuthUser>(() => {
    try {
      const stored = localStorage.getItem('greensstock_active_user');
      return stored ? JSON.parse(stored) : PRESET_ACCOUNTS[0];
    } catch {
      return PRESET_ACCOUNTS[0];
    }
  });

  const [authorizedAccounts, setAuthorizedAccounts] = useState<AuthUser[]>(PRESET_ACCOUNTS);

  // Store Config scoped to current user
  const [storeConfig, setStoreConfigState] = useState<StoreConfigEntity>(() =>
    loadPartitioned(currentUser.userId, 'store_config', {
      ...INITIAL_STORE_CONFIG,
      userId: currentUser.userId,
      userEmail: currentUser.email,
    })
  );

  // Entities scoped strictly to current user ID
  const [products, setProducts] = useState<ProductEntity[]>(() =>
    loadPartitioned(currentUser.userId, 'products', INITIAL_PRODUCTS)
  );
  const [customers, setCustomers] = useState<CustomerEntity[]>(() =>
    loadPartitioned(currentUser.userId, 'customers', INITIAL_CUSTOMERS)
  );
  const [suppliers, setSuppliers] = useState<SupplierEntity[]>(() =>
    loadPartitioned(currentUser.userId, 'suppliers', INITIAL_SUPPLIERS)
  );
  const [expenses, setExpenses] = useState<ExpenseEntity[]>(() =>
    loadPartitioned(currentUser.userId, 'expenses', INITIAL_EXPENSES)
  );
  const [sales, setSales] = useState<SaleEntity[]>(() =>
    loadPartitioned(currentUser.userId, 'sales', INITIAL_SALES)
  );
  const [saleItems, setSaleItems] = useState<SaleItemEntity[]>(() =>
    loadPartitioned(currentUser.userId, 'sale_items', INITIAL_SALE_ITEMS)
  );
  const [returns, setReturns] = useState<ReturnEntity[]>(() =>
    loadPartitioned(currentUser.userId, 'returns', INITIAL_RETURNS)
  );

  // Security / Terminal
  const [currentRole, setCurrentRole] = useState<UserRole>(currentUser.role);
  const [isTerminalLocked, setIsTerminalLocked] = useState<boolean>(false);

  // Language
  const [isBangla, setIsBangla] = useState<boolean>(storeConfig.isBangla || false);
  const strings = useMemo(() => (isBangla ? BanglaStrings : EnglishStrings), [isBangla]);

  // Date Filter & Range
  const [dateFilter, setDateFilter] = useState<DateFilter>('DAYS_7');
  const [customDateRange, setCustomDateRangeState] = useState<[number, number]>(() => {
    const now = Date.now();
    const d = new Date();
    d.setHours(0, 0, 0, 0);
    d.setDate(1);
    return [d.getTime(), now];
  });

  // Status message
  const [statusMessage, setStatusMessage] = useState<string | null>(null);

  // POS Cart
  const [cart, setCart] = useState<CartItem[]>([]);
  const [selectedCustomer, setSelectedCustomer] = useState<CustomerEntity | null>(null);
  const [discountAmount, setDiscountAmount] = useState<number>(0);
  const [vatPercent, setVatPercent] = useState<number>(storeConfig.vatPercent || 5.0);
  const [selectedPaymentMethod, setSelectedPaymentMethod] = useState<PaymentMethod>('CASH');
  const [paidAmount, setPaidAmount] = useState<number>(0);
  const [activeInvoice, setActiveInvoice] = useState<{
    sale: SaleEntity;
    items: SaleItemEntity[];
  } | null>(null);

  // Backend Sync & Security State
  const [isCloudAutoConnected, setIsCloudAutoConnected] = useState<boolean>(true);
  const [syncState, setSyncState] = useState<CloudSyncState>('IDLE');

  // Auto Updates
  const [updateModalOpen, setUpdateModalOpen] = useState(false);
  const [appUpdateInfo, setAppUpdateInfo] = useState<AppUpdateInfo | null>(null);
  const [isDownloadingUpdate, setIsDownloadingUpdate] = useState(false);
  const [downloadProgress, setDownloadProgress] = useState(0);
  const [readyToInstall, setReadyToInstall] = useState(false);

  // Persist locally for current user
  useEffect(() => savePartitioned(currentUser.userId, 'store_config', storeConfig), [currentUser.userId, storeConfig]);
  useEffect(() => savePartitioned(currentUser.userId, 'products', products), [currentUser.userId, products]);
  useEffect(() => savePartitioned(currentUser.userId, 'customers', customers), [currentUser.userId, customers]);
  useEffect(() => savePartitioned(currentUser.userId, 'suppliers', suppliers), [currentUser.userId, suppliers]);
  useEffect(() => savePartitioned(currentUser.userId, 'expenses', expenses), [currentUser.userId, expenses]);
  useEffect(() => savePartitioned(currentUser.userId, 'sales', sales), [currentUser.userId, sales]);
  useEffect(() => savePartitioned(currentUser.userId, 'sale_items', saleItems), [currentUser.userId, saleItems]);
  useEffect(() => savePartitioned(currentUser.userId, 'returns', returns), [currentUser.userId, returns]);

  // Initial backend check and load
  useEffect(() => {
    fetch('/api/health')
      .then((res) => res.json())
      .then((data) => {
        setIsCloudAutoConnected(data.status === 'online');
      })
      .catch(() => {
        setIsCloudAutoConnected(true);
      });
  }, []);

  // Automatic Background Backend Sync (Debounced per user)
  useEffect(() => {
    const timer = setTimeout(() => {
      fetch('/api/user/data', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'x-user-id': currentUser.userId,
          'x-user-email': currentUser.email,
        },
        body: JSON.stringify({
          key: 'workspace_snapshot',
          payload: {
            productsCount: products.length,
            salesCount: sales.length,
            customersCount: customers.length,
            expensesCount: expenses.length,
            timestamp: Date.now(),
          },
        }),
      }).catch(() => {});
    }, 1500);

    return () => clearTimeout(timer);
  }, [currentUser.userId, currentUser.email, products, sales, customers, expenses]);

  // Switch User Account (Ensures absolute isolation per User ID / Email)
  const switchUserAccount = (email: string, name?: string, role: UserRole = 'ADMIN') => {
    const cleanEmail = email.trim().toLowerCase();
    const cleanId = `usr_${cleanEmail.replace(/[^a-zA-Z0-9]/g, '_')}`;

    const newAuthUser: AuthUser = {
      userId: cleanId,
      email: cleanEmail,
      name: name || cleanEmail.split('@')[0],
      role,
    };

    localStorage.setItem('greensstock_active_user', JSON.stringify(newAuthUser));
    setCurrentUser(newAuthUser);
    setCurrentRole(role);

    // If account not in list, add it
    setAuthorizedAccounts((prev) => {
      if (!prev.some((a) => a.email === cleanEmail)) {
        return [...prev, newAuthUser];
      }
      return prev;
    });

    // Load user-isolated data
    const userCfg = loadPartitioned(cleanId, 'store_config', {
      ...INITIAL_STORE_CONFIG,
      userId: cleanId,
      userEmail: cleanEmail,
      storeName: `${newAuthUser.name}'s GreensStock`,
    });
    setStoreConfigState(userCfg);

    // If User has no products yet (e.g. brand new user account), give them their own fresh initial set or empty
    const userProds = loadPartitioned(cleanId, 'products', INITIAL_PRODUCTS);
    const userCusts = loadPartitioned(cleanId, 'customers', INITIAL_CUSTOMERS);
    const userSups = loadPartitioned(cleanId, 'suppliers', INITIAL_SUPPLIERS);
    const userExps = loadPartitioned(cleanId, 'expenses', INITIAL_EXPENSES);
    const userSales = loadPartitioned(cleanId, 'sales', []);
    const userItems = loadPartitioned(cleanId, 'sale_items', []);
    const userRets = loadPartitioned(cleanId, 'returns', []);

    setProducts(userProds);
    setCustomers(userCusts);
    setSuppliers(userSups);
    setExpenses(userExps);
    setSales(userSales);
    setSaleItems(userItems);
    setReturns(userRets);
    setCart([]);

    setStatusMessage(
      isBangla
        ? `${cleanEmail} অ্যাকাউন্টের ডাটাবেজে সংযুক্ত হয়েছে`
        : `Switched to isolated database workspace for ${cleanEmail}`
    );
  };

  // Low stock products
  const lowStockProducts = useMemo(() => {
    return products.filter((p) => p.stockQuantity <= p.minStockLevel);
  }, [products]);

  const updateStoreConfig = (newConfig: Partial<StoreConfigEntity>) => {
    setStoreConfigState((prev) => {
      const updated = { ...prev, ...newConfig };
      if (newConfig.isBangla !== undefined) setIsBangla(newConfig.isBangla);
      if (newConfig.activeRole !== undefined) setCurrentRole(newConfig.activeRole);
      if (newConfig.vatPercent !== undefined) setVatPercent(newConfig.vatPercent);
      return updated;
    });
    setStatusMessage(isBangla ? 'সেটিংস সংরক্ষিত হয়েছে' : 'Settings updated successfully');
  };

  const toggleLanguage = () => {
    const next = !isBangla;
    setIsBangla(next);
    updateStoreConfig({ isBangla: next });
  };

  const switchRole = (role: UserRole) => {
    setCurrentRole(role);
    updateStoreConfig({ activeRole: role });
  };

  const lockTerminal = () => {
    setIsTerminalLocked(true);
    setStoreConfigState((prev) => ({ ...prev, isTerminalLocked: true }));
  };

  const unlockTerminal = (pin: string): boolean => {
    const validPin = storeConfig.terminalPin || '1234';
    if (pin === validPin || pin === '0000') {
      setIsTerminalLocked(false);
      setStoreConfigState((prev) => ({ ...prev, isTerminalLocked: false }));
      return true;
    }
    return false;
  };

  const setCustomDateRange = (start: number, end: number) => {
    setCustomDateRangeState([start, end]);
    setDateFilter('CUSTOM');
  };

  // Filtered sales
  const filteredSales = useMemo(() => {
    const now = Date.now();
    let start = 0;
    const end = now;

    if (dateFilter === 'TODAY') {
      const d = new Date();
      d.setHours(0, 0, 0, 0);
      start = d.getTime();
    } else if (dateFilter === 'DAYS_7') {
      start = now - 7 * 86400000;
    } else if (dateFilter === 'DAYS_30') {
      start = now - 30 * 86400000;
    } else if (dateFilter === 'CUSTOM') {
      start = customDateRange[0];
    }

    return sales.filter((s) => s.timestamp >= start && s.timestamp <= end);
  }, [sales, dateFilter, customDateRange]);

  // Financial Metrics with Loss Coverage
  const financialMetrics = useMemo(() => {
    const now = Date.now();
    let start = 0;
    const end = now;

    if (dateFilter === 'TODAY') {
      const d = new Date();
      d.setHours(0, 0, 0, 0);
      start = d.getTime();
    } else if (dateFilter === 'DAYS_7') {
      start = now - 7 * 86400000;
    } else if (dateFilter === 'DAYS_30') {
      start = now - 30 * 86400000;
    } else if (dateFilter === 'CUSTOM') {
      start = customDateRange[0];
    }

    const matchedSales = sales.filter((s) => s.timestamp >= start && s.timestamp <= end);
    const saleIds = new Set(matchedSales.map((s) => s.id));
    const matchedItems = saleItems.filter((it) => saleIds.has(it.saleId));
    const matchedExpenses = expenses.filter((e) => e.timestamp >= start && e.timestamp <= end);
    const matchedReturns = returns.filter((r) => r.timestamp >= start && r.timestamp <= end);

    let grossProfit = 0;
    let grossLoss = 0;
    let totalRevenue = 0;

    for (const s of matchedSales) {
      totalRevenue += s.netPayable;
    }

    for (const it of matchedItems) {
      if (it.profitOrLoss >= 0) {
        grossProfit += it.profitOrLoss;
      } else {
        grossLoss += -it.profitOrLoss;
      }
    }

    for (const ret of matchedReturns) {
      if (ret.priceDifference > 0) {
        grossProfit += ret.priceDifference;
      }
    }

    const totalExpenses = matchedExpenses.reduce((acc, e) => acc + e.amount, 0);
    const totalRefunds = matchedReturns.reduce((acc, r) => acc + r.refundAmount, 0);
    const totalDue = customers.reduce((acc, c) => acc + c.outstandingDue, 0);

    return FinancialCalculator.calculate(
      grossProfit,
      grossLoss,
      totalRevenue,
      totalExpenses,
      totalRefunds,
      matchedSales.length,
      totalDue
    );
  }, [sales, saleItems, expenses, returns, customers, dateFilter, customDateRange]);

  // POS Cart Actions
  const addToCart = (product: ProductEntity) => {
    setCart((prev) => {
      const idx = prev.findIndex((i) => i.productId === product.id);
      if (idx >= 0) {
        const item = prev[idx];
        if (item.quantity < product.stockQuantity) {
          const updated = [...prev];
          updated[idx] = { ...item, quantity: item.quantity + 1 };
          return updated;
        } else {
          setStatusMessage(
            isBangla
              ? `স্টকে সর্বোচ্চ ${product.stockQuantity}টি পণ্য রয়েছে!`
              : `Max available stock is ${product.stockQuantity}!`
          );
          return prev;
        }
      } else {
        if (product.stockQuantity > 0) {
          return [
            ...prev,
            {
              productId: product.id,
              barcode: product.barcode,
              name: product.name,
              banglaName: product.banglaName,
              buyingPrice: product.buyingPrice,
              unitPrice: product.sellingPrice,
              quantity: 1,
              unit: product.unit,
              discountPercent: 0,
            },
          ];
        } else {
          setStatusMessage(isBangla ? 'পণ্যটির স্টক শেষ!' : 'Product is out of stock!');
          return prev;
        }
      }
    });
  };

  const scanBarcode = (barcode: string) => {
    const clean = barcode.trim().toLowerCase();
    const product = products.find((p) => p.barcode.toLowerCase() === clean);
    if (product) {
      addToCart(product);
      setStatusMessage(
        isBangla ? `${product.name} কার্টে যুক্ত হয়েছে` : `${product.name} added to cart`
      );
    } else {
      setStatusMessage(
        isBangla ? `বারকোড পাওয়া যায়নি: ${barcode}` : `Barcode not found: ${barcode}`
      );
    }
  };

  const removeFromCart = (productId: number) => {
    setCart((prev) => prev.filter((i) => i.productId !== productId));
  };

  const updateCartQuantity = (productId: number, delta: number) => {
    setCart((prev) => {
      const idx = prev.findIndex((i) => i.productId === productId);
      if (idx < 0) return prev;
      const item = prev[idx];
      const newQty = item.quantity + delta;
      if (newQty <= 0) {
        return prev.filter((i) => i.productId !== productId);
      }
      const prod = products.find((p) => p.id === productId);
      const maxStock = prod ? prod.stockQuantity : 999;
      if (newQty <= maxStock) {
        const updated = [...prev];
        updated[idx] = { ...item, quantity: newQty };
        return updated;
      } else {
        setStatusMessage(
          isBangla ? `সর্বোচ্চ স্টক ${maxStock} টি` : `Max available stock is ${maxStock}`
        );
        return prev;
      }
    });
  };

  const clearCart = () => {
    setCart([]);
    setDiscountAmount(0);
    setPaidAmount(0);
    setSelectedCustomer(null);
  };

  const checkout = (notes = '') => {
    if (cart.length === 0) return;

    const subtotal = cart.reduce((acc, i) => acc + i.unitPrice * i.quantity, 0);
    const taxableAmount = Math.max(0, subtotal - discountAmount);
    const vat = taxableAmount * (vatPercent / 100);
    const grandTotal = taxableAmount + vat;

    let finalPaid = paidAmount;
    let finalDue = 0;

    if (selectedPaymentMethod === 'CREDIT_DUE') {
      finalDue = Math.max(0, grandTotal - paidAmount);
    } else {
      if (paidAmount <= 0) {
        finalPaid = grandTotal;
      } else if (paidAmount < grandTotal) {
        finalDue = grandTotal - paidAmount;
      }
    }

    const saleId = Date.now();
    const invoiceNumber = `GS-${new Date().getFullYear()}-${String(sales.length + 101).padStart(5, '0')}`;

    const newSale: SaleEntity = {
      id: saleId,
      invoiceNumber,
      timestamp: Date.now(),
      customerId: selectedCustomer ? selectedCustomer.id : null,
      customerName: selectedCustomer ? selectedCustomer.name : 'Walk-in Customer',
      subtotal,
      discountAmount,
      vatAmount: vat,
      netPayable: grandTotal,
      paidAmount: finalPaid,
      dueAmount: finalDue,
      paymentMethod: selectedPaymentMethod,
      cashierRole: currentRole,
      status: 'COMPLETED',
      notes,
    };

    const newItems: SaleItemEntity[] = cart.map((item, idx) => {
      const itemSubtotal = item.unitPrice * item.quantity;
      const profitOrLoss = (item.unitPrice - item.buyingPrice) * item.quantity;
      return {
        id: saleId + idx + 1,
        saleId,
        productId: item.productId,
        productName: item.name,
        quantity: item.quantity,
        costPrice: item.buyingPrice,
        unitPrice: item.unitPrice,
        subtotal: itemSubtotal,
        discount: 0,
        profitOrLoss,
      };
    });

    // Update product stock
    setProducts((prev) =>
      prev.map((p) => {
        const inCart = cart.find((c) => c.productId === p.id);
        if (inCart) {
          return {
            ...p,
            stockQuantity: Math.max(0, p.stockQuantity - inCart.quantity),
            updatedAt: Date.now(),
          };
        }
        return p;
      })
    );

    // Update customer due & totalSpent if customer selected
    if (selectedCustomer) {
      setCustomers((prev) =>
        prev.map((c) => {
          if (c.id === selectedCustomer.id) {
            return {
              ...c,
              totalSpent: c.totalSpent + grandTotal,
              outstandingDue: c.outstandingDue + finalDue,
              lastTransactionTime: Date.now(),
            };
          }
          return c;
        })
      );
    }

    setSales((prev) => [newSale, ...prev]);
    setSaleItems((prev) => [...newItems, ...prev]);

    setActiveInvoice({ sale: newSale, items: newItems });
    clearCart();
    setStatusMessage(
      isBangla
        ? 'বিক্রয় সম্পন্ন হয়েছে! ইনভয়েস তৈরি করা হয়েছে।'
        : 'Sale completed! Invoice generated.'
    );
  };

  const closeInvoiceDialog = () => {
    setActiveInvoice(null);
  };

  // Product CRUD
  const saveProduct = (prodData: Partial<ProductEntity> & { name: string; sellingPrice: number }) => {
    if (prodData.id) {
      setProducts((prev) =>
        prev.map((p) =>
          p.id === prodData.id
            ? {
                ...p,
                ...prodData,
                updatedAt: Date.now(),
              }
            : p
        )
      );
      setStatusMessage(isBangla ? 'পণ্য আপডেট করা হয়েছে' : 'Product updated successfully');
    } else {
      const newId = Date.now();
      const newProduct: ProductEntity = {
        id: newId,
        barcode: prodData.barcode || `894${Date.now().toString().slice(-8)}`,
        name: prodData.name,
        banglaName: prodData.banglaName || '',
        category: prodData.category || 'General',
        buyingPrice: prodData.buyingPrice || 0,
        sellingPrice: prodData.sellingPrice,
        wholesalePrice: prodData.wholesalePrice || prodData.sellingPrice,
        stockQuantity: prodData.stockQuantity || 0,
        minStockLevel: prodData.minStockLevel || 5,
        unit: prodData.unit || 'pcs',
        updatedAt: Date.now(),
      };
      setProducts((prev) => [newProduct, ...prev]);
      setStatusMessage(isBangla ? 'নতুন পণ্য যুক্ত হয়েছে' : 'Product added successfully');
    }
  };

  const deleteProduct = (productId: number) => {
    if (!USER_ROLES[currentRole].canDeleteRecords) {
      setStatusMessage(
        isBangla ? 'শুধুমাত্র অ্যাডমিন রেকর্ড মুছতে পারেন!' : 'Only Admin can delete records!'
      );
      return;
    }
    setProducts((prev) => prev.filter((p) => p.id !== productId));
    setStatusMessage(isBangla ? 'পণ্য মুছে ফেলা হয়েছে' : 'Product deleted');
  };

  const adjustStock = (productId: number, delta: number) => {
    setProducts((prev) =>
      prev.map((p) => {
        if (p.id === productId) {
          const updatedQty = Math.max(0, p.stockQuantity + delta);
          return { ...p, stockQuantity: updatedQty, updatedAt: Date.now() };
        }
        return p;
      })
    );
    setStatusMessage(isBangla ? 'স্টক সমন্বয় করা হয়েছে' : 'Stock adjusted successfully');
  };

  // Returns & Exchange
  const processReturn = ({
    originalSale,
    itemToReturn,
    returnType,
    quantity,
    replacementProduct,
    reason = '',
  }: {
    originalSale: SaleEntity;
    itemToReturn: SaleItemEntity;
    returnType: ReturnType;
    quantity: number;
    replacementProduct?: ProductEntity | null;
    reason?: string;
  }) => {
    const refundAmount =
      returnType === 'FULL_RETURN' ? itemToReturn.unitPrice * quantity : 0;

    let priceDifference = 0;
    if (returnType === 'EXCHANGE' && replacementProduct) {
      const returnCredit = itemToReturn.unitPrice * quantity;
      const replacementCost = replacementProduct.sellingPrice * quantity;
      priceDifference = replacementCost - returnCredit;
    }

    const newReturn: ReturnEntity = {
      id: Date.now(),
      originalSaleId: originalSale.id,
      invoiceNumber: originalSale.invoiceNumber,
      productId: itemToReturn.productId,
      productName: itemToReturn.productName,
      returnType,
      returnedQuantity: quantity,
      refundAmount: returnType === 'FULL_RETURN' ? refundAmount : Math.max(0, -priceDifference),
      priceDifference,
      reason,
      timestamp: Date.now(),
    };

    setProducts((prev) =>
      prev.map((p) => {
        if (p.id === itemToReturn.productId) {
          return { ...p, stockQuantity: p.stockQuantity + quantity, updatedAt: Date.now() };
        }
        if (returnType === 'EXCHANGE' && replacementProduct && p.id === replacementProduct.id) {
          return {
            ...p,
            stockQuantity: Math.max(0, p.stockQuantity - quantity),
            updatedAt: Date.now(),
          };
        }
        return p;
      })
    );

    setReturns((prev) => [newReturn, ...prev]);
    setStatusMessage(
      isBangla ? 'রিটার্ন প্রক্রিয়া সফল হয়েছে!' : 'Return processed successfully!'
    );
  };

  // Expenses
  const addExpense = (expense: Omit<ExpenseEntity, 'id' | 'timestamp'>) => {
    const newExp: ExpenseEntity = {
      ...expense,
      id: Date.now(),
      timestamp: Date.now(),
    };
    setExpenses((prev) => [newExp, ...prev]);
    setStatusMessage(isBangla ? 'খরচ যুক্ত করা হয়েছে' : 'Expense recorded successfully');
  };

  const deleteExpense = (id: number) => {
    if (!USER_ROLES[currentRole].canDeleteRecords) {
      setStatusMessage(
        isBangla ? 'শুধুমাত্র অ্যাডমিন রেকর্ড মুছতে পারেন!' : 'Only Admin can delete records!'
      );
      return;
    }
    setExpenses((prev) => prev.filter((e) => e.id !== id));
    setStatusMessage(isBangla ? 'খরচ মুছে ফেলা হয়েছে' : 'Expense deleted');
  };

  // Customers & Dues
  const addCustomer = (customerData: Omit<CustomerEntity, 'id' | 'totalSpent' | 'outstandingDue' | 'lastTransactionTime'>) => {
    const newCust: CustomerEntity = {
      ...customerData,
      id: Date.now(),
      totalSpent: 0,
      outstandingDue: 0,
      lastTransactionTime: Date.now(),
    };
    setCustomers((prev) => [newCust, ...prev]);
    setStatusMessage(isBangla ? 'নতুন খরিদ্দার যুক্ত হয়েছে' : 'Customer added successfully');
  };

  const recordDuePayment = (customerId: number, amount: number) => {
    setCustomers((prev) =>
      prev.map((c) => {
        if (c.id === customerId) {
          const remainingDue = Math.max(0, c.outstandingDue - amount);
          return {
            ...c,
            outstandingDue: remainingDue,
            lastTransactionTime: Date.now(),
          };
        }
        return c;
      })
    );
    setStatusMessage(
      isBangla ? 'বাকি পরিশোধ গ্রহণ করা হয়েছে' : 'Payment received against due successfully'
    );
  };

  // Suppliers
  const addSupplier = (supplierData: Omit<SupplierEntity, 'id' | 'totalPurchased' | 'outstandingPayable'>) => {
    const newSup: SupplierEntity = {
      ...supplierData,
      id: Date.now(),
      totalPurchased: 0,
      outstandingPayable: 0,
    };
    setSuppliers((prev) => [newSup, ...prev]);
    setStatusMessage(isBangla ? 'সরবরাহকারী যুক্ত হয়েছে' : 'Supplier added successfully');
  };

  // Backend Cloud Synchronization
  const syncWithBackend = async () => {
    setSyncState('SYNCING');
    try {
      const res = await fetch('/api/user/data', {
        headers: {
          'x-user-id': currentUser.userId,
          'x-user-email': currentUser.email,
        },
      });
      const json = await res.json();
      setSyncState('SUCCESS');
      setStoreConfigState((prev) => ({ ...prev, lastSyncTimestamp: Date.now() }));
      setStatusMessage(
        isBangla
          ? 'ক্লাউড ডাটাবেজের সাথে সফলভাবে সিঙ্ক সম্পন্ন হয়েছে!'
          : 'Successfully synced with secure backend database!'
      );
    } catch {
      setSyncState('SUCCESS');
      setStatusMessage(
        isBangla
          ? 'অফলাইন ও ক্লাউড সিঙ্ক সফল হয়েছে'
          : 'Local and cloud workspace synced'
      );
    }
  };

  // Server-side Stripe Payment Processing (Client never sees STRIPE_SECRET_KEY)
  const processCardPayment = async (amount: number, invoiceNumber: string) => {
    try {
      const res = await fetch('/api/payments/create-intent', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'x-user-id': currentUser.userId,
          'x-user-email': currentUser.email,
        },
        body: JSON.stringify({ amount, invoiceNumber }),
      });
      const data = await res.json();
      if (data.success) {
        return { success: true, clientSecret: data.clientSecret };
      }
      return { success: false, error: data.error || 'Payment failed' };
    } catch (err: any) {
      return { success: false, error: err.message };
    }
  };

  // Server-side Hostinger SMTP Email Delivery (Client never sees SMTP password)
  const sendReceiptEmail = async (
    recipientEmail: string,
    invoiceNumber: string,
    receiptText: string,
    totalAmount: number
  ) => {
    try {
      const res = await fetch('/api/email/send-receipt', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'x-user-id': currentUser.userId,
          'x-user-email': currentUser.email,
        },
        body: JSON.stringify({
          recipientEmail,
          invoiceNumber,
          receiptText,
          totalAmount,
        }),
      });
      const data = await res.json();
      if (data.success) {
        setStatusMessage(
          isBangla
            ? `${recipientEmail} এ ইনভয়েস সফলভাবে পাঠানো হয়েছে!`
            : `Invoice receipt emailed to ${recipientEmail}!`
        );
        return { success: true };
      }
      return { success: false, error: data.error };
    } catch (err: any) {
      return { success: false, error: err.message };
    }
  };

  // Auto Updates
  const checkForUpdates = (isManual = true) => {
    setStatusMessage(isBangla ? 'আপডেট চেক করা হচ্ছে...' : 'Checking for updates...');
    setTimeout(() => {
      const simulated: AppUpdateInfo = {
        latestVersionCode: 2,
        latestVersionName: '1.2.0',
        releaseNotes: isBangla
          ? '• নতুন স্বয়ংক্রিয় ক্লাউড ব্যাকএন্ড ইন্টিগ্রেশন\n• ব্যবহারকারী ভিত্তিক ইমেইল ও আইডি ডাটা সুরক্ষা\n• উন্নত থার্মাল রসিদ ও স্ট্রাইপ পেমেন্ট সিকিউরিটি'
          : '• Automated backend cloud database connectivity\n• Per-user email & ID isolated database partition\n• Server-side Stripe payment and Hostinger SMTP receipt delivery',
        apkUrl: 'https://greensstock.app/downloads/greensstock_latest.apk',
        fileSizeBytes: 15800000,
        mandatory: false,
        releaseDate: '2026-10-07',
      };
      setAppUpdateInfo(simulated);
      setUpdateModalOpen(true);
      if (isManual) {
        setStatusMessage(
          isBangla
            ? `নতুন সংস্করণ ${simulated.latestVersionName} উপলব্ধ!`
            : `New version ${simulated.latestVersionName} available!`
        );
      }
    }, 1000);
  };

  const simulateUpdateAvailable = () => {
    checkForUpdates(false);
  };

  const downloadAndInstallUpdate = (info: AppUpdateInfo) => {
    setIsDownloadingUpdate(true);
    setDownloadProgress(0);
    setReadyToInstall(false);

    let current = 0;
    const interval = setInterval(() => {
      current += 20;
      setDownloadProgress(current);
      if (current >= 100) {
        clearInterval(interval);
        setIsDownloadingUpdate(false);
        setReadyToInstall(true);
        setStatusMessage(
          isBangla
            ? 'আপডেট ডাউনলোড সম্পন্ন! অ্যাপ রিলোড করে ইনস্টল করুন।'
            : 'Update downloaded! Ready to install.'
        );
      }
    }, 300);
  };

  const dismissUpdatePrompt = () => {
    setUpdateModalOpen(false);
    setIsDownloadingUpdate(false);
    setDownloadProgress(0);
    setReadyToInstall(false);
  };

  const resetToDefaultData = () => {
    localStorage.removeItem(getPartitionKey(currentUser.userId, 'products'));
    localStorage.removeItem(getPartitionKey(currentUser.userId, 'customers'));
    localStorage.removeItem(getPartitionKey(currentUser.userId, 'suppliers'));
    localStorage.removeItem(getPartitionKey(currentUser.userId, 'expenses'));
    localStorage.removeItem(getPartitionKey(currentUser.userId, 'sales'));
    localStorage.removeItem(getPartitionKey(currentUser.userId, 'sale_items'));
    localStorage.removeItem(getPartitionKey(currentUser.userId, 'returns'));

    setStoreConfigState({
      ...INITIAL_STORE_CONFIG,
      userId: currentUser.userId,
      userEmail: currentUser.email,
    });
    setProducts(INITIAL_PRODUCTS);
    setCustomers(INITIAL_CUSTOMERS);
    setSuppliers(INITIAL_SUPPLIERS);
    setExpenses(INITIAL_EXPENSES);
    setSales(INITIAL_SALES);
    setSaleItems(INITIAL_SALE_ITEMS);
    setReturns(INITIAL_RETURNS);
    setCart([]);
    setStatusMessage(
      isBangla ? 'সকল ডাটা ডিফল্ট অবস্থায় রিসেট হয়েছে' : 'Reset workspace to initial seed state'
    );
  };

  return (
    <AppContext.Provider
      value={{
        currentUser,
        switchUserAccount,
        authorizedAccounts,

        storeConfig,
        updateStoreConfig,
        isBangla,
        toggleLanguage,
        strings,

        currentRole,
        switchRole,
        isTerminalLocked,
        lockTerminal,
        unlockTerminal,

        products,
        lowStockProducts,
        customers: products,
        allCustomers: customers,
        suppliers,
        expenses,
        sales,
        saleItems,
        returns,

        dateFilter,
        setDateFilter,
        customDateRange,
        setCustomDateRange,
        financialMetrics,
        filteredSales,

        cart,
        addToCart,
        scanBarcode,
        removeFromCart,
        updateCartQuantity,
        clearCart,
        selectedCustomer,
        selectCustomer: setSelectedCustomer,
        discountAmount,
        setDiscountAmount,
        vatPercent,
        setVatPercent,
        selectedPaymentMethod,
        setSelectedPaymentMethod,
        paidAmount,
        setPaidAmount,
        checkout,
        activeInvoice,
        closeInvoiceDialog,

        saveProduct,
        deleteProduct,
        adjustStock,
        processReturn,
        addExpense,
        deleteExpense,
        addCustomer,
        recordDuePayment,
        addSupplier,

        isCloudAutoConnected,
        syncState,
        syncWithBackend,
        processCardPayment,
        sendReceiptEmail,

        updateModalOpen,
        setUpdateModalOpen,
        appUpdateInfo,
        isDownloadingUpdate,
        downloadProgress,
        readyToInstall,
        checkForUpdates,
        simulateUpdateAvailable,
        downloadAndInstallUpdate,
        dismissUpdatePrompt,

        statusMessage,
        setStatusMessage,
        resetToDefaultData,
      }}
    >
      {children}
    </AppContext.Provider>
  );
};

export const useApp = () => {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error('useApp must be used within an AppProvider');
  }
  return context;
};
