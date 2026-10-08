import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { USER_ROLES, UserRole } from '../types';
import {
  Globe,
  Lock,
  Cloud,
  CheckCircle2,
  AlertTriangle,
  RefreshCw,
  Store,
  ChevronDown,
} from 'lucide-react';

export const Header: React.FC = () => {
  const {
    storeConfig,
    isBangla,
    toggleLanguage,
    currentRole,
    switchRole,
    lockTerminal,
    syncState,
    syncWithBackend,
    lowStockProducts,
    currentUser,
  } = useApp();

  const [roleMenuOpen, setRoleMenuOpen] = useState(false);
  const [stockMenuOpen, setStockMenuOpen] = useState(false);

  const roleInfo = USER_ROLES[currentRole];

  return (
    <header className="sticky top-0 z-30 flex h-16 w-full items-center justify-between border-b border-slate-200 bg-white/95 px-4 backdrop-blur-md sm:px-6">
      {/* Brand & Store Name */}
      <div className="flex items-center gap-3">
        <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-emerald-600 font-bold text-white shadow-sm shadow-emerald-200">
          <Store className="h-5 w-5" />
        </div>
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-base font-bold tracking-tight text-slate-900 sm:text-lg">
              {isBangla ? storeConfig.banglaStoreName : storeConfig.storeName}
            </h1>
            <span className="hidden rounded-full bg-emerald-50 px-2 py-0.5 text-xs font-semibold text-emerald-700 sm:inline-block border border-emerald-200/60">
              {storeConfig.outletName}
            </span>
          </div>
          <p className="text-xs text-slate-500">
            {isBangla ? 'স্মার্ট পিওএস ও ইআরপি সিস্টেম' : 'Smart POS & ERP System'}
          </p>
        </div>
      </div>

      {/* Action Controls */}
      <div className="flex items-center gap-2 sm:gap-3">
        {/* Low Stock Indicator */}
        {lowStockProducts.length > 0 && (
          <div className="relative">
            <button
              onClick={() => setStockMenuOpen(!stockMenuOpen)}
              className="flex items-center gap-1.5 rounded-lg border border-amber-200 bg-amber-50 px-2.5 py-1.5 text-xs font-medium text-amber-800 transition hover:bg-amber-100"
              title={`${lowStockProducts.length} items low on stock`}
            >
              <AlertTriangle className="h-4 w-4 text-amber-600 animate-pulse" />
              <span className="hidden sm:inline">
                {isBangla ? 'স্বল্প স্টক:' : 'Low Stock:'}
              </span>
              <span className="rounded-full bg-amber-600 px-1.5 py-0.2 text-[11px] font-bold text-white">
                {lowStockProducts.length}
              </span>
            </button>

            {stockMenuOpen && (
              <div className="absolute right-0 mt-2 w-72 rounded-xl border border-slate-200 bg-white p-3 shadow-xl z-50">
                <div className="mb-2 flex items-center justify-between border-b pb-2">
                  <span className="text-xs font-bold text-slate-800">
                    {isBangla ? 'স্বল্প স্টক সতর্কতা' : 'Low Stock Alerts'}
                  </span>
                  <span className="text-[11px] text-amber-600 font-semibold">
                    {lowStockProducts.length} {isBangla ? 'টি পণ্য' : 'Items'}
                  </span>
                </div>
                <div className="max-h-56 space-y-2 overflow-y-auto pr-1">
                  {lowStockProducts.map((prod) => (
                    <div
                      key={prod.id}
                      className="flex items-center justify-between rounded-lg bg-slate-50 p-2 text-xs"
                    >
                      <div>
                        <p className="font-medium text-slate-800">{prod.name}</p>
                        <p className="text-[10px] text-slate-500">
                          {isBangla ? 'ন্যূনতম সীমা:' : 'Min Threshold:'}{' '}
                          {prod.minStockLevel} {prod.unit}
                        </p>
                      </div>
                      <span className="rounded bg-rose-100 px-2 py-0.5 text-xs font-bold text-rose-700">
                        {prod.stockQuantity} {prod.unit}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}

        {/* Cloud Sync Quick Trigger */}
        <button
          onClick={() => syncWithBackend()}
          disabled={syncState === 'SYNCING'}
          className="flex items-center gap-1.5 rounded-lg border border-slate-200 bg-slate-50 px-2.5 py-1.5 text-xs font-medium text-slate-700 transition hover:bg-slate-100 disabled:opacity-50"
          title={isBangla ? 'স্বয়ংক্রিয় ক্লাউড সিঙ্ক' : 'Sync with Secure Backend Database'}
        >
          {syncState === 'SYNCING' ? (
            <RefreshCw className="h-3.5 w-3.5 animate-spin text-emerald-600" />
          ) : syncState === 'SUCCESS' ? (
            <CheckCircle2 className="h-3.5 w-3.5 text-emerald-600" />
          ) : (
            <Cloud className="h-3.5 w-3.5 text-slate-500" />
          )}
          <span className="hidden sm:inline">
            {syncState === 'SYNCING'
              ? isBangla ? 'সিঙ্ক হচ্ছে...' : 'Syncing...'
              : isBangla ? 'ক্লাউড সিঙ্ক' : 'Cloud Sync'}
          </span>
        </button>

        {/* Current User Email Badge */}
        <div className="hidden lg:flex items-center gap-1.5 rounded-lg border border-slate-200 bg-slate-50 px-2.5 py-1 text-xs text-slate-600 font-mono">
          <div className="h-2 w-2 rounded-full bg-emerald-500" />
          <span className="max-w-[130px] truncate" title={currentUser.email}>
            {currentUser.email}
          </span>
        </div>

        {/* Language Switcher */}
        <button
          onClick={toggleLanguage}
          className="flex items-center gap-1 rounded-lg border border-slate-200 bg-white px-2.5 py-1.5 text-xs font-semibold text-slate-700 shadow-sm transition hover:bg-slate-50"
          title={isBangla ? 'Switch to English' : 'বাংলায় পরিবর্তন করুন'}
        >
          <Globe className="h-3.5 w-3.5 text-emerald-600" />
          <span>{isBangla ? 'English' : 'বাংলা'}</span>
        </button>

        {/* User Role Switcher Dropdown */}
        <div className="relative">
          <button
            onClick={() => setRoleMenuOpen(!roleMenuOpen)}
            className="flex items-center gap-1.5 rounded-lg border border-emerald-200 bg-emerald-50 px-2.5 py-1.5 text-xs font-semibold text-emerald-900 transition hover:bg-emerald-100"
          >
            <span>{isBangla ? roleInfo.titleBn : roleInfo.titleEn}</span>
            <ChevronDown className="h-3.5 w-3.5 opacity-70" />
          </button>

          {roleMenuOpen && (
            <div className="absolute right-0 mt-2 w-48 rounded-xl border border-slate-200 bg-white p-1.5 shadow-xl z-50">
              <div className="px-2 py-1 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
                {isBangla ? 'ভূমিকা পরিবর্তন' : 'Switch Role'}
              </div>
              {(['ADMIN', 'MANAGER', 'CASHIER'] as UserRole[]).map((r) => {
                const info = USER_ROLES[r];
                return (
                  <button
                    key={r}
                    onClick={() => {
                      switchRole(r);
                      setRoleMenuOpen(false);
                    }}
                    className={`flex w-full items-center justify-between rounded-lg px-2.5 py-1.5 text-left text-xs font-medium transition ${
                      currentRole === r
                        ? 'bg-emerald-50 font-bold text-emerald-700'
                        : 'text-slate-700 hover:bg-slate-50'
                    }`}
                  >
                    <span>{isBangla ? info.titleBn : info.titleEn}</span>
                    {currentRole === r && (
                      <CheckCircle2 className="h-3.5 w-3.5 text-emerald-600" />
                    )}
                  </button>
                );
              })}
            </div>
          )}
        </div>

        {/* Lock Terminal Button */}
        <button
          onClick={lockTerminal}
          className="flex items-center justify-center rounded-lg border border-slate-200 bg-slate-100 p-2 text-slate-600 transition hover:bg-rose-50 hover:border-rose-200 hover:text-rose-600"
          title={isBangla ? 'টার্মিনাল লক করুন' : 'Lock POS Terminal'}
        >
          <Lock className="h-4 w-4" />
        </button>
      </div>
    </header>
  );
};
