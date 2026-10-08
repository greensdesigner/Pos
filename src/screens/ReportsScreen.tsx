import React, { useMemo, useState } from 'react';
import { useApp } from '../context/AppContext';
import { Localization } from '../utils/localization';
import {
  BarChart3,
  Printer,
  Copy,
  Check,
  Share2,
  FileText,
  ShieldCheck,
  AlertCircle,
  TrendingUp,
  DollarSign,
  ArrowUpRight,
} from 'lucide-react';

export const ReportsScreen: React.FC = () => {
  const {
    financialMetrics,
    storeConfig,
    dateFilter,
    setDateFilter,
    isBangla,
    strings,
  } = useApp();

  const [copied, setCopied] = useState(false);

  const reportText = useMemo(() => {
    const lines = [
      '==========================================',
      isBangla ? storeConfig.banglaStoreName : storeConfig.storeName,
      isBangla
        ? 'আয় ও ব্যয়ের পূর্ণাঙ্গ আর্থিক বিবরণী (Income Statement)'
        : 'Comprehensive Profit & Loss Income Statement',
      '==========================================',
      `Gross Sales Revenue: ${Localization.formatCurrency(financialMetrics.grossRevenue, isBangla)}`,
      `Gross Sales Profit: ${Localization.formatCurrency(financialMetrics.grossSalesProfit, isBangla)}`,
      `Gross Sales Loss: ${Localization.formatCurrency(financialMetrics.grossSalesLoss, isBangla)}`,
      '------------------------------------------',
      `Current Profit (Loss-Covered): ${Localization.formatCurrency(financialMetrics.currentProfit, isBangla)}`,
      `Current Loss: ${Localization.formatCurrency(financialMetrics.currentLoss, isBangla)} ${
        financialMetrics.isLossCovered ? '[LOSS COVERED ✓]' : '[UNCOVERED LOSS]'
      }`,
      '------------------------------------------',
      `Total Operating Expenses: -${Localization.formatCurrency(financialMetrics.totalExpenses, isBangla)}`,
      `Total Returns / Refunds: -${Localization.formatCurrency(financialMetrics.totalRefunds, isBangla)}`,
      '==========================================',
      `FINAL NET PROFIT: ${Localization.formatCurrency(financialMetrics.netProfit, isBangla)}`,
      `Total Outstanding Customer Dues: ${Localization.formatCurrency(financialMetrics.totalOutstandingDue, isBangla)}`,
      `Total Completed Orders: ${financialMetrics.totalOrders}`,
      '==========================================',
      `Generated at: ${new Date().toLocaleString()}`,
    ];
    return lines.join('\n');
  }, [financialMetrics, storeConfig, isBangla]);

  const handleCopy = () => {
    navigator.clipboard.writeText(reportText);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handlePrint = () => {
    window.print();
  };

  return (
    <div className="space-y-6 p-4 sm:p-6 lg:p-8">
      {/* Header & Print / Export actions */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 className="text-2xl font-black tracking-tight text-slate-900">
            {strings.reports}
          </h2>
          <p className="text-xs text-slate-500">
            {isBangla
              ? 'আইনগত ও হিসাববিজ্ঞান মানসম্পন্ন লাভ-ক্ষতি ও আর্থিক হিসাব বিবরণী'
              : 'Audited P&L income statement, loss coverage audit, and financial ledger'}
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={handleCopy}
            className="flex items-center gap-1.5 rounded-xl border border-slate-200 bg-white px-3.5 py-2 text-xs font-semibold text-slate-700 shadow-xs hover:bg-slate-50"
          >
            {copied ? (
              <>
                <Check className="h-4 w-4 text-emerald-600" />
                <span>{isBangla ? 'কপি হয়েছে' : 'Copied'}</span>
              </>
            ) : (
              <>
                <Copy className="h-4 w-4 text-slate-500" />
                <span>{isBangla ? 'কপি রিপোর্ট' : 'Copy Text'}</span>
              </>
            )}
          </button>

          <button
            onClick={handlePrint}
            className="flex items-center gap-1.5 rounded-xl bg-slate-900 px-4 py-2 text-xs font-semibold text-white shadow-xs hover:bg-slate-800"
          >
            <Printer className="h-4 w-4" />
            <span>{isBangla ? 'প্রিন্ট রিপোর্ট' : 'Print Statement'}</span>
          </button>
        </div>
      </div>

      {/* Date Filter selector */}
      <div className="flex items-center gap-2">
        <span className="text-xs font-semibold text-slate-600">
          {isBangla ? 'রিপোর্ট সময়কাল:' : 'Period:'}
        </span>
        <div className="flex gap-1.5">
          {(['TODAY', 'DAYS_7', 'DAYS_30'] as const).map((key) => (
            <button
              key={key}
              onClick={() => setDateFilter(key)}
              className={`rounded-lg px-3 py-1 text-xs font-semibold transition ${
                dateFilter === key
                  ? 'bg-emerald-600 text-white'
                  : 'bg-white text-slate-600 border border-slate-200 hover:bg-slate-50'
              }`}
            >
              {key === 'TODAY'
                ? strings.filterToday
                : key === 'DAYS_7'
                ? strings.filter7Days
                : strings.filter30Days}
            </button>
          ))}
        </div>
      </div>

      {/* Visual Summary Cards */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-xs">
          <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">
            {strings.netRevenue}
          </span>
          <p className="mt-2 text-2xl font-black text-slate-900">
            {Localization.formatCurrency(financialMetrics.grossRevenue, isBangla)}
          </p>
          <p className="mt-1 text-xs text-slate-400">
            {financialMetrics.totalOrders} {isBangla ? 'টি লেনদেন' : 'orders'}
          </p>
        </div>

        <div className="rounded-2xl border border-emerald-100 bg-emerald-50/50 p-5 shadow-xs">
          <span className="text-xs font-semibold uppercase tracking-wider text-emerald-800">
            {strings.currentProfit}
          </span>
          <p className="mt-2 text-2xl font-black text-emerald-700">
            {Localization.formatCurrency(financialMetrics.currentProfit, isBangla)}
          </p>
          <div className="mt-1 flex items-center gap-1.5">
            {financialMetrics.isLossCovered ? (
              <span className="inline-flex items-center gap-1 text-[11px] font-bold text-emerald-800">
                <ShieldCheck className="h-3.5 w-3.5 text-emerald-600" />
                {strings.lossCoveredBadge}
              </span>
            ) : (
              <span className="inline-flex items-center gap-1 text-[11px] font-bold text-rose-700">
                <AlertCircle className="h-3.5 w-3.5 text-rose-600" />
                {strings.uncoveredLossBadge}
              </span>
            )}
          </div>
        </div>

        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-xs">
          <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">
            {strings.netProfit}
          </span>
          <p
            className={`mt-2 text-2xl font-black ${
              financialMetrics.netProfit >= 0 ? 'text-slate-900' : 'text-rose-600'
            }`}
          >
            {Localization.formatCurrency(financialMetrics.netProfit, isBangla)}
          </p>
          <p className="mt-1 text-xs text-slate-400">
            {isBangla ? 'পরিচালন ব্যয় ও রিফান্ড বাদের পর' : 'After expenses and customer returns'}
          </p>
        </div>
      </div>

      {/* Structured Income Statement Card */}
      <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-xs">
        <div className="flex items-center gap-2 border-b pb-4 mb-4">
          <FileText className="h-5 w-5 text-emerald-600" />
          <h3 className="text-base font-bold text-slate-900">
            {isBangla ? 'আর্থিক আয়-ব্যয় বিবরণী (Income Statement)' : 'Financial Income Statement'}
          </h3>
        </div>

        <div className="space-y-3 text-xs">
          <div className="flex justify-between py-1 text-slate-700">
            <span className="font-semibold">Gross Sales Revenue (বিক্রয় রাজস্ব):</span>
            <span className="font-mono font-bold">
              {Localization.formatCurrency(financialMetrics.grossRevenue, isBangla)}
            </span>
          </div>

          <div className="flex justify-between py-1 text-slate-600 pl-4">
            <span>• Gross Sales Profit (বিক্রয় লাভ):</span>
            <span className="font-mono text-emerald-600 font-semibold">
              +{Localization.formatCurrency(financialMetrics.grossSalesProfit, isBangla)}
            </span>
          </div>

          <div className="flex justify-between py-1 text-slate-600 pl-4">
            <span>• Gross Sales Loss (বিক্রয় ক্ষতি / ছাড়):</span>
            <span className="font-mono text-rose-600 font-semibold">
              -{Localization.formatCurrency(financialMetrics.grossSalesLoss, isBangla)}
            </span>
          </div>

          <div className="flex justify-between py-2 border-y border-slate-100 bg-slate-50/50 px-2 rounded-lg font-bold">
            <span className="text-emerald-900">
              Current Profit (ক্ষতি কাভার্ড লাভ):
            </span>
            <span className="font-mono text-emerald-700">
              {Localization.formatCurrency(financialMetrics.currentProfit, isBangla)}
            </span>
          </div>

          <div className="flex justify-between py-1 text-slate-700">
            <span>Less: Operating Expenses (পরিচালন ব্যয়):</span>
            <span className="font-mono text-rose-600 font-bold">
              -{Localization.formatCurrency(financialMetrics.totalExpenses, isBangla)}
            </span>
          </div>

          <div className="flex justify-between py-1 text-slate-700">
            <span>Less: Customer Returns & Refunds (পণ্য ফেরত):</span>
            <span className="font-mono text-rose-600 font-bold">
              -{Localization.formatCurrency(financialMetrics.totalRefunds, isBangla)}
            </span>
          </div>

          <div className="flex justify-between py-3 border-t-2 border-slate-900 mt-2 font-black text-sm">
            <span className="text-slate-900">FINAL NET PROFIT (নিট লাভ):</span>
            <span
              className={`font-mono text-base ${
                financialMetrics.netProfit >= 0 ? 'text-emerald-700' : 'text-rose-600'
              }`}
            >
              {Localization.formatCurrency(financialMetrics.netProfit, isBangla)}
            </span>
          </div>

          <div className="flex justify-between py-1 text-amber-700 border-t border-slate-100 pt-2 font-medium">
            <span>Customer Outstanding Due (বাকি খাতা প্রাপ্য):</span>
            <span className="font-mono font-bold">
              {Localization.formatCurrency(financialMetrics.totalOutstandingDue, isBangla)}
            </span>
          </div>
        </div>
      </div>

      {/* Raw Monospace Text Preview for Auditing */}
      <div className="rounded-2xl border border-slate-200 bg-slate-900 p-5 text-slate-200 shadow-xs">
        <div className="flex items-center justify-between pb-3 border-b border-slate-800 mb-3">
          <span className="font-mono text-xs font-semibold text-slate-400">
            AUDIT STATEMENT (MONOSPACE)
          </span>
          <button
            onClick={handleCopy}
            className="text-xs text-emerald-400 hover:text-emerald-300 font-semibold"
          >
            {copied ? 'Copied!' : 'Copy Text'}
          </button>
        </div>
        <pre className="font-mono text-xs leading-relaxed overflow-x-auto whitespace-pre-wrap">
          {reportText}
        </pre>
      </div>
    </div>
  );
};
