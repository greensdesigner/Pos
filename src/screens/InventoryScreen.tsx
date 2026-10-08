import React, { useMemo, useState } from 'react';
import { useApp } from '../context/AppContext';
import { Localization } from '../utils/localization';
import { ProductEntity, USER_ROLES } from '../types';
import {
  Boxes,
  Plus,
  Search,
  AlertTriangle,
  Edit2,
  Trash2,
  TrendingUp,
  PackageCheck,
  X,
  SlidersHorizontal,
} from 'lucide-react';

export const InventoryScreen: React.FC = () => {
  const {
    products,
    saveProduct,
    deleteProduct,
    adjustStock,
    currentRole,
    isBangla,
    strings,
  } = useApp();

  const [searchQuery, setSearchQuery] = useState('');
  const [filterType, setFilterType] = useState<'ALL' | 'LOW' | 'OUT'>('ALL');
  const [editingProduct, setEditingProduct] = useState<Partial<ProductEntity> | null>(null);
  const [adjustingProduct, setAdjustingProduct] = useState<ProductEntity | null>(null);
  const [adjustDelta, setAdjustDelta] = useState<number>(0);

  const roleInfo = USER_ROLES[currentRole];

  // Inventory Valuation
  const valuation = useMemo(() => {
    let totalCost = 0;
    let totalRetail = 0;
    products.forEach((p) => {
      totalCost += p.buyingPrice * p.stockQuantity;
      totalRetail += p.sellingPrice * p.stockQuantity;
    });
    return {
      totalCost,
      totalRetail,
      potentialProfit: totalRetail - totalCost,
    };
  }, [products]);

  // Filtered Products
  const filteredProducts = useMemo(() => {
    return products.filter((p) => {
      const q = searchQuery.trim().toLowerCase();
      const matchSearch =
        !q ||
        p.name.toLowerCase().includes(q) ||
        p.banglaName.toLowerCase().includes(q) ||
        p.barcode.toLowerCase().includes(q) ||
        p.category.toLowerCase().includes(q);

      if (!matchSearch) return false;

      if (filterType === 'LOW') {
        return p.stockQuantity > 0 && p.stockQuantity <= p.minStockLevel;
      }
      if (filterType === 'OUT') {
        return p.stockQuantity <= 0;
      }
      return true;
    });
  }, [products, searchQuery, filterType]);

  const handleSaveProduct = (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingProduct || !editingProduct.name || editingProduct.sellingPrice === undefined) {
      return;
    }
    saveProduct(editingProduct as any);
    setEditingProduct(null);
  };

  const handleApplyAdjustment = (e: React.FormEvent) => {
    e.preventDefault();
    if (adjustingProduct && adjustDelta !== 0) {
      adjustStock(adjustingProduct.id, adjustDelta);
      setAdjustingProduct(null);
      setAdjustDelta(0);
    }
  };

  return (
    <div className="space-y-6 p-4 sm:p-6 lg:p-8">
      {/* Header & Add Button */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 className="text-2xl font-black tracking-tight text-slate-900">
            {strings.inventory}
          </h2>
          <p className="text-xs text-slate-500">
            {isBangla
              ? 'মজুদ পণ্য তালিকা, কেনা মূলধন মূল্যায়ন ও স্টক সমন্বয়'
              : 'Product catalog, asset valuation, and stock replenishment'}
          </p>
        </div>

        <button
          onClick={() =>
            setEditingProduct({
              name: '',
              banglaName: '',
              barcode: `894${Date.now().toString().slice(-8)}`,
              category: 'Groceries',
              buyingPrice: 0,
              sellingPrice: 0,
              wholesalePrice: 0,
              stockQuantity: 10,
              minStockLevel: 5,
              unit: 'pcs',
            })
          }
          className="flex items-center gap-2 rounded-xl bg-emerald-600 px-4 py-2.5 text-xs font-bold text-white shadow-md shadow-emerald-200 transition hover:bg-emerald-700"
        >
          <Plus className="h-4 w-4" />
          <span>{strings.addProduct}</span>
        </button>
      </div>

      {/* Inventory Valuation Metric Cards */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-xs">
          <div className="flex items-center justify-between text-slate-500">
            <span className="text-xs font-semibold uppercase tracking-wider">
              {strings.assetCost}
            </span>
            <Boxes className="h-4 w-4 text-slate-400" />
          </div>
          <p className="mt-2 text-xl font-black text-slate-900">
            {Localization.formatCurrency(valuation.totalCost, isBangla)}
          </p>
          <p className="mt-1 text-xs text-slate-400">
            {isBangla ? 'স্টকে থাকা পণ্যের ক্রয়মূল্য' : 'Current stock at cost price'}
          </p>
        </div>

        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-xs">
          <div className="flex items-center justify-between text-slate-500">
            <span className="text-xs font-semibold uppercase tracking-wider">
              {strings.expectedRetailValue}
            </span>
            <TrendingUp className="h-4 w-4 text-emerald-600" />
          </div>
          <p className="mt-2 text-xl font-black text-emerald-700">
            {Localization.formatCurrency(valuation.totalRetail, isBangla)}
          </p>
          <p className="mt-1 text-xs text-slate-400">
            {isBangla ? 'সর্বমোট বিক্রয় সম্ভাবনা' : 'Total estimated retail value'}
          </p>
        </div>

        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-xs">
          <div className="flex items-center justify-between text-slate-500">
            <span className="text-xs font-semibold uppercase tracking-wider">
              {strings.potentialProfit}
            </span>
            <PackageCheck className="h-4 w-4 text-blue-600" />
          </div>
          <p className="mt-2 text-xl font-black text-blue-700">
            {Localization.formatCurrency(valuation.potentialProfit, isBangla)}
          </p>
          <p className="mt-1 text-xs text-slate-400">
            {isBangla ? 'মজুদ বিক্রয়ে সম্ভাব্য মোট লাভ' : 'Projected gross margin'}
          </p>
        </div>

        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-xs">
          <div className="flex items-center justify-between text-slate-500">
            <span className="text-xs font-semibold uppercase tracking-wider">
              {isBangla ? 'মোট পণ্যের ধরন' : 'Active Catalog SKUs'}
            </span>
            <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-bold text-slate-700">
              {products.length}
            </span>
          </div>
          <p className="mt-2 text-xl font-black text-slate-900">
            {products.length} {isBangla ? 'আইটেম' : 'Items'}
          </p>
          <p className="mt-1 text-xs text-slate-400">
            {isBangla ? 'ডাটাবেসে নিবন্ধিত পণ্য' : 'Registered catalog items'}
          </p>
        </div>
      </div>

      {/* Filter Chips & Search Bar */}
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div className="relative flex-1 max-w-md">
          <Search className="absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder={isBangla ? 'পণ্য, বারকোড বা ক্যাটাগরি খুঁজুন...' : 'Search by name, SKU, or category...'}
            className="w-full rounded-xl border border-slate-200 bg-white py-2 pl-10 pr-4 text-xs shadow-xs focus:border-emerald-500 focus:outline-hidden"
          />
        </div>

        <div className="flex items-center gap-1.5">
          <button
            onClick={() => setFilterType('ALL')}
            className={`rounded-lg px-3 py-1.5 text-xs font-semibold transition ${
              filterType === 'ALL'
                ? 'bg-slate-900 text-white'
                : 'bg-white text-slate-600 border border-slate-200 hover:bg-slate-50'
            }`}
          >
            {isBangla ? 'সকল পণ্য' : 'All Products'}
          </button>
          <button
            onClick={() => setFilterType('LOW')}
            className={`rounded-lg px-3 py-1.5 text-xs font-semibold transition ${
              filterType === 'LOW'
                ? 'bg-amber-600 text-white'
                : 'bg-white text-amber-700 border border-amber-200 hover:bg-amber-50'
            }`}
          >
            {strings.lowStockAlert}
          </button>
          <button
            onClick={() => setFilterType('OUT')}
            className={`rounded-lg px-3 py-1.5 text-xs font-semibold transition ${
              filterType === 'OUT'
                ? 'bg-rose-600 text-white'
                : 'bg-white text-rose-700 border border-rose-200 hover:bg-rose-50'
            }`}
          >
            {strings.outOfStock}
          </button>
        </div>
      </div>

      {/* Product Table */}
      <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-xs">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="border-b border-slate-200 bg-slate-50 text-[11px] font-bold uppercase tracking-wider text-slate-500">
              <tr>
                <th className="px-4 py-3">Barcode</th>
                <th className="px-4 py-3">Product Name</th>
                <th className="px-4 py-3">Category</th>
                <th className="px-4 py-3 text-right">Cost (ক্রয়)</th>
                <th className="px-4 py-3 text-right">Retail (বিক্রয়)</th>
                <th className="px-4 py-3 text-right">Wholesale</th>
                <th className="px-4 py-3 text-center">Stock</th>
                <th className="px-4 py-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {filteredProducts.map((p) => {
                const isLow = p.stockQuantity <= p.minStockLevel && p.stockQuantity > 0;
                const isOut = p.stockQuantity <= 0;

                return (
                  <tr key={p.id} className="hover:bg-slate-50/70 transition">
                    <td className="px-4 py-3 font-mono font-medium text-slate-500">
                      {p.barcode}
                    </td>
                    <td className="px-4 py-3">
                      <p className="font-bold text-slate-800">{p.name}</p>
                      {p.banglaName && (
                        <p className="text-[11px] text-slate-400">{p.banglaName}</p>
                      )}
                    </td>
                    <td className="px-4 py-3">
                      <span className="rounded bg-slate-100 px-2 py-0.5 text-[11px] font-medium text-slate-600">
                        {p.category}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-right font-medium text-slate-600">
                      ৳{p.buyingPrice.toFixed(2)}
                    </td>
                    <td className="px-4 py-3 text-right font-bold text-slate-900">
                      ৳{p.sellingPrice.toFixed(2)}
                    </td>
                    <td className="px-4 py-3 text-right text-slate-500">
                      ৳{p.wholesalePrice.toFixed(2)}
                    </td>
                    <td className="px-4 py-3 text-center">
                      <span
                        className={`inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-[11px] font-bold ${
                          isOut
                            ? 'bg-rose-100 text-rose-800'
                            : isLow
                            ? 'bg-amber-100 text-amber-800'
                            : 'bg-emerald-100 text-emerald-800'
                        }`}
                      >
                        {p.stockQuantity} {p.unit}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-right">
                      <div className="flex items-center justify-end gap-1.5">
                        <button
                          onClick={() => {
                            setAdjustingProduct(p);
                            setAdjustDelta(0);
                          }}
                          className="rounded-lg border border-slate-200 bg-white px-2 py-1 text-[11px] font-semibold text-slate-700 hover:bg-slate-50"
                          title="Quick stock adjustment"
                        >
                          {isBangla ? 'সমন্বয়' : 'Adjust'}
                        </button>
                        <button
                          onClick={() => setEditingProduct(p)}
                          className="rounded-lg p-1.5 text-slate-400 hover:bg-slate-100 hover:text-slate-800"
                          title="Edit Product"
                        >
                          <Edit2 className="h-4 w-4" />
                        </button>
                        {roleInfo.canDeleteRecords && (
                          <button
                            onClick={() => {
                              if (confirm(`Delete ${p.name}?`)) deleteProduct(p.id);
                            }}
                            className="rounded-lg p-1.5 text-slate-400 hover:bg-rose-50 hover:text-rose-600"
                            title="Delete Product"
                          >
                            <Trash2 className="h-4 w-4" />
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      {/* Edit / Add Product Modal */}
      {editingProduct && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4 backdrop-blur-xs">
          <form
            onSubmit={handleSaveProduct}
            className="w-full max-w-lg rounded-2xl bg-white p-6 shadow-2xl overflow-y-auto max-h-[90vh]"
          >
            <div className="flex items-center justify-between pb-3 border-b border-slate-200 mb-4">
              <h3 className="text-base font-bold text-slate-900">
                {editingProduct.id ? strings.editProduct : strings.addProduct}
              </h3>
              <button
                type="button"
                onClick={() => setEditingProduct(null)}
                className="text-slate-400 hover:text-slate-700"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs">
              <div className="sm:col-span-2">
                <label className="text-slate-600 font-medium block mb-1">
                  Product Name (English)*
                </label>
                <input
                  type="text"
                  required
                  value={editingProduct.name || ''}
                  onChange={(e) =>
                    setEditingProduct({ ...editingProduct, name: e.target.value })
                  }
                  className="w-full rounded-xl border border-slate-200 p-2 text-xs focus:border-emerald-500 focus:outline-hidden"
                />
              </div>

              <div className="sm:col-span-2">
                <label className="text-slate-600 font-medium block mb-1">
                  পণ্যের নাম (বাংলা)
                </label>
                <input
                  type="text"
                  value={editingProduct.banglaName || ''}
                  onChange={(e) =>
                    setEditingProduct({ ...editingProduct, banglaName: e.target.value })
                  }
                  className="w-full rounded-xl border border-slate-200 p-2 text-xs focus:border-emerald-500 focus:outline-hidden"
                />
              </div>

              <div>
                <label className="text-slate-600 font-medium block mb-1">Barcode / SKU*</label>
                <input
                  type="text"
                  required
                  value={editingProduct.barcode || ''}
                  onChange={(e) =>
                    setEditingProduct({ ...editingProduct, barcode: e.target.value })
                  }
                  className="w-full rounded-xl border border-slate-200 p-2 text-xs focus:border-emerald-500 focus:outline-hidden"
                />
              </div>

              <div>
                <label className="text-slate-600 font-medium block mb-1">Category*</label>
                <input
                  type="text"
                  required
                  value={editingProduct.category || ''}
                  onChange={(e) =>
                    setEditingProduct({ ...editingProduct, category: e.target.value })
                  }
                  className="w-full rounded-xl border border-slate-200 p-2 text-xs focus:border-emerald-500 focus:outline-hidden"
                />
              </div>

              <div>
                <label className="text-slate-600 font-medium block mb-1">
                  {strings.buyingPrice} (৳)*
                </label>
                <input
                  type="number"
                  step="0.01"
                  required
                  value={editingProduct.buyingPrice ?? 0}
                  onChange={(e) =>
                    setEditingProduct({
                      ...editingProduct,
                      buyingPrice: Number(e.target.value),
                    })
                  }
                  className="w-full rounded-xl border border-slate-200 p-2 text-xs focus:border-emerald-500 focus:outline-hidden"
                />
              </div>

              <div>
                <label className="text-slate-600 font-medium block mb-1">
                  {strings.sellingPrice} (৳)*
                </label>
                <input
                  type="number"
                  step="0.01"
                  required
                  value={editingProduct.sellingPrice ?? 0}
                  onChange={(e) =>
                    setEditingProduct({
                      ...editingProduct,
                      sellingPrice: Number(e.target.value),
                    })
                  }
                  className="w-full rounded-xl border border-slate-200 p-2 text-xs focus:border-emerald-500 focus:outline-hidden"
                />
              </div>

              <div>
                <label className="text-slate-600 font-medium block mb-1">
                  {strings.wholesalePrice} (৳)
                </label>
                <input
                  type="number"
                  step="0.01"
                  value={editingProduct.wholesalePrice ?? 0}
                  onChange={(e) =>
                    setEditingProduct({
                      ...editingProduct,
                      wholesalePrice: Number(e.target.value),
                    })
                  }
                  className="w-full rounded-xl border border-slate-200 p-2 text-xs focus:border-emerald-500 focus:outline-hidden"
                />
              </div>

              <div>
                <label className="text-slate-600 font-medium block mb-1">Unit (e.g. kg, pcs, btl)</label>
                <input
                  type="text"
                  value={editingProduct.unit || 'pcs'}
                  onChange={(e) =>
                    setEditingProduct({ ...editingProduct, unit: e.target.value })
                  }
                  className="w-full rounded-xl border border-slate-200 p-2 text-xs focus:border-emerald-500 focus:outline-hidden"
                />
              </div>

              <div>
                <label className="text-slate-600 font-medium block mb-1">
                  {strings.stockQuantity}
                </label>
                <input
                  type="number"
                  value={editingProduct.stockQuantity ?? 0}
                  onChange={(e) =>
                    setEditingProduct({
                      ...editingProduct,
                      stockQuantity: Number(e.target.value),
                    })
                  }
                  className="w-full rounded-xl border border-slate-200 p-2 text-xs focus:border-emerald-500 focus:outline-hidden"
                />
              </div>

              <div>
                <label className="text-slate-600 font-medium block mb-1">
                  Min Stock Alert Threshold
                </label>
                <input
                  type="number"
                  value={editingProduct.minStockLevel ?? 5}
                  onChange={(e) =>
                    setEditingProduct({
                      ...editingProduct,
                      minStockLevel: Number(e.target.value),
                    })
                  }
                  className="w-full rounded-xl border border-slate-200 p-2 text-xs focus:border-emerald-500 focus:outline-hidden"
                />
              </div>
            </div>

            <div className="mt-6 flex justify-end gap-2 border-t pt-4">
              <button
                type="button"
                onClick={() => setEditingProduct(null)}
                className="rounded-xl border border-slate-200 px-4 py-2 text-xs font-semibold text-slate-600"
              >
                {strings.cancel}
              </button>
              <button
                type="submit"
                className="rounded-xl bg-emerald-600 px-5 py-2 text-xs font-semibold text-white shadow-md shadow-emerald-200"
              >
                {strings.save}
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Quick Stock Adjustment Modal */}
      {adjustingProduct && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4 backdrop-blur-xs">
          <form
            onSubmit={handleApplyAdjustment}
            className="w-full max-w-sm rounded-2xl bg-white p-5 shadow-2xl"
          >
            <h3 className="text-sm font-bold text-slate-900 mb-1">
              {strings.adjustStock}
            </h3>
            <p className="text-xs text-slate-500 mb-4">
              {adjustingProduct.name} (Current: {adjustingProduct.stockQuantity} {adjustingProduct.unit})
            </p>

            <div className="space-y-3">
              <div>
                <label className="text-xs font-medium text-slate-600 block mb-1">
                  {isBangla ? 'স্টক পরিবর্তন (+ বৃদ্ধি / - হ্রাস):' : 'Stock Quantity Delta (+ or -):'}
                </label>
                <input
                  type="number"
                  required
                  value={adjustDelta || ''}
                  onChange={(e) => setAdjustDelta(Number(e.target.value))}
                  placeholder="e.g. +20 or -5"
                  className="w-full rounded-xl border border-slate-200 p-2.5 text-xs text-center font-bold text-slate-800"
                />
              </div>

              <div className="flex gap-2">
                {[5, 10, 20, 50].map((num) => (
                  <button
                    key={num}
                    type="button"
                    onClick={() => setAdjustDelta(num)}
                    className="flex-1 rounded-lg bg-slate-100 py-1.5 text-xs font-semibold text-slate-700 hover:bg-slate-200"
                  >
                    +{num}
                  </button>
                ))}
              </div>
            </div>

            <div className="mt-5 flex justify-end gap-2 border-t pt-3">
              <button
                type="button"
                onClick={() => setAdjustingProduct(null)}
                className="rounded-xl border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-600"
              >
                {strings.cancel}
              </button>
              <button
                type="submit"
                className="rounded-xl bg-emerald-600 px-4 py-1.5 text-xs font-semibold text-white"
              >
                {strings.confirm}
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
};
