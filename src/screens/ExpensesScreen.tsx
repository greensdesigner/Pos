import React, { useMemo, useState } from 'react';
import { useApp } from '../context/AppContext';
import { Localization } from '../utils/localization';
import { EXPENSE_CATEGORIES, ExpenseCategory, USER_ROLES } from '../types';
import {
  Wallet,
  Plus,
  Trash2,
  Calendar,
  X,
  CreditCard,
  Building,
  DollarSign,
} from 'lucide-react';

export const ExpensesScreen: React.FC = () => {
  const {
    expenses,
    addExpense,
    deleteExpense,
    currentRole,
    isBangla,
    strings,
  } = useApp();

  const [selectedCategory, setSelectedCategory] = useState<string>('ALL');
  const [showAddModal, setShowAddModal] = useState(false);

  // New Expense form state
  const [title, setTitle] = useState('');
  const [category, setCategory] = useState<ExpenseCategory>('RENT');
  const [amount, setAmount] = useState<number>(0);
  const [paymentMethod, setPaymentMethod] = useState('CASH');
  const [notes, setNotes] = useState('');

  const roleInfo = USER_ROLES[currentRole];

  const totalExpense = useMemo(() => {
    return expenses.reduce((acc, e) => acc + e.amount, 0);
  }, [expenses]);

  const filteredExpenses = useMemo(() => {
    if (selectedCategory === 'ALL') return expenses;
    return expenses.filter((e) => e.category === selectedCategory);
  }, [expenses, selectedCategory]);

  const handleAddExpense = (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || amount <= 0) return;

    addExpense({
      title: title.trim(),
      category,
      amount,
      paymentMethod,
      notes: notes.trim(),
    });

    setTitle('');
    setAmount(0);
    setNotes('');
    setShowAddModal(false);
  };

  return (
    <div className="space-y-6 p-4 sm:p-6 lg:p-8">
      {/* Header & Add Button */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 className="text-2xl font-black tracking-tight text-slate-900">
            {strings.expenses}
          </h2>
          <p className="text-xs text-slate-500">
            {isBangla
              ? 'দোকান ভাড়া, কর্মচারীর বেতন, বিদ্যুৎ ও আনুষঙ্গিক পরিচালন ব্যয় হিসাব'
              : 'Record store rent, payroll, utilities, and daily operations costs'}
          </p>
        </div>

        <button
          onClick={() => setShowAddModal(true)}
          className="flex items-center gap-2 rounded-xl bg-emerald-600 px-4 py-2.5 text-xs font-bold text-white shadow-md shadow-emerald-200 transition hover:bg-emerald-700"
        >
          <Plus className="h-4 w-4" />
          <span>{isBangla ? 'নতুন খরচ যোগ করুন' : 'Record Expense'}</span>
        </button>
      </div>

      {/* Summary Stat Card */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-xs">
          <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">
            {strings.totalExpenses}
          </span>
          <p className="mt-2 text-2xl font-black text-rose-600">
            {Localization.formatCurrency(totalExpense, isBangla)}
          </p>
          <p className="mt-1 text-xs text-slate-400">
            {expenses.length} {isBangla ? 'টি ভাউচার এন্ট্রি' : 'recorded transactions'}
          </p>
        </div>
      </div>

      {/* Category Filter Pills */}
      <div className="flex gap-1.5 overflow-x-auto pb-1">
        <button
          onClick={() => setSelectedCategory('ALL')}
          className={`rounded-lg px-3 py-1.5 text-xs font-semibold whitespace-nowrap transition ${
            selectedCategory === 'ALL'
              ? 'bg-slate-900 text-white'
              : 'bg-white text-slate-600 border border-slate-200 hover:bg-slate-50'
          }`}
        >
          {strings.filterAll}
        </button>
        {EXPENSE_CATEGORIES.map((cat) => (
          <button
            key={cat.key}
            onClick={() => setSelectedCategory(cat.key)}
            className={`rounded-lg px-3 py-1.5 text-xs font-semibold whitespace-nowrap transition ${
              selectedCategory === cat.key
                ? 'bg-emerald-600 text-white'
                : 'bg-white text-slate-600 border border-slate-200 hover:bg-slate-50'
            }`}
          >
            {isBangla ? cat.labelBn : cat.labelEn}
          </button>
        ))}
      </div>

      {/* Expenses Table */}
      <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-xs">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="border-b border-slate-200 bg-slate-50 text-[11px] font-bold uppercase tracking-wider text-slate-500">
              <tr>
                <th className="px-4 py-3">Title & Voucher</th>
                <th className="px-4 py-3">Category</th>
                <th className="px-4 py-3">Method</th>
                <th className="px-4 py-3">Date</th>
                <th className="px-4 py-3 text-right">Amount</th>
                <th className="px-4 py-3 text-right">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {filteredExpenses.length === 0 ? (
                <tr>
                  <td colSpan={6} className="py-8 text-center text-slate-400">
                    {isBangla ? 'কোনো খরচ পাওয়া যায়নি' : 'No expenses found in this filter'}
                  </td>
                </tr>
              ) : (
                filteredExpenses.map((exp) => {
                  const catInfo = EXPENSE_CATEGORIES.find((c) => c.key === exp.category);
                  return (
                    <tr key={exp.id} className="hover:bg-slate-50/70">
                      <td className="px-4 py-3 font-semibold text-slate-800">
                        {exp.title}
                        {exp.notes && (
                          <span className="block text-[11px] text-slate-400 font-normal">
                            {exp.notes}
                          </span>
                        )}
                      </td>
                      <td className="px-4 py-3">
                        <span className="rounded bg-slate-100 px-2 py-0.5 text-[11px] font-medium text-slate-700">
                          {isBangla && catInfo ? catInfo.labelBn : catInfo?.labelEn || exp.category}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-slate-500">{exp.paymentMethod}</td>
                      <td className="px-4 py-3 text-slate-500">
                        {Localization.formatDate(exp.timestamp, isBangla)}
                      </td>
                      <td className="px-4 py-3 text-right font-black text-rose-600">
                        {Localization.formatCurrency(exp.amount, isBangla)}
                      </td>
                      <td className="px-4 py-3 text-right">
                        {roleInfo.canDeleteRecords && (
                          <button
                            onClick={() => {
                              if (confirm(`Delete "${exp.title}"?`)) deleteExpense(exp.id);
                            }}
                            className="p-1 text-slate-400 hover:text-rose-600"
                            title="Delete Expense"
                          >
                            <Trash2 className="h-4 w-4" />
                          </button>
                        )}
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Add Expense Modal */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4 backdrop-blur-xs">
          <form
            onSubmit={handleAddExpense}
            className="w-full max-w-md rounded-2xl bg-white p-6 shadow-2xl"
          >
            <div className="flex items-center justify-between pb-3 border-b mb-4">
              <h3 className="text-base font-bold text-slate-900">
                {isBangla ? 'নতুন ব্যবসায়িক খরচ যোগ করুন' : 'Record Operating Expense'}
              </h3>
              <button
                type="button"
                onClick={() => setShowAddModal(false)}
                className="text-slate-400 hover:text-slate-700"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            <div className="space-y-3 text-xs">
              <div>
                <label className="text-slate-600 font-medium block mb-1">
                  {isBangla ? 'খরচের বিবরণ / টাইটেল*' : 'Expense Title*'}
                </label>
                <input
                  type="text"
                  required
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  placeholder={isBangla ? 'যেমন: দোকান ভাড়া, কর্মচারীর বেতন' : 'e.g. Monthly rent, staff salary'}
                  className="w-full rounded-xl border border-slate-200 p-2 text-xs focus:border-emerald-500 focus:outline-hidden"
                />
              </div>

              <div>
                <label className="text-slate-600 font-medium block mb-1">
                  {isBangla ? 'ক্যাটাগরি*' : 'Category*'}
                </label>
                <select
                  value={category}
                  onChange={(e) => setCategory(e.target.value as ExpenseCategory)}
                  className="w-full rounded-xl border border-slate-200 p-2 text-xs focus:border-emerald-500 focus:outline-hidden"
                >
                  {EXPENSE_CATEGORIES.map((c) => (
                    <option key={c.key} value={c.key}>
                      {isBangla ? c.labelBn : c.labelEn}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="text-slate-600 font-medium block mb-1">
                  {isBangla ? 'টাকার পরিমাণ (৳)*' : 'Amount (৳)*'}
                </label>
                <input
                  type="number"
                  step="0.01"
                  required
                  min="1"
                  value={amount || ''}
                  onChange={(e) => setAmount(Number(e.target.value))}
                  placeholder="0.00"
                  className="w-full rounded-xl border border-slate-200 p-2 text-xs font-bold text-slate-800 focus:border-emerald-500 focus:outline-hidden"
                />
              </div>

              <div>
                <label className="text-slate-600 font-medium block mb-1">
                  {strings.paymentMethod}
                </label>
                <select
                  value={paymentMethod}
                  onChange={(e) => setPaymentMethod(e.target.value)}
                  className="w-full rounded-xl border border-slate-200 p-2 text-xs focus:border-emerald-500 focus:outline-hidden"
                >
                  <option value="CASH">Cash (নগদ)</option>
                  <option value="BKASH">bKash (বিকাশ)</option>
                  <option value="NAGAD">Nagad (নগদ)</option>
                  <option value="BANK">Bank Transfer (ব্যাংক)</option>
                </select>
              </div>

              <div>
                <label className="text-slate-600 font-medium block mb-1">
                  {isBangla ? 'নোট / মন্তব্য' : 'Notes / Remarks'}
                </label>
                <input
                  type="text"
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  placeholder={isBangla ? 'অতিরিক্ত বিবরণ...' : 'Optional details...'}
                  className="w-full rounded-xl border border-slate-200 p-2 text-xs focus:border-emerald-500 focus:outline-hidden"
                />
              </div>
            </div>

            <div className="mt-5 flex justify-end gap-2 border-t pt-4">
              <button
                type="button"
                onClick={() => setShowAddModal(false)}
                className="rounded-xl border border-slate-200 px-4 py-2 text-xs font-semibold text-slate-600"
              >
                {strings.cancel}
              </button>
              <button
                type="submit"
                className="rounded-xl bg-emerald-600 px-5 py-2 text-xs font-semibold text-white shadow-md shadow-emerald-200 hover:bg-emerald-700"
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
