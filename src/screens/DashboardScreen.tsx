import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { Localization } from '../utils/localization';
import { DateFilter, ScreenNav } from '../types';
import {
  TrendingUp,
  TrendingDown,
  DollarSign,
  ShoppingCart,
  Boxes,
  RotateCcw,
  Wallet,
  CheckCircle2,
  AlertCircle,
  Calendar,
  Clock,
  ArrowUpRight,
  ShieldCheck,
} from 'lucide-react';

interface DashboardScreenProps {
  onNavigate: (screen: ScreenNav) => void;
}

export const DashboardScreen: React.FC<DashboardScreenProps> = ({ onNavigate }) => {
  const {
    financialMetrics,
    dateFilter,
    setDateFilter,
    setCustomDateRange,
    customDateRange,
    filteredSales,
    lowStockProducts,
    adjustStock,
    currentRole,
    isBangla,
    strings,
  } = useApp();

  const [customStart, setCustomStart] = useState<string>(
    new Date(customDateRange[0]).toISOString().slice(0, 10)
  );
  const [customEnd, setCustomEnd] = useState<string>(
    new Date(customDateRange[1]).toISOString().slice(0, 10)
  );
  const [showCustomModal, setShowCustomModal] = useState(false);

  const canViewMargins = currentRole === 'ADMIN' || currentRole === 'MANAGER';

  const marginPercent =
    financialMetrics.grossRevenue > 0
      ? ((financialMetrics.netProfit / financialMetrics.grossRevenue) * 100).toFixed(1)
      : '0.0';

  const handleApplyCustomDate = () => {
    const s = new Date(customStart).getTime();
    const e = new Date(customEnd).setHours(23, 59, 59, 999);
    setCustomDateRange(s, e);
    setShowCustomModal(false);
  };

  return (
    <div className="space-y-6 p-4 sm:p-6 lg:p-8">
      {/* Top Banner: Greetings & Date Range Filter */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 className="text-2xl font-black tracking-tight text-slate-900">
            {isBangla ? 'ব্যবসায়িক ড্যাশবোর্ড ও বিশ্লেষণ' : 'Business Dashboard & Analytics'}
          </h2>
          <p className="text-xs text-slate-500">
            {isBangla
              ? 'দৈনিক ও মাসিক আয়-ব্যয় এবং স্বয়ংক্রিয় ক্ষতি কাভারেজ রিপোর্ট'
              : 'Real-time sales, expenses, and automated profit/loss coverage logic'}
          </p>
        </div>

        {/* Date Filter Chips */}
        <div className="flex flex-wrap items-center gap-1.5 rounded-xl border border-slate-200 bg-white p-1 shadow-xs">
          {(
            [
              { key: 'TODAY', labelEn: 'Today', labelBn: 'আজকের' },
              { key: 'DAYS_7', labelEn: '7 Days', labelBn: '৭ দিন' },
              { key: 'DAYS_30', labelEn: '30 Days', labelBn: '৩০ দিন' },
              { key: 'CUSTOM', labelEn: 'Custom', labelBn: 'কাস্টম' },
            ] as { key: DateFilter; labelEn: string; labelBn: string }[]
          ).map((item) => (
            <button
              key={item.key}
              onClick={() => {
                if (item.key === 'CUSTOM') {
                  setShowCustomModal(true);
                } else {
                  setDateFilter(item.key);
                }
              }}
              className={`rounded-lg px-3 py-1.5 text-xs font-semibold transition ${
                dateFilter === item.key
                  ? 'bg-emerald-600 text-white shadow-xs'
                  : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
              }`}
            >
              {isBangla ? item.labelBn : item.labelEn}
            </button>
          ))}
        </div>
      </div>

      {/* Primary Financial Metric Cards */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {/* Net Revenue */}
        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-xs">
          <div className="flex items-center justify-between text-slate-500">
            <span className="text-xs font-semibold uppercase tracking-wider">
              {strings.netRevenue}
            </span>
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-emerald-50 text-emerald-600">
              <DollarSign className="h-4 w-4" />
            </div>
          </div>
          <div className="mt-3">
            <p className="text-2xl font-black text-slate-900">
              {Localization.formatCurrency(financialMetrics.grossRevenue, isBangla)}
            </p>
            <p className="mt-1 flex items-center text-xs text-slate-500">
              <span className="font-semibold text-emerald-600 mr-1">
                {financialMetrics.totalOrders}
              </span>{' '}
              {isBangla ? 'টি ইনভয়েস সম্পন্ন' : 'orders processed'}
            </p>
          </div>
        </div>

        {/* Current Profit (with Loss Covered Logic) */}
        <div className="rounded-2xl border border-emerald-100 bg-linear-to-br from-emerald-50/50 to-white p-5 shadow-xs">
          <div className="flex items-center justify-between text-emerald-800">
            <span className="text-xs font-semibold uppercase tracking-wider">
              {strings.currentProfit}
            </span>
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-emerald-100 text-emerald-700">
              <TrendingUp className="h-4 w-4" />
            </div>
          </div>
          <div className="mt-3">
            <p className="text-2xl font-black text-emerald-700">
              {Localization.formatCurrency(financialMetrics.currentProfit, isBangla)}
            </p>
            <div className="mt-1 flex items-center gap-1.5">
              {financialMetrics.isLossCovered ? (
                <span className="inline-flex items-center gap-1 rounded-full bg-emerald-100 px-2 py-0.5 text-[10px] font-bold text-emerald-800">
                  <ShieldCheck className="h-3 w-3" />
                  {strings.lossCoveredBadge}
                </span>
              ) : (
                <span className="inline-flex items-center gap-1 rounded-full bg-rose-100 px-2 py-0.5 text-[10px] font-bold text-rose-800">
                  <AlertCircle className="h-3 w-3" />
                  {strings.uncoveredLossBadge}
                </span>
              )}
            </div>
          </div>
        </div>

        {/* Current Loss */}
        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-xs">
          <div className="flex items-center justify-between text-slate-500">
            <span className="text-xs font-semibold uppercase tracking-wider">
              {strings.currentLoss}
            </span>
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-rose-50 text-rose-600">
              <TrendingDown className="h-4 w-4" />
            </div>
          </div>
          <div className="mt-3">
            <p
              className={`text-2xl font-black ${
                financialMetrics.currentLoss > 0 ? 'text-rose-600' : 'text-slate-400'
              }`}
            >
              {Localization.formatCurrency(financialMetrics.currentLoss, isBangla)}
            </p>
            <p className="mt-1 text-xs text-slate-500">
              {financialMetrics.isLossCovered
                ? isBangla ? 'বিক্রয়ের লাভ দ্বারা সম্পূর্ণ কাভার্ড' : 'All losses fully absorbed'
                : isBangla ? 'অবশিষ্ট ক্ষতি সমন্বয় প্রয়োজন' : 'Requires margin absorption'}
            </p>
          </div>
        </div>

        {/* Final Net Profit */}
        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-xs">
          <div className="flex items-center justify-between text-slate-500">
            <span className="text-xs font-semibold uppercase tracking-wider">
              {strings.netProfit}
            </span>
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-slate-100 text-slate-800">
              <ArrowUpRight className="h-4 w-4" />
            </div>
          </div>
          <div className="mt-3">
            <p
              className={`text-2xl font-black ${
                financialMetrics.netProfit >= 0 ? 'text-slate-900' : 'text-rose-600'
              }`}
            >
              {Localization.formatCurrency(financialMetrics.netProfit, isBangla)}
            </p>
            <p className="mt-1 text-xs text-slate-500">
              {strings.estimatedMargin}:{' '}
              <span className="font-bold text-slate-800">{marginPercent}%</span>
            </p>
          </div>
        </div>
      </div>

      {/* Secondary Financial Indicators Bar */}
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        <div className="rounded-xl border border-slate-200 bg-white p-3.5">
          <span className="text-[11px] font-medium text-slate-500">{strings.totalExpenses}</span>
          <p className="mt-1 text-base font-bold text-slate-800">
            {Localization.formatCurrency(financialMetrics.totalExpenses, isBangla)}
          </p>
        </div>
        <div className="rounded-xl border border-slate-200 bg-white p-3.5">
          <span className="text-[11px] font-medium text-slate-500">{strings.totalRefunds}</span>
          <p className="mt-1 text-base font-bold text-slate-800">
            {Localization.formatCurrency(financialMetrics.totalRefunds, isBangla)}
          </p>
        </div>
        <div className="rounded-xl border border-slate-200 bg-white p-3.5">
          <span className="text-[11px] font-medium text-slate-500">{strings.dueAmount}</span>
          <p className="mt-1 text-base font-bold text-amber-600">
            {Localization.formatCurrency(financialMetrics.totalOutstandingDue, isBangla)}
          </p>
        </div>
        <div className="rounded-xl border border-slate-200 bg-white p-3.5">
          <span className="text-[11px] font-medium text-slate-500">
            {isBangla ? 'গ্রস সেলস প্রফিট / লস' : 'Gross Profit / Loss'}
          </span>
          <p className="mt-1 text-xs font-semibold text-slate-700">
            <span className="text-emerald-600 font-bold">
              +{Localization.formatCompactCurrency(financialMetrics.grossSalesProfit, isBangla)}
            </span>{' '}
            /{' '}
            <span className="text-rose-600 font-bold">
              -{Localization.formatCompactCurrency(financialMetrics.grossSalesLoss, isBangla)}
            </span>
          </p>
        </div>
      </div>

      {/* Quick Launchpad Buttons */}
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        <button
          onClick={() => onNavigate('pos')}
          className="flex items-center gap-3 rounded-2xl border border-emerald-300 bg-emerald-600 p-4 text-left text-white shadow-md shadow-emerald-200 transition hover:bg-emerald-700"
        >
          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-white/20">
            <ShoppingCart className="h-5 w-5" />
          </div>
          <div>
            <p className="text-sm font-bold">{isBangla ? 'নতুন বিক্রয় (POS)' : 'New Sale (POS)'}</p>
            <p className="text-[11px] text-emerald-100">{isBangla ? 'ক্যাশিয়ার রেজিস্টার' : 'Checkout & Print'}</p>
          </div>
        </button>

        <button
          onClick={() => onNavigate('inventory')}
          className="flex items-center gap-3 rounded-2xl border border-slate-200 bg-white p-4 text-left shadow-xs transition hover:bg-slate-50"
        >
          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-blue-50 text-blue-600">
            <Boxes className="h-5 w-5" />
          </div>
          <div>
            <p className="text-sm font-bold text-slate-900">{isBangla ? 'স্টক ব্যবস্থাপনা' : 'Stock Inventory'}</p>
            <p className="text-[11px] text-slate-500">{isBangla ? 'পণ্য ও মূল্য নির্ধারণ' : 'SKU & Pricing'}</p>
          </div>
        </button>

        <button
          onClick={() => onNavigate('expenses')}
          className="flex items-center gap-3 rounded-2xl border border-slate-200 bg-white p-4 text-left shadow-xs transition hover:bg-slate-50"
        >
          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-purple-50 text-purple-600">
            <Wallet className="h-5 w-5" />
          </div>
          <div>
            <p className="text-sm font-bold text-slate-900">{isBangla ? 'খরচ রেকর্ড' : 'Add Expense'}</p>
            <p className="text-[11px] text-slate-500">{isBangla ? 'দোকান ভাড়া, বিল ইত্যাদি' : 'Rent, utility, bill'}</p>
          </div>
        </button>

        <button
          onClick={() => onNavigate('returns')}
          className="flex items-center gap-3 rounded-2xl border border-slate-200 bg-white p-4 text-left shadow-xs transition hover:bg-slate-50"
        >
          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-amber-50 text-amber-600">
            <RotateCcw className="h-5 w-5" />
          </div>
          <div>
            <p className="text-sm font-bold text-slate-900">{isBangla ? 'পণ্য ফেরত ও বদল' : 'Return & Exchange'}</p>
            <p className="text-[11px] text-slate-500">{isBangla ? 'ইনভয়েস থেকে ফেরত' : 'Refund & replacement'}</p>
          </div>
        </button>
      </div>

      {/* Two Column Layout: Low Stock Warning + Recent Transactions */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Low Stock Alerts (1 Col) */}
        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-xs">
          <div className="flex items-center justify-between pb-3 border-b border-slate-100">
            <div className="flex items-center gap-2">
              <AlertCircle className="h-5 w-5 text-amber-600" />
              <h3 className="text-sm font-bold text-slate-900">
                {strings.lowStockAlert}
              </h3>
            </div>
            <span className="rounded-full bg-amber-100 px-2 py-0.5 text-xs font-bold text-amber-800">
              {lowStockProducts.length}
            </span>
          </div>

          <div className="mt-3 divide-y divide-slate-100">
            {lowStockProducts.length === 0 ? (
              <div className="py-6 text-center text-xs text-slate-400">
                <CheckCircle2 className="mx-auto h-8 w-8 text-emerald-500/50 mb-1" />
                {isBangla ? 'সকল পণ্যের পর্যাপ্ত স্টক রয়েছে!' : 'All stock levels are healthy!'}
              </div>
            ) : (
              lowStockProducts.slice(0, 5).map((p) => (
                <div key={p.id} className="flex items-center justify-between py-2.5 text-xs">
                  <div>
                    <p className="font-semibold text-slate-800">{p.name}</p>
                    <p className="text-[11px] text-slate-400">
                      {isBangla ? 'মজুদ:' : 'In stock:'}{' '}
                      <span className="font-bold text-rose-600">{p.stockQuantity}</span> / Min{' '}
                      {p.minStockLevel} {p.unit}
                    </p>
                  </div>
                  <button
                    onClick={() => adjustStock(p.id, 10)}
                    className="rounded-lg bg-emerald-50 px-2 py-1 text-[11px] font-bold text-emerald-700 hover:bg-emerald-100"
                    title="Quick restock +10"
                  >
                    +10 {isBangla ? 'যুক্ত' : 'Restock'}
                  </button>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Recent Transactions (2 Cols) */}
        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-xs lg:col-span-2">
          <div className="flex items-center justify-between pb-3 border-b border-slate-100">
            <h3 className="text-sm font-bold text-slate-900">
              {isBangla ? 'সাম্প্রতিক বিক্রয় লেনদেন' : 'Recent Sales Transactions'}
            </h3>
            <button
              onClick={() => onNavigate('reports')}
              className="text-xs font-semibold text-emerald-600 hover:text-emerald-700"
            >
              {isBangla ? 'সকল দেখুন →' : 'View All →'}
            </button>
          </div>

          <div className="mt-3 overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead>
                <tr className="border-b border-slate-100 text-[11px] uppercase tracking-wider text-slate-400">
                  <th className="py-2">Invoice</th>
                  <th className="py-2">Customer</th>
                  <th className="py-2">Payment</th>
                  <th className="py-2 text-right">Payable</th>
                  <th className="py-2 text-right">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-50">
                {filteredSales.slice(0, 5).map((sale) => (
                  <tr key={sale.id} className="hover:bg-slate-50/50">
                    <td className="py-2.5 font-mono font-semibold text-slate-800">
                      {sale.invoiceNumber}
                    </td>
                    <td className="py-2.5 text-slate-600">{sale.customerName}</td>
                    <td className="py-2.5">
                      <span className="rounded bg-slate-100 px-2 py-0.5 text-[10px] font-semibold text-slate-700">
                        {sale.paymentMethod}
                      </span>
                    </td>
                    <td className="py-2.5 text-right font-bold text-slate-900">
                      {Localization.formatCurrency(sale.netPayable, isBangla)}
                    </td>
                    <td className="py-2.5 text-right">
                      <span
                        className={`rounded-full px-2 py-0.5 text-[10px] font-bold ${
                          sale.dueAmount > 0
                            ? 'bg-amber-100 text-amber-800'
                            : 'bg-emerald-100 text-emerald-800'
                        }`}
                      >
                        {sale.dueAmount > 0
                          ? isBangla ? 'বাকি আছে' : 'Due Balance'
                          : isBangla ? 'পরিশোধিত' : 'Paid'}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* Custom Date Modal */}
      {showCustomModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4 backdrop-blur-xs">
          <div className="w-full max-w-sm rounded-2xl bg-white p-5 shadow-xl">
            <h3 className="text-sm font-bold text-slate-900 mb-3">
              {isBangla ? 'কাস্টম তারিখ সীমা নির্বাচন' : 'Select Custom Date Range'}
            </h3>
            <div className="space-y-3">
              <div>
                <label className="text-xs text-slate-600 block mb-1">
                  {isBangla ? 'শুরুর তারিখ:' : 'Start Date:'}
                </label>
                <input
                  type="date"
                  value={customStart}
                  onChange={(e) => setCustomStart(e.target.value)}
                  className="w-full rounded-xl border border-slate-200 p-2 text-xs"
                />
              </div>
              <div>
                <label className="text-xs text-slate-600 block mb-1">
                  {isBangla ? 'শেষের তারিখ:' : 'End Date:'}
                </label>
                <input
                  type="date"
                  value={customEnd}
                  onChange={(e) => setCustomEnd(e.target.value)}
                  className="w-full rounded-xl border border-slate-200 p-2 text-xs"
                />
              </div>
            </div>
            <div className="mt-4 flex justify-end gap-2">
              <button
                onClick={() => setShowCustomModal(false)}
                className="rounded-xl border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-600"
              >
                {strings.cancel}
              </button>
              <button
                onClick={handleApplyCustomDate}
                className="rounded-xl bg-emerald-600 px-4 py-1.5 text-xs font-semibold text-white"
              >
                {strings.confirm}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
