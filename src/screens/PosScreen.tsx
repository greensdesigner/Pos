import React, { useMemo, useState } from 'react';
import { useApp } from '../context/AppContext';
import { Localization } from '../utils/localization';
import { PAYMENT_METHODS, PaymentMethod } from '../types';
import {
  Search,
  Barcode,
  Plus,
  Minus,
  Trash2,
  User,
  CreditCard,
  CheckCircle,
  AlertTriangle,
  Receipt,
  UserPlus,
} from 'lucide-react';

export const PosScreen: React.FC = () => {
  const {
    products,
    cart,
    addToCart,
    scanBarcode,
    removeFromCart,
    updateCartQuantity,
    clearCart,
    selectedCustomer,
    selectCustomer,
    allCustomers,
    discountAmount,
    setDiscountAmount,
    vatPercent,
    setVatPercent,
    selectedPaymentMethod,
    setSelectedPaymentMethod,
    paidAmount,
    setPaidAmount,
    checkout,
    currentRole,
    isBangla,
    strings,
    addCustomer,
  } = useApp();

  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string>('ALL');
  const [barcodeInput, setBarcodeInput] = useState('');
  const [orderNotes, setOrderNotes] = useState('');
  const [showAddCustomerModal, setShowAddCustomerModal] = useState(false);
  const [newCustName, setNewCustName] = useState('');
  const [newCustPhone, setNewCustPhone] = useState('');
  const [newCustAddress, setNewCustAddress] = useState('');

  const canViewMargins = currentRole === 'ADMIN' || currentRole === 'MANAGER';

  // Extract unique categories
  const categories = useMemo(() => {
    const set = new Set<string>();
    products.forEach((p) => set.add(p.category));
    return ['ALL', ...Array.from(set)];
  }, [products]);

  // Filter products by category and search
  const filteredProducts = useMemo(() => {
    return products.filter((p) => {
      const matchCat = selectedCategory === 'ALL' || p.category === selectedCategory;
      const q = searchQuery.trim().toLowerCase();
      const matchSearch =
        !q ||
        p.name.toLowerCase().includes(q) ||
        p.banglaName.toLowerCase().includes(q) ||
        p.barcode.toLowerCase().includes(q);
      return matchCat && matchSearch;
    });
  }, [products, selectedCategory, searchQuery]);

  // Financial calculations for Cart
  const subtotal = useMemo(() => {
    return cart.reduce((acc, item) => acc + item.unitPrice * item.quantity, 0);
  }, [cart]);

  const taxableAmount = Math.max(0, subtotal - discountAmount);
  const vatAmount = taxableAmount * (vatPercent / 100);
  const grandTotal = taxableAmount + vatAmount;

  const dueAmount = useMemo(() => {
    if (selectedPaymentMethod === 'CREDIT_DUE') {
      return Math.max(0, grandTotal - paidAmount);
    }
    if (paidAmount > 0 && paidAmount < grandTotal) {
      return grandTotal - paidAmount;
    }
    return 0;
  }, [selectedPaymentMethod, grandTotal, paidAmount]);

  const changeAmount = useMemo(() => {
    if (selectedPaymentMethod !== 'CREDIT_DUE' && paidAmount > grandTotal) {
      return paidAmount - grandTotal;
    }
    return 0;
  }, [selectedPaymentMethod, grandTotal, paidAmount]);

  const handleBarcodeSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (barcodeInput.trim()) {
      scanBarcode(barcodeInput);
      setBarcodeInput('');
    }
  };

  const handleCreateCustomer = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newCustName.trim() || !newCustPhone.trim()) return;
    addCustomer({
      name: newCustName.trim(),
      banglaName: '',
      phone: newCustPhone.trim(),
      address: newCustAddress.trim(),
    });
    setNewCustName('');
    setNewCustPhone('');
    setNewCustAddress('');
    setShowAddCustomerModal(false);
  };

  return (
    <div className="grid h-[calc(100vh-4rem)] grid-cols-1 overflow-hidden lg:grid-cols-12">
      {/* Left Column: Product Catalog & Search (7 Cols) */}
      <div className="flex flex-col border-r border-slate-200 bg-slate-50 p-4 lg:col-span-7 xl:col-span-8 overflow-y-auto">
        {/* Search & Quick Barcode Scan Bar */}
        <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
          <div className="relative flex-1">
            <Search className="absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder={strings.searchProduct}
              className="w-full rounded-xl border border-slate-200 bg-white py-2.5 pl-10 pr-4 text-xs shadow-xs focus:border-emerald-500 focus:outline-hidden"
            />
          </div>

          <form onSubmit={handleBarcodeSubmit} className="flex items-center gap-1.5">
            <div className="relative">
              <Barcode className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
              <input
                type="text"
                value={barcodeInput}
                onChange={(e) => setBarcodeInput(e.target.value)}
                placeholder={strings.scanOrEnterSku}
                className="w-48 rounded-xl border border-slate-200 bg-white py-2.5 pl-9 pr-3 text-xs shadow-xs focus:border-emerald-500 focus:outline-hidden"
              />
            </div>
            <button
              type="submit"
              className="rounded-xl bg-slate-900 px-3 py-2.5 text-xs font-semibold text-white transition hover:bg-slate-800"
            >
              {isBangla ? 'স্ক্যান' : 'Scan'}
            </button>
          </form>
        </div>

        {/* Category Pills */}
        <div className="mt-3 flex gap-1.5 overflow-x-auto pb-1">
          {categories.map((cat) => (
            <button
              key={cat}
              onClick={() => setSelectedCategory(cat)}
              className={`rounded-lg px-3 py-1.5 text-xs font-semibold whitespace-nowrap transition ${
                selectedCategory === cat
                  ? 'bg-emerald-600 text-white shadow-xs'
                  : 'bg-white text-slate-600 border border-slate-200 hover:bg-slate-100'
              }`}
            >
              {cat === 'ALL' ? strings.allCategories : cat}
            </button>
          ))}
        </div>

        {/* Products Grid */}
        <div className="mt-4 grid flex-1 grid-cols-2 gap-3 sm:grid-cols-3 xl:grid-cols-4 overflow-y-auto pb-6">
          {filteredProducts.map((product) => {
            const inCart = cart.find((i) => i.productId === product.id);
            const isOutOfStock = product.stockQuantity <= 0;
            const isLossPricing = product.sellingPrice < product.buyingPrice;
            const unitProfitOrLoss = product.sellingPrice - product.buyingPrice;

            return (
              <div
                key={product.id}
                onClick={() => !isOutOfStock && addToCart(product)}
                className={`relative flex flex-col justify-between rounded-2xl border p-3.5 transition ${
                  isOutOfStock
                    ? 'border-slate-200 bg-slate-100/60 opacity-60 cursor-not-allowed'
                    : 'border-slate-200 bg-white hover:border-emerald-300 hover:shadow-md cursor-pointer'
                }`}
              >
                <div>
                  <div className="flex items-start justify-between gap-1">
                    <span className="rounded bg-slate-100 px-1.5 py-0.5 text-[10px] font-medium text-slate-600">
                      {product.category}
                    </span>
                    <span
                      className={`rounded px-1.5 py-0.5 text-[10px] font-bold ${
                        product.stockQuantity <= product.minStockLevel
                          ? 'bg-rose-100 text-rose-700'
                          : 'bg-emerald-50 text-emerald-700'
                      }`}
                    >
                      {product.stockQuantity} {product.unit}
                    </span>
                  </div>

                  <h4 className="mt-2 text-xs font-bold text-slate-800 line-clamp-2">
                    {isBangla && product.banglaName ? product.banglaName : product.name}
                  </h4>
                  <p className="mt-0.5 font-mono text-[10px] text-slate-400">
                    {product.barcode}
                  </p>
                </div>

                <div className="mt-3 pt-2 border-t border-slate-100">
                  <div className="flex items-baseline justify-between">
                    <span className="text-sm font-black text-slate-900">
                      {Localization.formatCompactCurrency(product.sellingPrice, isBangla)}
                    </span>
                    {canViewMargins && (
                      <span
                        className={`text-[10px] font-semibold ${
                          isLossPricing ? 'text-rose-600' : 'text-emerald-600'
                        }`}
                      >
                        {isLossPricing ? 'Loss ' : 'Profit '}
                        {Localization.formatCompactCurrency(
                          Math.abs(unitProfitOrLoss),
                          isBangla
                        )}
                      </span>
                    )}
                  </div>

                  {inCart && (
                    <div className="mt-1 flex items-center justify-between rounded-lg bg-emerald-50 px-2 py-0.5 text-[11px] font-bold text-emerald-800">
                      <span>{isBangla ? 'কার্টে:' : 'In Cart:'}</span>
                      <span>{inCart.quantity}</span>
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Right Column: Shopping Cart & Order Summary (5 Cols) */}
      <div className="flex flex-col bg-white p-4 lg:col-span-5 xl:col-span-4 h-full overflow-y-auto">
        <div className="flex items-center justify-between border-b pb-3 border-slate-200">
          <div className="flex items-center gap-2">
            <h3 className="text-sm font-bold text-slate-900">{strings.cart}</h3>
            <span className="rounded-full bg-emerald-100 px-2 py-0.5 text-xs font-bold text-emerald-800">
              {cart.length}
            </span>
          </div>
          {cart.length > 0 && (
            <button
              onClick={clearCart}
              className="text-xs font-semibold text-rose-600 hover:text-rose-700"
            >
              {isBangla ? 'কার্ট খালি করুন' : 'Clear All'}
            </button>
          )}
        </div>

        {/* Customer Selector Bar */}
        <div className="mt-3 flex items-center gap-2">
          <div className="flex-1">
            <select
              value={selectedCustomer ? selectedCustomer.id : ''}
              onChange={(e) => {
                const val = e.target.value;
                if (!val) selectCustomer(null);
                else {
                  const found = allCustomers.find((c) => c.id === Number(val));
                  selectCustomer(found || null);
                }
              }}
              className="w-full rounded-xl border border-slate-200 bg-white p-2 text-xs font-medium text-slate-800 focus:border-emerald-500 focus:outline-hidden"
            >
              <option value="">{strings.walkInCustomer}</option>
              {allCustomers.map((cust) => (
                <option key={cust.id} value={cust.id}>
                  {cust.name} ({cust.phone}) - Due: ৳{cust.outstandingDue}
                </option>
              ))}
            </select>
          </div>
          <button
            onClick={() => setShowAddCustomerModal(true)}
            className="flex h-8 w-8 items-center justify-center rounded-xl border border-slate-200 bg-slate-50 text-slate-700 hover:bg-slate-100"
            title="Add New Customer"
          >
            <UserPlus className="h-4 w-4" />
          </button>
        </div>

        {/* Cart Items List */}
        <div className="flex-1 overflow-y-auto my-3 divide-y divide-slate-100 pr-1">
          {cart.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-48 text-center text-xs text-slate-400">
              <Receipt className="h-10 w-10 text-slate-300 mb-2" />
              {strings.emptyCart}
            </div>
          ) : (
            cart.map((item) => (
              <div key={item.productId} className="py-2.5 flex items-center justify-between text-xs">
                <div className="flex-1 pr-2">
                  <p className="font-bold text-slate-800 leading-tight">
                    {isBangla && item.banglaName ? item.banglaName : item.name}
                  </p>
                  <p className="text-[11px] text-slate-400">
                    ৳{item.unitPrice} × {item.quantity} ={' '}
                    <span className="font-bold text-slate-700">
                      ৳{item.unitPrice * item.quantity}
                    </span>
                  </p>
                </div>

                {/* Qty +/- & Delete */}
                <div className="flex items-center gap-1.5">
                  <button
                    onClick={() => updateCartQuantity(item.productId, -1)}
                    className="flex h-7 w-7 items-center justify-center rounded-lg border border-slate-200 bg-slate-50 text-slate-700 hover:bg-slate-100"
                  >
                    <Minus className="h-3.5 w-3.5" />
                  </button>
                  <span className="w-5 text-center font-bold text-slate-800">
                    {item.quantity}
                  </span>
                  <button
                    onClick={() => updateCartQuantity(item.productId, 1)}
                    className="flex h-7 w-7 items-center justify-center rounded-lg border border-slate-200 bg-slate-50 text-slate-700 hover:bg-slate-100"
                  >
                    <Plus className="h-3.5 w-3.5" />
                  </button>
                  <button
                    onClick={() => removeFromCart(item.productId)}
                    className="ml-1 text-slate-400 hover:text-rose-600"
                  >
                    <Trash2 className="h-4 w-4" />
                  </button>
                </div>
              </div>
            ))
          )}
        </div>

        {/* Pricing Breakdown & Checkout Form */}
        <div className="border-t border-slate-200 pt-3 space-y-2 text-xs">
          {/* Subtotal */}
          <div className="flex justify-between text-slate-600">
            <span>{strings.subtotal}:</span>
            <span className="font-bold text-slate-800">
              {Localization.formatCurrency(subtotal, isBangla)}
            </span>
          </div>

          {/* Discount & VAT inline */}
          <div className="grid grid-cols-2 gap-2">
            <div>
              <label className="text-[11px] text-slate-500 block mb-0.5">
                {strings.discount} (৳):
              </label>
              <input
                type="number"
                min="0"
                value={discountAmount || ''}
                onChange={(e) => setDiscountAmount(Math.max(0, Number(e.target.value)))}
                placeholder="0"
                className="w-full rounded-lg border border-slate-200 p-1.5 text-xs focus:border-emerald-500 focus:outline-hidden"
              />
            </div>
            <div>
              <label className="text-[11px] text-slate-500 block mb-0.5">
                {strings.taxVat} (%):
              </label>
              <input
                type="number"
                min="0"
                value={vatPercent || ''}
                onChange={(e) => setVatPercent(Math.max(0, Number(e.target.value)))}
                placeholder="5"
                className="w-full rounded-lg border border-slate-200 p-1.5 text-xs focus:border-emerald-500 focus:outline-hidden"
              />
            </div>
          </div>

          {/* Grand Total */}
          <div className="flex items-baseline justify-between border-t border-slate-100 pt-2 text-sm font-black text-slate-900">
            <span>{strings.grandTotal}:</span>
            <span className="text-base text-emerald-700">
              {Localization.formatCurrency(grandTotal, isBangla)}
            </span>
          </div>

          {/* Payment Method Selector */}
          <div>
            <label className="text-[11px] text-slate-500 block mb-1">
              {strings.paymentMethod}:
            </label>
            <div className="grid grid-cols-3 gap-1.5">
              {PAYMENT_METHODS.slice(0, 6).map((pm) => (
                <button
                  key={pm.key}
                  type="button"
                  onClick={() => setSelectedPaymentMethod(pm.key)}
                  className={`rounded-lg py-1.5 text-[11px] font-semibold border transition ${
                    selectedPaymentMethod === pm.key
                      ? 'border-emerald-600 bg-emerald-600 text-white shadow-xs'
                      : 'border-slate-200 bg-white text-slate-700 hover:bg-slate-50'
                  }`}
                >
                  {isBangla ? pm.labelBn : pm.labelEn}
                </button>
              ))}
            </div>

            {selectedPaymentMethod === 'CARD' && (
              <div className="mt-1.5 flex items-center gap-1.5 rounded-lg bg-blue-50 px-2 py-1 text-[10px] font-semibold text-blue-800 border border-blue-200">
                <CreditCard className="h-3 w-3 text-blue-600" />
                <span>
                  {isBangla ? 'সুরক্ষিত স্ট্রাইপ পেমেন্ট প্রক্সি সক্রিয়' : 'Stripe Server-Side Gateway Active'}
                </span>
              </div>
            )}
            {/* Credit Due Button full width */}
            <button
              type="button"
              onClick={() => setSelectedPaymentMethod('CREDIT_DUE')}
              className={`mt-1.5 w-full rounded-lg py-1.5 text-[11px] font-semibold border transition ${
                selectedPaymentMethod === 'CREDIT_DUE'
                  ? 'border-amber-600 bg-amber-600 text-white shadow-xs'
                  : 'border-amber-200 bg-amber-50 text-amber-800 hover:bg-amber-100'
              }`}
            >
              {isBangla ? 'বাকি খাতা (Credit Due)' : 'Credit Due (Customer Ledger)'}
            </button>
          </div>

          {/* Paid Amount Input */}
          <div className="grid grid-cols-2 gap-2 pt-1">
            <div>
              <label className="text-[11px] text-slate-500 block mb-0.5">
                {strings.paidAmount} (৳):
              </label>
              <input
                type="number"
                min="0"
                value={paidAmount || ''}
                onChange={(e) => setPaidAmount(Number(e.target.value))}
                placeholder={grandTotal.toFixed(0)}
                className="w-full rounded-lg border border-slate-200 p-1.5 text-xs focus:border-emerald-500 focus:outline-hidden"
              />
            </div>
            <div>
              <label className="text-[11px] text-slate-500 block mb-0.5">
                {dueAmount > 0 ? strings.dueAmount : strings.changeAmount}:
              </label>
              <div
                className={`rounded-lg p-1.5 text-xs font-bold ${
                  dueAmount > 0
                    ? 'bg-amber-50 text-amber-700 border border-amber-200'
                    : 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                }`}
              >
                {Localization.formatCompactCurrency(
                  dueAmount > 0 ? dueAmount : changeAmount,
                  isBangla
                )}
              </div>
            </div>
          </div>

          {/* Checkout Button */}
          <button
            type="button"
            onClick={() => checkout(orderNotes)}
            disabled={cart.length === 0}
            className="mt-3 w-full rounded-xl bg-emerald-600 py-3 text-sm font-bold text-white shadow-md shadow-emerald-200 transition hover:bg-emerald-700 disabled:opacity-50"
          >
            {strings.completeSale}
          </button>
        </div>
      </div>

      {/* Quick Add Customer Modal */}
      {showAddCustomerModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4 backdrop-blur-xs">
          <form
            onSubmit={handleCreateCustomer}
            className="w-full max-w-sm rounded-2xl bg-white p-5 shadow-2xl"
          >
            <h3 className="text-sm font-bold text-slate-900 mb-3">
              {isBangla ? 'নতুন খরিদ্দার যুক্ত করুন' : 'Add New Customer'}
            </h3>
            <div className="space-y-3">
              <div>
                <label className="text-xs text-slate-600 block mb-1">
                  {isBangla ? 'নাম:' : 'Customer Name:'}
                </label>
                <input
                  type="text"
                  required
                  value={newCustName}
                  onChange={(e) => setNewCustName(e.target.value)}
                  className="w-full rounded-xl border border-slate-200 p-2 text-xs"
                />
              </div>
              <div>
                <label className="text-xs text-slate-600 block mb-1">
                  {isBangla ? 'মোবাইল নম্বর:' : 'Phone Number:'}
                </label>
                <input
                  type="text"
                  required
                  value={newCustPhone}
                  onChange={(e) => setNewCustPhone(e.target.value)}
                  className="w-full rounded-xl border border-slate-200 p-2 text-xs"
                />
              </div>
              <div>
                <label className="text-xs text-slate-600 block mb-1">
                  {isBangla ? 'ঠিকানা:' : 'Address:'}
                </label>
                <input
                  type="text"
                  value={newCustAddress}
                  onChange={(e) => setNewCustAddress(e.target.value)}
                  className="w-full rounded-xl border border-slate-200 p-2 text-xs"
                />
              </div>
            </div>
            <div className="mt-4 flex justify-end gap-2">
              <button
                type="button"
                onClick={() => setShowAddCustomerModal(false)}
                className="rounded-xl border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-600"
              >
                {strings.cancel}
              </button>
              <button
                type="submit"
                className="rounded-xl bg-emerald-600 px-4 py-1.5 text-xs font-semibold text-white"
              >
                {strings.save}
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
};
