import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { Lock, Delete, ShieldAlert } from 'lucide-react';
import { USER_ROLES } from '../types';

export const LockScreenModal: React.FC = () => {
  const { isTerminalLocked, unlockTerminal, currentRole, isBangla } = useApp();
  const [pin, setPin] = useState('');
  const [error, setError] = useState(false);

  if (!isTerminalLocked) return null;

  const roleInfo = USER_ROLES[currentRole];

  const handleDigit = (digit: string) => {
    if (pin.length < 6) {
      setPin((prev) => prev + digit);
      setError(false);
    }
  };

  const handleBackspace = () => {
    setPin((prev) => prev.slice(0, -1));
    setError(false);
  };

  const handleClear = () => {
    setPin('');
    setError(false);
  };

  const handleSubmit = (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    const success = unlockTerminal(pin);
    if (!success) {
      setError(true);
      setPin('');
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/80 p-4 backdrop-blur-md">
      <div className="w-full max-w-sm rounded-3xl border border-slate-700 bg-slate-900 p-6 text-white shadow-2xl">
        <div className="flex flex-col items-center text-center">
          <div className="mb-3 flex h-16 w-16 items-center justify-center rounded-2xl bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
            <Lock className="h-8 w-8" />
          </div>

          <h2 className="text-xl font-bold tracking-tight">
            {isBangla ? 'টার্মিনাল লক করা' : 'POS Terminal Locked'}
          </h2>
          <p className="mt-1 text-xs text-slate-400">
            {isBangla
              ? `${roleInfo.titleBn} হিসেবে আনলক করতে পিন দিন`
              : `Enter PIN to unlock as ${roleInfo.titleEn}`}
          </p>

          {/* PIN Dots Display */}
          <div className="my-6 flex items-center justify-center gap-3">
            {[0, 1, 2, 3].map((index) => (
              <div
                key={index}
                className={`h-4 w-4 rounded-full border transition-all ${
                  index < pin.length
                    ? 'border-emerald-500 bg-emerald-500 shadow-md shadow-emerald-500/50 scale-110'
                    : 'border-slate-600 bg-slate-800'
                }`}
              />
            ))}
          </div>

          {error && (
            <div className="mb-4 flex items-center gap-1.5 text-xs font-semibold text-rose-400">
              <ShieldAlert className="h-4 w-4" />
              <span>
                {isBangla ? 'ভুল পিন! আবার চেষ্টা করুন (ডিফল্ট: 1234)' : 'Incorrect PIN! Try again (Default: 1234)'}
              </span>
            </div>
          )}

          {/* Keypad */}
          <div className="grid w-full grid-cols-3 gap-3">
            {['1', '2', '3', '4', '5', '6', '7', '8', '9'].map((digit) => (
              <button
                key={digit}
                type="button"
                onClick={() => handleDigit(digit)}
                className="flex h-14 items-center justify-center rounded-2xl bg-slate-800 text-xl font-bold text-white transition hover:bg-slate-700 active:scale-95"
              >
                {digit}
              </button>
            ))}

            <button
              type="button"
              onClick={handleClear}
              className="flex h-14 items-center justify-center rounded-2xl bg-slate-800/60 text-xs font-semibold text-slate-400 transition hover:bg-slate-700 active:scale-95"
            >
              {isBangla ? 'মুছুন' : 'CLR'}
            </button>

            <button
              type="button"
              onClick={() => handleDigit('0')}
              className="flex h-14 items-center justify-center rounded-2xl bg-slate-800 text-xl font-bold text-white transition hover:bg-slate-700 active:scale-95"
            >
              0
            </button>

            <button
              type="button"
              onClick={handleBackspace}
              className="flex h-14 items-center justify-center rounded-2xl bg-slate-800/60 text-slate-300 transition hover:bg-slate-700 active:scale-95"
            >
              <Delete className="h-6 w-6" />
            </button>
          </div>

          {/* Submit Action */}
          <button
            type="button"
            onClick={() => handleSubmit()}
            disabled={pin.length < 4}
            className="mt-5 w-full rounded-2xl bg-emerald-600 py-3 text-sm font-bold text-white shadow-lg shadow-emerald-900/40 transition hover:bg-emerald-500 disabled:opacity-50"
          >
            {isBangla ? 'টার্মিনাল আনলক করুন' : 'Unlock Terminal'}
          </button>
        </div>
      </div>
    </div>
  );
};
