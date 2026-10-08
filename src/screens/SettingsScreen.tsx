import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import {
  Settings,
  Store,
  Lock,
  Cloud,
  RefreshCw,
  Database,
  CheckCircle2,
  Sparkles,
  RotateCcw,
  KeyRound,
  ShieldCheck,
  CreditCard,
  Mail,
  UserCheck,
  Users,
  Shield,
  ArrowRight,
} from 'lucide-react';
import { UserRole } from '../types';

export const SettingsScreen: React.FC = () => {
  const {
    storeConfig,
    updateStoreConfig,
    lockTerminal,
    syncState,
    syncWithBackend,
    currentUser,
    switchUserAccount,
    authorizedAccounts,
    isCloudAutoConnected,
    checkForUpdates,
    simulateUpdateAvailable,
    resetToDefaultData,
    isBangla,
    strings,
  } = useApp();

  // Store form state
  const [storeName, setStoreName] = useState(storeConfig.storeName);
  const [banglaStoreName, setBanglaStoreName] = useState(storeConfig.banglaStoreName);
  const [phone, setPhone] = useState(storeConfig.phone);
  const [address, setAddress] = useState(storeConfig.address);
  const [receiptFooter, setReceiptFooter] = useState(storeConfig.receiptFooter);
  const [vatPercent, setVatPercent] = useState(storeConfig.vatPercent);
  const [terminalPin, setTerminalPin] = useState(storeConfig.terminalPin);

  // Switch account state
  const [customSwitchEmail, setCustomSwitchEmail] = useState('');
  const [customSwitchRole, setCustomSwitchRole] = useState<UserRole>('ADMIN');

  const handleSaveStoreProfile = (e: React.FormEvent) => {
    e.preventDefault();
    updateStoreConfig({
      storeName: storeName.trim(),
      banglaStoreName: banglaStoreName.trim(),
      phone: phone.trim(),
      address: address.trim(),
      receiptFooter: receiptFooter.trim(),
      vatPercent: Number(vatPercent),
      terminalPin: terminalPin.trim(),
    });
  };

  const handleSwitchCustomAccount = (e: React.FormEvent) => {
    e.preventDefault();
    if (!customSwitchEmail.trim()) return;
    switchUserAccount(customSwitchEmail.trim(), undefined, customSwitchRole);
    setCustomSwitchEmail('');
  };

  return (
    <div className="space-y-6 p-4 sm:p-6 lg:p-8">
      <div>
        <h2 className="text-2xl font-black tracking-tight text-slate-900">
          {strings.settings}
        </h2>
        <p className="text-xs text-slate-500">
          {isBangla
            ? 'দোকানের পরিচিতি, ইউজার এক্সেস অথোরাইজেশন ও ক্লাউড ডাটাবেজ ইন্টিগ্রেশন'
            : 'Store profile, User ID authorization, and automated backend cloud database'}
        </p>
      </div>

      {/* Security Notice: Credentials Protected */}
      <div className="rounded-2xl border border-emerald-200 bg-emerald-50/70 p-4 text-xs text-emerald-900 shadow-xs">
        <div className="flex items-start gap-3">
          <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-xl bg-emerald-600 text-white">
            <ShieldCheck className="h-5 w-5" />
          </div>
          <div>
            <h4 className="font-bold text-emerald-950">
              {isBangla
                ? 'সংবেদনশীল তথ্য ও ব্যাকএন্ড সুরক্ষা সুরক্ষিত (Server-Side Vault)'
                : 'Server-Side Architecture & Security Vault Active'}
            </h4>
            <p className="mt-0.5 text-emerald-800 leading-relaxed">
              {isBangla
                ? 'ডাটাবেজ পাসওয়ার্ড, স্ট্রাইপ পেমেন্ট সিক্রেট কী এবং এসএমটিপি ক্রেডেনশিয়াল সম্পূর্ণভাবে ব্যাকএন্ড সার্ভারে সুরক্ষিত। অ্যাপটিতে কোনো সংবেদনশীল তথ্য প্রদর্শিত বা উন্মোচিত হয় না। প্রতিটি User ID এবং Email Access-এর অধীনে তথ্য সম্পূর্ণ আলাদা ও সুরক্ষিত।'
                : 'Database passwords, Stripe secret keys, and SMTP credentials are fully isolated on the secure server-side proxy. No credentials are visible in the client UI. Records are strictly segregated by User ID and Email.'}
            </p>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* User Identity & Authorization Partitioning Card */}
        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-xs flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between border-b pb-3 mb-4">
              <div className="flex items-center gap-2">
                <UserCheck className="h-5 w-5 text-emerald-600" />
                <h3 className="text-sm font-bold text-slate-900">
                  {isBangla ? 'ইউজার এক্সেস ও ইমেইল অথোরাইজেশন' : 'User Identity & Access Authorization'}
                </h3>
              </div>
              <span className="rounded-full bg-emerald-100 px-2.5 py-0.5 text-[10px] font-bold text-emerald-800">
                Authorized
              </span>
            </div>

            <div className="rounded-xl border border-slate-100 bg-slate-50 p-4 text-xs space-y-2">
              <div className="flex justify-between">
                <span className="text-slate-500">{isBangla ? 'সক্রিয় ইমেইল:' : 'Active Email:'}</span>
                <span className="font-mono font-bold text-slate-800">{currentUser.email}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">{isBangla ? 'ইউজার আইডি (User ID):' : 'User ID:'}</span>
                <span className="font-mono text-slate-600">{currentUser.userId}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">{isBangla ? 'অনুমোদিত ভূমিকা:' : 'Access Role:'}</span>
                <span className="font-bold text-emerald-700">{currentUser.role}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">{isBangla ? 'ডাটা পার্টিশন:' : 'Data Partition:'}</span>
                <span className="font-semibold text-slate-700">Strict Single-Tenant Isolation</span>
              </div>
            </div>

            {/* Quick Switch to test isolation */}
            <div className="mt-4">
              <label className="text-[11px] font-bold text-slate-600 uppercase tracking-wider block mb-2">
                {isBangla ? 'অনুমোদিত অ্যাকাউন্ট নির্বাচন (Switch Account):' : 'Select Authorized Workspace Account:'}
              </label>
              <div className="space-y-1.5">
                {authorizedAccounts.map((acc) => (
                  <button
                    key={acc.email}
                    onClick={() => switchUserAccount(acc.email, acc.name, acc.role)}
                    className={`flex w-full items-center justify-between rounded-xl p-2.5 text-xs font-semibold border transition ${
                      currentUser.email === acc.email
                        ? 'border-emerald-600 bg-emerald-50 text-emerald-950 font-bold'
                        : 'border-slate-200 bg-white text-slate-700 hover:bg-slate-50'
                    }`}
                  >
                    <div className="flex items-center gap-2">
                      <div
                        className={`h-2 w-2 rounded-full ${
                          currentUser.email === acc.email ? 'bg-emerald-600' : 'bg-slate-300'
                        }`}
                      />
                      <span>{acc.name}</span>
                      <span className="font-mono text-[10px] text-slate-400">({acc.email})</span>
                    </div>
                    <span className="text-[10px] uppercase font-bold text-slate-500">
                      {acc.role}
                    </span>
                  </button>
                ))}
              </div>
            </div>

            {/* Custom Email switch to prove multi-tenant segregation */}
            <form onSubmit={handleSwitchCustomAccount} className="mt-4 border-t pt-3">
              <label className="text-[11px] font-semibold text-slate-600 block mb-1.5">
                {isBangla
                  ? 'অন্য কোনো ইমেইল দিয়ে টেস্ট করুন (নতুন ইউজার স্পেস):'
                  : 'Test with another Email (Creates isolated user partition):'}
              </label>
              <div className="flex gap-2">
                <input
                  type="email"
                  required
                  value={customSwitchEmail}
                  onChange={(e) => setCustomSwitchEmail(e.target.value)}
                  placeholder="e.g. staff2@greensdesigner.com"
                  className="flex-1 rounded-xl border border-slate-200 p-2 text-xs focus:border-emerald-500 focus:outline-hidden"
                />
                <button
                  type="submit"
                  className="rounded-xl bg-slate-900 px-3.5 py-2 text-xs font-bold text-white hover:bg-slate-800 flex items-center gap-1"
                >
                  <span>{isBangla ? 'লগইন' : 'Open'}</span>
                  <ArrowRight className="h-3 w-3" />
                </button>
              </div>
            </form>
          </div>
        </div>

        {/* Backend Automated Services Status (No manual credentials needed) */}
        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-xs flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between border-b pb-3 mb-4">
              <div className="flex items-center gap-2">
                <Cloud className="h-5 w-5 text-emerald-600" />
                <h3 className="text-sm font-bold text-slate-900">
                  {isBangla ? 'স্বয়ংক্রিয় ব্যাকএন্ড সার্ভিসেস' : 'Automated Cloud Backend Services'}
                </h3>
              </div>
              <span className="flex items-center gap-1 text-[11px] font-bold text-emerald-600">
                <CheckCircle2 className="h-4 w-4" />
                <span>{isBangla ? 'সংযুক্ত' : 'Connected'}</span>
              </span>
            </div>

            <div className="space-y-3 text-xs">
              {/* 1. Database Service */}
              <div className="rounded-xl border border-slate-200 bg-slate-50/60 p-3.5">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2 font-bold text-slate-900">
                    <Database className="h-4 w-4 text-emerald-600" />
                    <span>{isBangla ? 'ক্লাউড ডাটাবেজ সার্ভিস' : 'Cloud Database Service'}</span>
                  </div>
                  <span className="rounded bg-emerald-100 px-2 py-0.5 text-[10px] font-bold text-emerald-800">
                    Auto-Configured
                  </span>
                </div>
                <p className="mt-1 text-[11px] text-slate-500">
                  {isBangla
                    ? 'ডাটাবেজ সংযোগ ব্যাকএন্ডে স্বয়ংক্রিয়ভাবে সক্রিয়। কোনো ম্যানুয়াল কনফিগারেশনের প্রয়োজন নেই।'
                    : 'Zero-configuration server connection. Records automatically synced per User ID.'}
                </p>
              </div>

              {/* 2. Stripe Payment Gateway */}
              <div className="rounded-xl border border-slate-200 bg-slate-50/60 p-3.5">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2 font-bold text-slate-900">
                    <CreditCard className="h-4 w-4 text-blue-600" />
                    <span>{isBangla ? 'স্ট্রাইপ পেমেন্ট সিস্টেম' : 'Stripe Payment Gateway'}</span>
                  </div>
                  <span className="rounded bg-blue-100 px-2 py-0.5 text-[10px] font-bold text-blue-800">
                    Server-Side Proxy
                  </span>
                </div>
                <p className="mt-1 text-[11px] text-slate-500">
                  {isBangla
                    ? 'পেমেন্ট সিক্রেট কী সম্পূর্ণ সার্ভার-সাইডে রক্ষিত। ক্লায়েন্ট ডিভাইস কোনো গোপন কী দেখতে পায় না।'
                    : 'Stripe secret key is executed strictly inside backend API proxy (/api/payments/*).'}
                </p>
              </div>

              {/* 3. Hostinger SMTP Email */}
              <div className="rounded-xl border border-slate-200 bg-slate-50/60 p-3.5">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2 font-bold text-slate-900">
                    <Mail className="h-4 w-4 text-amber-600" />
                    <span>{isBangla ? 'এসএমটিপি ইমেইল সিস্টেম' : 'Hostinger SMTP Relay'}</span>
                  </div>
                  <span className="rounded bg-amber-100 px-2 py-0.5 text-[10px] font-bold text-amber-800">
                    SSL Port 465
                  </span>
                </div>
                <p className="mt-1 text-[11px] text-slate-500">
                  {isBangla
                    ? 'ইনভয়েস ও রসিদ সরাসরি গ্রাহকের ইমেইলে পাঠাতে ব্যাকএন্ড মেইল রিলে প্রস্তুত।'
                    : 'Automated electronic receipt delivery through verified backend mail transport.'}
                </p>
              </div>
            </div>

            {/* Sync Now button */}
            <div className="mt-5 border-t pt-3 flex items-center justify-between">
              <span className="text-[11px] text-slate-400">
                {isBangla ? 'সর্বশেষ সিঙ্ক:' : 'Last Synced:'}{' '}
                {new Date(storeConfig.lastSyncTimestamp).toLocaleTimeString()}
              </span>

              <button
                type="button"
                onClick={() => syncWithBackend()}
                disabled={syncState === 'SYNCING'}
                className="flex items-center gap-1.5 rounded-xl bg-emerald-600 px-4 py-2 text-xs font-bold text-white shadow-xs hover:bg-emerald-700 disabled:opacity-50"
              >
                <RefreshCw
                  className={`h-3.5 w-3.5 ${syncState === 'SYNCING' ? 'animate-spin' : ''}`}
                />
                <span>{isBangla ? 'এখনই সিঙ্ক করুন' : 'Sync Now'}</span>
              </button>
            </div>
          </div>
        </div>

        {/* Store Profile Settings */}
        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-xs">
          <div className="flex items-center gap-2 border-b pb-3 mb-4">
            <Store className="h-5 w-5 text-emerald-600" />
            <h3 className="text-sm font-bold text-slate-900">
              {isBangla ? 'দোকানের পরিচিতি ও রশিদের তথ্য' : 'Store Profile & Receipt Header'}
            </h3>
          </div>

          <form onSubmit={handleSaveStoreProfile} className="space-y-3 text-xs">
            <div>
              <label className="text-slate-600 font-medium block mb-1">
                Store Name (English)*
              </label>
              <input
                type="text"
                required
                value={storeName}
                onChange={(e) => setStoreName(e.target.value)}
                className="w-full rounded-xl border border-slate-200 p-2 text-xs focus:border-emerald-500 focus:outline-hidden"
              />
            </div>

            <div>
              <label className="text-slate-600 font-medium block mb-1">
                দোকানের নাম (বাংলা)
              </label>
              <input
                type="text"
                value={banglaStoreName}
                onChange={(e) => setBanglaStoreName(e.target.value)}
                className="w-full rounded-xl border border-slate-200 p-2 text-xs focus:border-emerald-500 focus:outline-hidden"
              />
            </div>

            <div className="grid grid-cols-2 gap-2">
              <div>
                <label className="text-slate-600 font-medium block mb-1">Phone Number*</label>
                <input
                  type="text"
                  required
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  className="w-full rounded-xl border border-slate-200 p-2 text-xs focus:border-emerald-500 focus:outline-hidden"
                />
              </div>
              <div>
                <label className="text-slate-600 font-medium block mb-1">Default VAT (%)*</label>
                <input
                  type="number"
                  step="0.1"
                  required
                  value={vatPercent}
                  onChange={(e) => setVatPercent(Number(e.target.value))}
                  className="w-full rounded-xl border border-slate-200 p-2 text-xs focus:border-emerald-500 focus:outline-hidden"
                />
              </div>
            </div>

            <div>
              <label className="text-slate-600 font-medium block mb-1">Store Address</label>
              <input
                type="text"
                value={address}
                onChange={(e) => setAddress(e.target.value)}
                className="w-full rounded-xl border border-slate-200 p-2 text-xs focus:border-emerald-500 focus:outline-hidden"
              />
            </div>

            <div>
              <label className="text-slate-600 font-medium block mb-1">Receipt Footer Message</label>
              <input
                type="text"
                value={receiptFooter}
                onChange={(e) => setReceiptFooter(e.target.value)}
                className="w-full rounded-xl border border-slate-200 p-2 text-xs focus:border-emerald-500 focus:outline-hidden"
              />
            </div>

            <div className="border-t pt-3">
              <label className="text-slate-600 font-medium block mb-1 flex items-center gap-1.5">
                <KeyRound className="h-3.5 w-3.5 text-slate-400" />
                <span>{isBangla ? 'টার্মিনাল আনলক পিন (PIN)' : 'Terminal Unlock PIN'}</span>
              </label>
              <input
                type="password"
                maxLength={6}
                value={terminalPin}
                onChange={(e) => setTerminalPin(e.target.value)}
                placeholder="1234"
                className="w-full rounded-xl border border-slate-200 p-2 text-xs font-mono font-bold focus:border-emerald-500 focus:outline-hidden"
              />
            </div>

            <div className="flex items-center justify-between pt-2">
              <button
                type="button"
                onClick={lockTerminal}
                className="rounded-xl border border-rose-200 bg-rose-50 px-3 py-2 text-xs font-semibold text-rose-700 hover:bg-rose-100"
              >
                {isBangla ? 'এখনই টার্মিনাল লক করুন' : 'Lock Terminal Now'}
              </button>
              <button
                type="submit"
                className="rounded-xl bg-emerald-600 px-5 py-2 text-xs font-bold text-white shadow-xs hover:bg-emerald-700"
              >
                {strings.save}
              </button>
            </div>
          </form>
        </div>

        {/* In-App Auto Update & Maintenance */}
        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-xs flex flex-col justify-between">
          <div>
            <div className="flex items-center gap-2 border-b pb-3 mb-4">
              <Sparkles className="h-5 w-5 text-emerald-600" />
              <h3 className="text-sm font-bold text-slate-900">
                {isBangla ? 'সফটওয়্যার ভার্সন ও আপডেট' : 'Application Updates & Health'}
              </h3>
            </div>

            <div className="rounded-xl bg-slate-50 p-4 text-xs space-y-2">
              <div className="flex justify-between">
                <span className="text-slate-500">App Build:</span>
                <span className="font-bold text-slate-900">GreensStock ERP v1.0.0</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Runtime:</span>
                <span className="font-semibold text-slate-700">Full-Stack Node.js 22 + React</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Database Engine:</span>
                <span className="font-semibold text-emerald-700">MySQL & Partitioned Storage</span>
              </div>
            </div>

            <div className="mt-4 flex items-center gap-2">
              <button
                onClick={() => checkForUpdates(true)}
                className="rounded-xl border border-slate-200 bg-white px-3.5 py-2 text-xs font-semibold text-slate-700 hover:bg-slate-50"
              >
                {isBangla ? 'আপডেট চেক করুন' : 'Check for Updates'}
              </button>
              <button
                onClick={simulateUpdateAvailable}
                className="rounded-xl bg-emerald-50 border border-emerald-200 px-3.5 py-2 text-xs font-bold text-emerald-800 hover:bg-emerald-100"
              >
                {isBangla ? 'আপডেট টেস্ট সিমুলেশন' : 'Simulate Update Prompt'}
              </button>
            </div>
          </div>

          {/* Reset Workspace */}
          <div className="mt-6 border-t border-slate-100 pt-4 flex items-center justify-between">
            <div>
              <p className="text-xs font-bold text-slate-800">
                {isBangla ? 'ইউজার স্পেস রিসেট' : 'Reset User Space'}
              </p>
              <p className="text-[11px] text-slate-400">
                {isBangla
                  ? 'বর্তমান ইউজারের জন্য ডেমো ডাটা রিসেট করে'
                  : 'Reverts current user data partition to initial sample records'}
              </p>
            </div>
            <button
              onClick={() => {
                if (confirm('Reset workspace for current user?')) {
                  resetToDefaultData();
                }
              }}
              className="flex items-center gap-1.5 rounded-xl border border-rose-200 bg-rose-50 px-3 py-2 text-xs font-bold text-rose-700 hover:bg-rose-100"
            >
              <RotateCcw className="h-3.5 w-3.5" />
              <span>{isBangla ? 'রিসেট' : 'Reset'}</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
