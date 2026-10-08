import React, { useMemo, useState } from 'react';
import { useApp } from '../context/AppContext';
import { Localization } from '../utils/localization';
import { CustomerEntity, SupplierEntity } from '../types';
import {
  Users,
  UserPlus,
  Search,
  Phone,
  MapPin,
  CreditCard,
  Building,
  DollarSign,
  X,
  CheckCircle,
} from 'lucide-react';

export const LedgerScreen: React.FC = () => {
  const {
    allCustomers,
    suppliers,
    addCustomer,
    recordDuePayment,
    addSupplier,
    isBangla,
    strings,
  } = useApp();

  const [activeTab, setActiveTab] = useState<'CUSTOMERS' | 'SUPPLIERS'>('CUSTOMERS');
  const [searchQuery, setSearchQuery] = useState('');
  const [onlyDue, setOnlyDue] = useState(false);

  // Modals
  const [showAddCustomer, setShowAddCustomer] = useState(false);
  const [showAddSupplier, setShowAddSupplier] = useState(false);
  const [paymentCustomer, setPaymentCustomer] = useState<CustomerEntity | null>(null);
  const [paymentAmount, setPaymentAmount] = useState<number>(0);

  // Add Customer State
  const [cName, setCName] = useState('');
  const [cBanglaName, setCBanglaName] = useState('');
  const [cPhone, setCPhone] = useState('');
  const [cAddress, setCAddress] = useState('');

  // Add Supplier State
  const [sName, setSName] = useState('');
  const [sCompany, setSCompany] = useState('');
  const [sPhone, setSPhone] = useState('');
  const [sAddress, setSAddress] = useState('');

  const totalCustomerDue = useMemo(() => {
    return allCustomers.reduce((acc, c) => acc + c.outstandingDue, 0);
  }, [allCustomers]);

  const totalSupplierPayable = useMemo(() => {
    return suppliers.reduce((acc, s) => acc + s.outstandingPayable, 0);
  }, [suppliers]);

  const filteredCustomers = useMemo(() => {
    return allCustomers.filter((c) => {
      const q = searchQuery.trim().toLowerCase();
      const matchSearch =
        !q ||
        c.name.toLowerCase().includes(q) ||
        c.banglaName.toLowerCase().includes(q) ||
        c.phone.toLowerCase().includes(q);

      if (!matchSearch) return false;
      if (onlyDue && c.outstandingDue <= 0) return false;
      return true;
    });
  }, [allCustomers, searchQuery, onlyDue]);

  const filteredSuppliers = useMemo(() => {
    return suppliers.filter((s) => {
      const q = searchQuery.trim().toLowerCase();
      return (
        !q ||
        s.name.toLowerCase().includes(q) ||
        s.companyName.toLowerCase().includes(q) ||
        s.phone.toLowerCase().includes(q)
      );
    });
  }, [suppliers, searchQuery]);

  const handleCreateCustomer = (e: React.FormEvent) => {
    e.preventDefault();
    if (!cName.trim() || !cPhone.trim()) return;

    addCustomer({
      name: cName.trim(),
      banglaName: cBanglaName.trim(),
      phone: cPhone.trim(),
      address: cAddress.trim(),
    });

    setCName('');
    setCBanglaName('');
    setCPhone('');
    setCAddress('');
    setShowAddCustomer(false);
  };

  const handleCreateSupplier = (e: React.FormEvent) => {
    e.preventDefault();
    if (!sName.trim() || !sCompany.trim() || !sPhone.trim()) return;

    addSupplier({
      name: sName.trim(),
      companyName: sCompany.trim(),
      phone: sPhone.trim(),
      address: sAddress.trim(),
    });

    setSName('');
    setSCompany('');
    setSPhone('');
    setSAddress('');
    setShowAddSupplier(false);
  };

  const handleRecordPayment = (e: React.FormEvent) => {
    e.preventDefault();
    if (!paymentCustomer || paymentAmount <= 0) return;

    recordDuePayment(paymentCustomer.id, paymentAmount);
    setPaymentCustomer(null);
    setPaymentAmount(0);
  };

  return (
    <div className="space-y-6 p-4 sm:p-6 lg:p-8">
      {/* Header & Tabs */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 className="text-2xl font-black tracking-tight text-slate-900">
            {strings.ledger}
          </h2>
          <p className="text-xs text-slate-500">
            {isBangla
              ? 'খরিদ্দারদের বাকি খাতা ও সরবরাহকারীদের প্রদেয় দেনা-পাওনা'
              : 'Customer credit ledger, outstanding receivables, and supplier accounts'}
          </p>
        </div>

        <div className="flex items-center gap-2">
          {activeTab === 'CUSTOMERS' ? (
            <button
              onClick={() => setShowAddCustomer(true)}
              className="flex items-center gap-2 rounded-xl bg-emerald-600 px-4 py-2.5 text-xs font-bold text-white shadow-md shadow-emerald-200 transition hover:bg-emerald-700"
            >
              <UserPlus className="h-4 w-4" />
              <span>{isBangla ? 'নতুন খরিদ্দার' : 'Add Customer'}</span>
            </button>
          ) : (
            <button
              onClick={() => setShowAddSupplier(true)}
              className="flex items-center gap-2 rounded-xl bg-emerald-600 px-4 py-2.5 text-xs font-bold text-white shadow-md shadow-emerald-200 transition hover:bg-emerald-700"
            >
              <Building className="h-4 w-4" />
              <span>{isBangla ? 'নতুন সরবরাহকারী' : 'Add Supplier'}</span>
            </button>
          )}
        </div>
      </div>

      {/* Tab Switcher: Customers vs Suppliers */}
      <div className="flex items-center gap-2 border-b border-slate-200">
        <button
          onClick={() => setActiveTab('CUSTOMERS')}
          className={`border-b-2 pb-3 px-2 text-sm font-bold transition ${
            activeTab === 'CUSTOMERS'
              ? 'border-emerald-600 text-emerald-700'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          {isBangla ? 'খরিদ্দার ও বাকি খাতা' : 'Customers (Due Ledger)'} (
          {allCustomers.length})
        </button>
        <button
          onClick={() => setActiveTab('SUPPLIERS')}
          className={`border-b-2 pb-3 px-2 text-sm font-bold transition ${
            activeTab === 'SUPPLIERS'
              ? 'border-emerald-600 text-emerald-700'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          {isBangla ? 'সরবরাহকারী (সাপ্লায়ার)' : 'Suppliers & Vendors'} (
          {suppliers.length})
        </button>
      </div>

      {/* Summary Cards */}
      {activeTab === 'CUSTOMERS' ? (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
          <div className="rounded-2xl border border-amber-200 bg-amber-50/60 p-5 shadow-xs">
            <span className="text-xs font-semibold uppercase tracking-wider text-amber-900">
              {isBangla ? 'মোট খরিদ্দার বাকি (প্রাপ্য)' : 'Total Outstanding Customer Due'}
            </span>
            <p className="mt-2 text-2xl font-black text-amber-700">
              {Localization.formatCurrency(totalCustomerDue, isBangla)}
            </p>
            <p className="mt-1 text-xs text-amber-800">
              {allCustomers.filter((c) => c.outstandingDue > 0).length}{' '}
              {isBangla ? 'জন খরিদ্দারের বাকি আছে' : 'customers with dues'}
            </p>
          </div>
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
          <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-xs">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">
              {isBangla ? 'সাপ্লায়ার দেনা (প্রদেয়)' : 'Total Outstanding Payable'}
            </span>
            <p className="mt-2 text-2xl font-black text-slate-900">
              {Localization.formatCurrency(totalSupplierPayable, isBangla)}
            </p>
            <p className="mt-1 text-xs text-slate-400">
              {suppliers.length} {isBangla ? 'জন সরবরাহকারী' : 'active vendors'}
            </p>
          </div>
        </div>
      )}

      {/* Search and Filters */}
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div className="relative flex-1 max-w-md">
          <Search className="absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder={
              activeTab === 'CUSTOMERS'
                ? isBangla ? 'খরিদ্দারের নাম বা মোবাইল নম্বর খুঁজুন...' : 'Search customer by name or phone...'
                : isBangla ? 'কোম্পানি বা সরবরাহকারী খুঁজুন...' : 'Search supplier name or company...'
            }
            className="w-full rounded-xl border border-slate-200 bg-white py-2 pl-10 pr-4 text-xs shadow-xs focus:border-emerald-500 focus:outline-hidden"
          />
        </div>

        {activeTab === 'CUSTOMERS' && (
          <label className="flex items-center gap-2 text-xs font-semibold text-slate-700 cursor-pointer">
            <input
              type="checkbox"
              checked={onlyDue}
              onChange={(e) => setOnlyDue(e.target.checked)}
              className="rounded border-slate-300 text-emerald-600 focus:ring-emerald-500"
            />
            <span>{isBangla ? 'শুধুমাত্র যাদের বাকি আছে' : 'Only with outstanding dues'}</span>
          </label>
        )}
      </div>

      {/* Main List / Table */}
      {activeTab === 'CUSTOMERS' ? (
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
          {filteredCustomers.map((cust) => (
            <div
              key={cust.id}
              className="flex flex-col justify-between rounded-2xl border border-slate-200 bg-white p-4 shadow-xs hover:border-emerald-300 transition"
            >
              <div>
                <div className="flex items-start justify-between">
                  <div>
                    <h4 className="text-sm font-bold text-slate-900">{cust.name}</h4>
                    {cust.banglaName && (
                      <p className="text-[11px] text-slate-400">{cust.banglaName}</p>
                    )}
                  </div>
                  <span
                    className={`rounded-full px-2 py-0.5 text-xs font-bold ${
                      cust.outstandingDue > 0
                        ? 'bg-amber-100 text-amber-800'
                        : 'bg-emerald-50 text-emerald-800'
                    }`}
                  >
                    Due: ৳{cust.outstandingDue.toFixed(0)}
                  </span>
                </div>

                <div className="mt-3 space-y-1 text-xs text-slate-500">
                  <p className="flex items-center gap-1.5">
                    <Phone className="h-3.5 w-3.5 text-slate-400" />
                    <span>{cust.phone}</span>
                  </p>
                  {cust.address && (
                    <p className="flex items-center gap-1.5">
                      <MapPin className="h-3.5 w-3.5 text-slate-400" />
                      <span>{cust.address}</span>
                    </p>
                  )}
                </div>
              </div>

              <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between">
                <div>
                  <span className="text-[10px] text-slate-400 block uppercase font-semibold">
                    Total Spent
                  </span>
                  <span className="text-xs font-bold text-slate-700">
                    ৳{cust.totalSpent.toFixed(0)}
                  </span>
                </div>

                {cust.outstandingDue > 0 && (
                  <button
                    onClick={() => {
                      setPaymentCustomer(cust);
                      setPaymentAmount(cust.outstandingDue);
                    }}
                    className="rounded-xl bg-emerald-600 px-3 py-1.5 text-xs font-bold text-white shadow-xs hover:bg-emerald-700"
                  >
                    {isBangla ? 'বাকি পরিশোধ' : 'Collect Due'}
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
          {filteredSuppliers.map((sup) => (
            <div
              key={sup.id}
              className="flex flex-col justify-between rounded-2xl border border-slate-200 bg-white p-4 shadow-xs"
            >
              <div>
                <h4 className="text-sm font-bold text-slate-900">{sup.name}</h4>
                <p className="text-xs font-semibold text-emerald-700">{sup.companyName}</p>

                <div className="mt-3 space-y-1 text-xs text-slate-500">
                  <p className="flex items-center gap-1.5">
                    <Phone className="h-3.5 w-3.5 text-slate-400" />
                    <span>{sup.phone}</span>
                  </p>
                  {sup.address && (
                    <p className="flex items-center gap-1.5">
                      <MapPin className="h-3.5 w-3.5 text-slate-400" />
                      <span>{sup.address}</span>
                    </p>
                  )}
                </div>
              </div>

              <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between text-xs">
                <div>
                  <span className="text-[10px] text-slate-400 block uppercase font-semibold">
                    Total Purchased
                  </span>
                  <span className="font-bold text-slate-800">
                    ৳{sup.totalPurchased.toFixed(0)}
                  </span>
                </div>
                <div>
                  <span className="text-[10px] text-slate-400 block uppercase font-semibold">
                    Payable Due
                  </span>
                  <span className="font-bold text-rose-600">
                    ৳{sup.outstandingPayable.toFixed(0)}
                  </span>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Record Due Payment Modal */}
      {paymentCustomer && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4 backdrop-blur-xs">
          <form
            onSubmit={handleRecordPayment}
            className="w-full max-w-sm rounded-2xl bg-white p-5 shadow-2xl"
          >
            <div className="flex items-center justify-between pb-3 border-b mb-3">
              <h3 className="text-sm font-bold text-slate-900">
                {isBangla ? 'বাকি টাকা পরিশোধ গ্রহণ' : 'Receive Due Balance Payment'}
              </h3>
              <button
                type="button"
                onClick={() => setPaymentCustomer(null)}
                className="text-slate-400 hover:text-slate-700"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            <p className="text-xs text-slate-600 mb-1">
              Customer: <span className="font-bold">{paymentCustomer.name}</span>
            </p>
            <p className="text-xs text-amber-700 font-bold mb-4">
              Current Due: ৳{paymentCustomer.outstandingDue.toFixed(2)}
            </p>

            <div>
              <label className="text-xs text-slate-600 font-medium block mb-1">
                {isBangla ? 'গৃহীত টাকা (৳):' : 'Collected Payment Amount (৳):'}
              </label>
              <input
                type="number"
                step="0.01"
                min="1"
                max={paymentCustomer.outstandingDue}
                required
                value={paymentAmount || ''}
                onChange={(e) => setPaymentAmount(Number(e.target.value))}
                className="w-full rounded-xl border border-slate-200 p-2.5 text-xs font-bold text-slate-800 focus:border-emerald-500 focus:outline-hidden"
              />
            </div>

            <div className="mt-5 flex justify-end gap-2 border-t pt-3">
              <button
                type="button"
                onClick={() => setPaymentCustomer(null)}
                className="rounded-xl border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-600"
              >
                {strings.cancel}
              </button>
              <button
                type="submit"
                className="rounded-xl bg-emerald-600 px-4 py-1.5 text-xs font-semibold text-white shadow-xs hover:bg-emerald-700"
              >
                {isBangla ? 'নিশ্চিত করুন' : 'Record Payment'}
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Add Customer Modal */}
      {showAddCustomer && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4 backdrop-blur-xs">
          <form
            onSubmit={handleCreateCustomer}
            className="w-full max-w-sm rounded-2xl bg-white p-5 shadow-2xl"
          >
            <div className="flex items-center justify-between pb-3 border-b mb-3">
              <h3 className="text-sm font-bold text-slate-900">
                {isBangla ? 'নতুন খরিদ্দার যোগ করুন' : 'Add New Customer'}
              </h3>
              <button
                type="button"
                onClick={() => setShowAddCustomer(false)}
                className="text-slate-400 hover:text-slate-700"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            <div className="space-y-3 text-xs">
              <div>
                <label className="text-slate-600 font-medium block mb-1">Name*</label>
                <input
                  type="text"
                  required
                  value={cName}
                  onChange={(e) => setCName(e.target.value)}
                  className="w-full rounded-xl border border-slate-200 p-2 text-xs focus:border-emerald-500 focus:outline-hidden"
                />
              </div>
              <div>
                <label className="text-slate-600 font-medium block mb-1">
                  বাংলা নাম
                </label>
                <input
                  type="text"
                  value={cBanglaName}
                  onChange={(e) => setCBanglaName(e.target.value)}
                  className="w-full rounded-xl border border-slate-200 p-2 text-xs focus:border-emerald-500 focus:outline-hidden"
                />
              </div>
              <div>
                <label className="text-slate-600 font-medium block mb-1">Phone Number*</label>
                <input
                  type="text"
                  required
                  value={cPhone}
                  onChange={(e) => setCPhone(e.target.value)}
                  className="w-full rounded-xl border border-slate-200 p-2 text-xs focus:border-emerald-500 focus:outline-hidden"
                />
              </div>
              <div>
                <label className="text-slate-600 font-medium block mb-1">Address</label>
                <input
                  type="text"
                  value={cAddress}
                  onChange={(e) => setCAddress(e.target.value)}
                  className="w-full rounded-xl border border-slate-200 p-2 text-xs focus:border-emerald-500 focus:outline-hidden"
                />
              </div>
            </div>

            <div className="mt-5 flex justify-end gap-2 border-t pt-3">
              <button
                type="button"
                onClick={() => setShowAddCustomer(false)}
                className="rounded-xl border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-600"
              >
                {strings.cancel}
              </button>
              <button
                type="submit"
                className="rounded-xl bg-emerald-600 px-4 py-1.5 text-xs font-semibold text-white shadow-xs"
              >
                {strings.save}
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Add Supplier Modal */}
      {showAddSupplier && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4 backdrop-blur-xs">
          <form
            onSubmit={handleCreateSupplier}
            className="w-full max-w-sm rounded-2xl bg-white p-5 shadow-2xl"
          >
            <div className="flex items-center justify-between pb-3 border-b mb-3">
              <h3 className="text-sm font-bold text-slate-900">
                {isBangla ? 'নতুন সরবরাহকারী যোগ করুন' : 'Add New Supplier'}
              </h3>
              <button
                type="button"
                onClick={() => setShowAddSupplier(false)}
                className="text-slate-400 hover:text-slate-700"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            <div className="space-y-3 text-xs">
              <div>
                <label className="text-slate-600 font-medium block mb-1">
                  Contact Person Name*
                </label>
                <input
                  type="text"
                  required
                  value={sName}
                  onChange={(e) => setSName(e.target.value)}
                  className="w-full rounded-xl border border-slate-200 p-2 text-xs focus:border-emerald-500 focus:outline-hidden"
                />
              </div>
              <div>
                <label className="text-slate-600 font-medium block mb-1">Company Name*</label>
                <input
                  type="text"
                  required
                  value={sCompany}
                  onChange={(e) => setSCompany(e.target.value)}
                  className="w-full rounded-xl border border-slate-200 p-2 text-xs focus:border-emerald-500 focus:outline-hidden"
                />
              </div>
              <div>
                <label className="text-slate-600 font-medium block mb-1">Phone Number*</label>
                <input
                  type="text"
                  required
                  value={sPhone}
                  onChange={(e) => setSPhone(e.target.value)}
                  className="w-full rounded-xl border border-slate-200 p-2 text-xs focus:border-emerald-500 focus:outline-hidden"
                />
              </div>
              <div>
                <label className="text-slate-600 font-medium block mb-1">Office Address</label>
                <input
                  type="text"
                  value={sAddress}
                  onChange={(e) => setSAddress(e.target.value)}
                  className="w-full rounded-xl border border-slate-200 p-2 text-xs focus:border-emerald-500 focus:outline-hidden"
                />
              </div>
            </div>

            <div className="mt-5 flex justify-end gap-2 border-t pt-3">
              <button
                type="button"
                onClick={() => setShowAddSupplier(false)}
                className="rounded-xl border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-600"
              >
                {strings.cancel}
              </button>
              <button
                type="submit"
                className="rounded-xl bg-emerald-600 px-4 py-1.5 text-xs font-semibold text-white shadow-xs"
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
