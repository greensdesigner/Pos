import React from 'react';
import { ScreenNav } from '../types';
import { useApp } from '../context/AppContext';
import {
  LayoutDashboard,
  ShoppingCart,
  Boxes,
  RotateCcw,
  Wallet,
  Users,
  BarChart3,
  Settings,
} from 'lucide-react';

interface SidebarProps {
  currentScreen: ScreenNav;
  onNavigate: (screen: ScreenNav) => void;
}

export const Sidebar: React.FC<SidebarProps> = ({ currentScreen, onNavigate }) => {
  const { isBangla, lowStockProducts, cart } = useApp();

  const navItems: {
    id: ScreenNav;
    labelEn: string;
    labelBn: string;
    icon: React.FC<{ className?: string }>;
    badge?: number;
  }[] = [
    {
      id: 'dashboard',
      labelEn: 'Dashboard',
      labelBn: 'ড্যাশবোর্ড',
      icon: LayoutDashboard,
    },
    {
      id: 'pos',
      labelEn: 'POS',
      labelBn: 'বিক্রয় (পিওএস)',
      icon: ShoppingCart,
      badge: cart.length > 0 ? cart.length : undefined,
    },
    {
      id: 'inventory',
      labelEn: 'Inventory',
      labelBn: 'স্টক / মজুদ',
      icon: Boxes,
      badge: lowStockProducts.length > 0 ? lowStockProducts.length : undefined,
    },
    {
      id: 'returns',
      labelEn: 'Returns',
      labelBn: 'ফেরত ও বদল',
      icon: RotateCcw,
    },
    {
      id: 'expenses',
      labelEn: 'Expenses',
      labelBn: 'খরচ হিসাব',
      icon: Wallet,
    },
    {
      id: 'ledger',
      labelEn: 'Due Ledger',
      labelBn: 'বাকি খাতা',
      icon: Users,
    },
    {
      id: 'reports',
      labelEn: 'Reports',
      labelBn: 'রিপোর্ট',
      icon: BarChart3,
    },
    {
      id: 'settings',
      labelEn: 'Settings',
      labelBn: 'সেটিংস',
      icon: Settings,
    },
  ];

  return (
    <>
      {/* Desktop / Tablet Vertical Sidebar */}
      <aside className="hidden md:flex h-[calc(100vh-4rem)] w-64 flex-col justify-between border-r border-slate-200 bg-white p-3 shrink-0">
        <div className="space-y-1">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = currentScreen === item.id;
            return (
              <button
                key={item.id}
                onClick={() => onNavigate(item.id)}
                className={`flex w-full items-center justify-between rounded-xl px-3.5 py-2.5 text-sm font-medium transition ${
                  isActive
                    ? 'bg-emerald-600 text-white shadow-sm shadow-emerald-200'
                    : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
                }`}
              >
                <div className="flex items-center gap-3">
                  <Icon className={`h-5 w-5 ${isActive ? 'text-white' : 'text-slate-500'}`} />
                  <span>{isBangla ? item.labelBn : item.labelEn}</span>
                </div>
                {item.badge !== undefined && (
                  <span
                    className={`rounded-full px-2 py-0.5 text-xs font-bold ${
                      isActive
                        ? 'bg-white text-emerald-700'
                        : item.id === 'inventory'
                        ? 'bg-rose-100 text-rose-700'
                        : 'bg-emerald-100 text-emerald-800'
                    }`}
                  >
                    {item.badge}
                  </span>
                )}
              </button>
            );
          })}
        </div>

        {/* Footer info in sidebar */}
        <div className="rounded-xl border border-slate-100 bg-slate-50/70 p-3 text-xs text-slate-500">
          <div className="flex items-center justify-between font-semibold text-slate-700">
            <span>GreensStock POS</span>
            <span className="rounded bg-emerald-100 px-1.5 py-0.5 text-[10px] font-bold text-emerald-800">
              v1.0.0
            </span>
          </div>
          <p className="mt-1 text-[11px] text-slate-400">
            {isBangla ? 'অফলাইন ও অনলাইন হাইব্রিড সিস্টেম' : 'Offline-first Enterprise ERP'}
          </p>
        </div>
      </aside>

      {/* Mobile Bottom Navigation Bar */}
      <nav className="fixed bottom-0 left-0 right-0 z-40 flex h-16 items-center justify-around border-t border-slate-200 bg-white/95 px-2 backdrop-blur-md md:hidden">
        {navItems.slice(0, 5).map((item) => {
          const Icon = item.icon;
          const isActive = currentScreen === item.id;
          return (
            <button
              key={item.id}
              onClick={() => onNavigate(item.id)}
              className={`flex flex-col items-center justify-center p-1.5 transition ${
                isActive ? 'text-emerald-600 font-bold' : 'text-slate-500 hover:text-slate-900'
              }`}
            >
              <div className="relative">
                <Icon className="h-5 w-5" />
                {item.badge !== undefined && (
                  <span className="absolute -right-2 -top-1 flex h-4 w-4 items-center justify-center rounded-full bg-rose-600 text-[10px] font-bold text-white">
                    {item.badge}
                  </span>
                )}
              </div>
              <span className="mt-1 text-[10px] leading-tight">
                {isBangla ? item.labelBn : item.labelEn}
              </span>
            </button>
          );
        })}
        {/* Overflow Menu for remaining screens on mobile */}
        <button
          onClick={() => onNavigate('settings')}
          className={`flex flex-col items-center justify-center p-1.5 transition ${
            ['ledger', 'reports', 'settings'].includes(currentScreen)
              ? 'text-emerald-600 font-bold'
              : 'text-slate-500 hover:text-slate-900'
          }`}
        >
          <Settings className="h-5 w-5" />
          <span className="mt-1 text-[10px] leading-tight">
            {isBangla ? 'আরও' : 'More'}
          </span>
        </button>
      </nav>
    </>
  );
};
