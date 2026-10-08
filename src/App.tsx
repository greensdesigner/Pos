import React, { useState } from 'react';
import { AppProvider, useApp } from './context/AppContext';
import { Header } from './components/Header';
import { Sidebar } from './components/Sidebar';
import { InvoiceModal } from './components/InvoiceModal';
import { LockScreenModal } from './components/LockScreenModal';
import { AppUpdateModal } from './components/AppUpdateModal';
import { ScreenNav } from './types';

import { DashboardScreen } from './screens/DashboardScreen';
import { PosScreen } from './screens/PosScreen';
import { InventoryScreen } from './screens/InventoryScreen';
import { ReturnsScreen } from './screens/ReturnsScreen';
import { ExpensesScreen } from './screens/ExpensesScreen';
import { LedgerScreen } from './screens/LedgerScreen';
import { ReportsScreen } from './screens/ReportsScreen';
import { SettingsScreen } from './screens/SettingsScreen';

const MainLayout: React.FC = () => {
  const [currentScreen, setCurrentScreen] = useState<ScreenNav>('dashboard');
  const { statusMessage, setStatusMessage } = useApp();

  return (
    <div className="flex h-screen w-screen flex-col overflow-hidden bg-slate-50 font-sans text-slate-900">
      {/* Top Header */}
      <Header />

      {/* Main Body Area: Sidebar + Screen Content */}
      <div className="flex flex-1 overflow-hidden">
        <Sidebar currentScreen={currentScreen} onNavigate={setCurrentScreen} />

        <main className="flex-1 overflow-y-auto pb-20 md:pb-6">
          {currentScreen === 'dashboard' && (
            <DashboardScreen onNavigate={setCurrentScreen} />
          )}
          {currentScreen === 'pos' && <PosScreen />}
          {currentScreen === 'inventory' && <InventoryScreen />}
          {currentScreen === 'returns' && <ReturnsScreen />}
          {currentScreen === 'expenses' && <ExpensesScreen />}
          {currentScreen === 'ledger' && <LedgerScreen />}
          {currentScreen === 'reports' && <ReportsScreen />}
          {currentScreen === 'settings' && <SettingsScreen />}
        </main>
      </div>

      {/* Global Toast Feedback */}
      {statusMessage && (
        <div className="fixed bottom-20 left-1/2 z-50 -translate-x-1/2 rounded-full border border-slate-700 bg-slate-900/95 px-5 py-2.5 text-xs font-semibold text-white shadow-xl backdrop-blur-md md:bottom-6 animate-bounce">
          <div className="flex items-center gap-2">
            <span>{statusMessage}</span>
            <button
              onClick={() => setStatusMessage(null)}
              className="ml-2 text-slate-400 hover:text-white"
            >
              ✕
            </button>
          </div>
        </div>
      )}

      {/* Modals & Overlays */}
      <InvoiceModal />
      <LockScreenModal />
      <AppUpdateModal />
    </div>
  );
};

export const App: React.FC = () => {
  return (
    <AppProvider>
      <MainLayout />
    </AppProvider>
  );
};

export default App;
