import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { Localization } from '../utils/localization';
import { ProductEntity, ReturnType, SaleEntity, SaleItemEntity } from '../types';
import {
  RotateCcw,
  Search,
  CheckCircle2,
  AlertCircle,
  ArrowRightLeft,
  Calendar,
  Package,
} from 'lucide-react';

export const ReturnsScreen: React.FC = () => {
  const {
    sales,
    saleItems,
    products,
    returns,
    processReturn,
    isBangla,
    strings,
  } = useApp();

  const [invoiceQuery, setInvoiceQuery] = useState('');
  const [selectedSale, setSelectedSale] = useState<SaleEntity | null>(null);
  const [selectedItem, setSelectedItem] = useState<SaleItemEntity | null>(null);
  const [returnType, setReturnType] = useState<ReturnType>('FULL_RETURN');
  const [quantity, setQuantity] = useState<number>(1);
  const [replacementProduct, setReplacementProduct] = useState<ProductEntity | null>(null);
  const [reason, setReason] = useState('');

  // Items for the selected sale
  const itemsForSelectedSale = selectedSale
    ? saleItems.filter((i) => i.saleId === selectedSale.id)
    : [];

  const handleSearchInvoice = (e: React.FormEvent) => {
    e.preventDefault();
    const clean = invoiceQuery.trim().toLowerCase();
    const match = sales.find((s) => s.invoiceNumber.toLowerCase().includes(clean));
    if (match) {
      setSelectedSale(match);
      setSelectedItem(null);
      setReplacementProduct(null);
    }
  };

  const handleSelectSale = (sale: SaleEntity) => {
    setSelectedSale(sale);
    setSelectedItem(null);
    setReplacementProduct(null);
    setQuantity(1);
  };

  const handleSubmitReturn = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedSale || !selectedItem) return;

    processReturn({
      originalSale: selectedSale,
      itemToReturn: selectedItem,
      returnType,
      quantity,
      replacementProduct,
      reason,
    });

    // Reset selection
    setSelectedItem(null);
    setReplacementProduct(null);
    setReason('');
    setQuantity(1);
  };

  const priceDifference =
    returnType === 'EXCHANGE' && selectedItem && replacementProduct
      ? replacementProduct.sellingPrice * quantity - selectedItem.unitPrice * quantity
      : 0;

  return (
    <div className="space-y-6 p-4 sm:p-6 lg:p-8">
      <div>
        <h2 className="text-2xl font-black tracking-tight text-slate-900">
          {strings.returns}
        </h2>
        <p className="text-xs text-slate-500">
          {isBangla
            ? 'বিক্রীত পণ্যের সম্পূর্ণ ফেরত বা পণ্য বদল (এক্সচেঞ্জ) প্রক্রিয়া'
            : 'Process customer product refunds and item-for-item replacements'}
        </p>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-12">
        {/* Left Column: Search & Return Form (7 Cols) */}
        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-xs lg:col-span-7">
          <h3 className="text-sm font-bold text-slate-900 mb-3 flex items-center gap-2">
            <RotateCcw className="h-4 w-4 text-emerald-600" />
            {isBangla ? 'রিটার্ন / এক্সচেঞ্জ ফরম' : 'Initiate Return or Exchange'}
          </h3>

          {/* Search Sale by Invoice */}
          <form onSubmit={handleSearchInvoice} className="flex gap-2 mb-4">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
              <input
                type="text"
                value={invoiceQuery}
                onChange={(e) => setInvoiceQuery(e.target.value)}
                placeholder={isBangla ? 'ইনভয়েস নম্বর লিখুন (যেমন: GS-2026-00101)...' : 'Enter invoice number (e.g. GS-2026-00101)...'}
                className="w-full rounded-xl border border-slate-200 py-2.5 pl-10 pr-3 text-xs focus:border-emerald-500 focus:outline-hidden"
              />
            </div>
            <button
              type="submit"
              className="rounded-xl bg-slate-900 px-4 py-2.5 text-xs font-semibold text-white hover:bg-slate-800"
            >
              {isBangla ? 'খুঁজুন' : 'Search'}
            </button>
          </form>

          {/* Quick Recent Sales Chips if no sale selected */}
          {!selectedSale && (
            <div className="mb-4">
              <span className="text-[11px] font-medium text-slate-400 block mb-1.5">
                {isBangla ? 'অথবা সাম্প্রতিক ইনভয়েস নির্বাচন করুন:' : 'Or select from recent sales:'}
              </span>
              <div className="flex flex-wrap gap-1.5">
                {sales.slice(0, 4).map((s) => (
                  <button
                    key={s.id}
                    onClick={() => handleSelectSale(s)}
                    className="rounded-lg border border-slate-200 bg-slate-50 px-2.5 py-1.5 text-xs font-mono font-semibold text-slate-700 hover:bg-emerald-50 hover:border-emerald-300"
                  >
                    {s.invoiceNumber} (৳{s.netPayable})
                  </button>
                ))}
              </div>
            </div>
          )}

          {/* If Sale is Selected */}
          {selectedSale && (
            <div className="rounded-xl border border-emerald-200 bg-emerald-50/50 p-4 mb-4 text-xs">
              <div className="flex items-center justify-between">
                <div>
                  <p className="font-mono font-bold text-emerald-950">
                    {selectedSale.invoiceNumber}
                  </p>
                  <p className="text-[11px] text-slate-500">
                    {selectedSale.customerName} •{' '}
                    {Localization.formatDate(selectedSale.timestamp, isBangla)}
                  </p>
                </div>
                <button
                  onClick={() => setSelectedSale(null)}
                  className="text-xs text-rose-600 font-semibold hover:underline"
                >
                  {isBangla ? 'পরিবর্তন করুন' : 'Change Sale'}
                </button>
              </div>

              {/* Items in this sale */}
              <div className="mt-3">
                <span className="text-[11px] font-semibold text-slate-700 block mb-1">
                  {isBangla ? 'ফেরতযোগ্য পণ্য বেছে নিন:' : 'Select item to return:'}
                </span>
                <div className="space-y-1.5">
                  {itemsForSelectedSale.map((it) => (
                    <button
                      key={it.id}
                      type="button"
                      onClick={() => {
                        setSelectedItem(it);
                        setQuantity(1);
                      }}
                      className={`flex w-full items-center justify-between rounded-xl p-2.5 text-left border transition ${
                        selectedItem?.id === it.id
                          ? 'border-emerald-600 bg-white font-bold text-emerald-900 shadow-xs'
                          : 'border-slate-200 bg-white text-slate-700 hover:bg-slate-50'
                      }`}
                    >
                      <div>
                        <p className="font-semibold">{it.productName}</p>
                        <p className="text-[11px] text-slate-400">
                          Qty: {it.quantity} × ৳{it.unitPrice}
                        </p>
                      </div>
                      <span className="text-xs font-bold">৳{it.subtotal}</span>
                    </button>
                  ))}
                </div>
              </div>
            </div>
          )}

          {/* Action Form when Item Selected */}
          {selectedItem && selectedSale && (
            <form onSubmit={handleSubmitReturn} className="space-y-4 border-t pt-4 text-xs">
              {/* Return Type: Full Return vs Exchange */}
              <div>
                <label className="font-semibold text-slate-700 block mb-1.5">
                  {isBangla ? 'রিটার্নের ধরন:' : 'Return Type:'}
                </label>
                <div className="grid grid-cols-2 gap-2">
                  <button
                    type="button"
                    onClick={() => setReturnType('FULL_RETURN')}
                    className={`rounded-xl py-2 px-3 font-semibold border transition ${
                      returnType === 'FULL_RETURN'
                        ? 'border-emerald-600 bg-emerald-600 text-white'
                        : 'border-slate-200 bg-slate-50 text-slate-700 hover:bg-slate-100'
                    }`}
                  >
                    {isBangla ? 'সম্পূর্ণ ফেরত (টাকা রিফান্ড)' : 'Full Return (Cash Refund)'}
                  </button>
                  <button
                    type="button"
                    onClick={() => setReturnType('EXCHANGE')}
                    className={`rounded-xl py-2 px-3 font-semibold border transition ${
                      returnType === 'EXCHANGE'
                        ? 'border-emerald-600 bg-emerald-600 text-white'
                        : 'border-slate-200 bg-slate-50 text-slate-700 hover:bg-slate-100'
                    }`}
                  >
                    {isBangla ? 'পণ্য বদল / এক্সচেঞ্জ' : 'Exchange / Replacement'}
                  </button>
                </div>
              </div>

              {/* Quantity */}
              <div>
                <label className="font-semibold text-slate-700 block mb-1">
                  {isBangla ? 'ফেরত পরিমাণ (সর্বোচ্চ:' : 'Return Quantity (Max:'}{' '}
                  {selectedItem.quantity}):
                </label>
                <input
                  type="number"
                  min="1"
                  max={selectedItem.quantity}
                  value={quantity}
                  onChange={(e) =>
                    setQuantity(
                      Math.min(selectedItem.quantity, Math.max(1, Number(e.target.value)))
                    )
                  }
                  className="w-full rounded-xl border border-slate-200 p-2 text-xs font-bold"
                />
              </div>

              {/* If Exchange: Select Replacement Product */}
              {returnType === 'EXCHANGE' && (
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">
                    {isBangla ? 'বিকল্প পণ্য নির্বাচন করুন:' : 'Select Replacement Product:'}
                  </label>
                  <select
                    value={replacementProduct ? replacementProduct.id : ''}
                    onChange={(e) => {
                      const found = products.find((p) => p.id === Number(e.target.value));
                      setReplacementProduct(found || null);
                    }}
                    required
                    className="w-full rounded-xl border border-slate-200 p-2 text-xs"
                  >
                    <option value="">{isBangla ? '-- পণ্য বেছে নিন --' : '-- Choose Product --'}</option>
                    {products
                      .filter((p) => p.stockQuantity > 0)
                      .map((p) => (
                        <option key={p.id} value={p.id}>
                          {p.name} - ৳{p.sellingPrice} ({p.stockQuantity} in stock)
                        </option>
                      ))}
                  </select>

                  {replacementProduct && (
                    <div className="mt-2 rounded-xl bg-slate-100 p-3 text-xs">
                      <div className="flex justify-between">
                        <span>Original Credit:</span>
                        <span className="font-bold">
                          ৳{(selectedItem.unitPrice * quantity).toFixed(2)}
                        </span>
                      </div>
                      <div className="flex justify-between mt-1">
                        <span>Replacement Cost:</span>
                        <span className="font-bold">
                          ৳{(replacementProduct.sellingPrice * quantity).toFixed(2)}
                        </span>
                      </div>
                      <div className="flex justify-between border-t border-slate-200 pt-1.5 mt-1.5 font-bold">
                        <span>
                          {priceDifference >= 0
                            ? isBangla ? 'গ্রাহক অতিরিক্ত পরিশোধ করবেন:' : 'Customer Pays Difference:'
                            : isBangla ? 'গ্রাহককে ফেরতযোগ্য:' : 'Refund Customer Difference:'}
                        </span>
                        <span
                          className={priceDifference >= 0 ? 'text-emerald-700' : 'text-rose-600'}
                        >
                          ৳{Math.abs(priceDifference).toFixed(2)}
                        </span>
                      </div>
                    </div>
                  )}
                </div>
              )}

              {/* Reason */}
              <div>
                <label className="font-semibold text-slate-700 block mb-1">
                  {isBangla ? 'ফেরতের কারণ:' : 'Reason for Return:'}
                </label>
                <input
                  type="text"
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  placeholder={isBangla ? 'যেমন: মেয়াদোত্তীর্ণ, ক্ষতিগ্রস্ত, সাইজ পরিবর্তন ইত্যাদি' : 'e.g. Defective, change of mind, exchange size'}
                  className="w-full rounded-xl border border-slate-200 p-2 text-xs"
                />
              </div>

              {/* Submit */}
              <button
                type="submit"
                className="w-full rounded-xl bg-emerald-600 py-3 text-xs font-bold text-white shadow-md shadow-emerald-200 hover:bg-emerald-700"
              >
                {returnType === 'FULL_RETURN'
                  ? isBangla ? 'ফেরত নিশ্চিত ও টাকা রিফান্ড করুন' : 'Confirm Return & Issue Refund'
                  : isBangla ? 'পণ্য বদল সম্পন্ন করুন' : 'Complete Item Exchange'}
              </button>
            </form>
          )}
        </div>

        {/* Right Column: Returns History Log (5 Cols) */}
        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-xs lg:col-span-5 flex flex-col">
          <h3 className="text-sm font-bold text-slate-900 mb-3 flex items-center justify-between">
            <span>{isBangla ? 'রিটার্ন ও এক্সচেঞ্জ হিস্ট্রি' : 'Processed Returns Log'}</span>
            <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-bold text-slate-700">
              {returns.length}
            </span>
          </h3>

          <div className="flex-1 overflow-y-auto space-y-2.5 max-h-[600px] pr-1">
            {returns.length === 0 ? (
              <div className="py-12 text-center text-xs text-slate-400">
                <RotateCcw className="mx-auto h-8 w-8 text-slate-300 mb-1" />
                {isBangla ? 'এখনো কোনো রিটার্ন রেকর্ড নেই' : 'No returns processed yet'}
              </div>
            ) : (
              returns.map((ret) => (
                <div
                  key={ret.id}
                  className="rounded-xl border border-slate-200 bg-slate-50 p-3 text-xs"
                >
                  <div className="flex items-center justify-between">
                    <span className="font-mono font-bold text-slate-800">
                      {ret.invoiceNumber}
                    </span>
                    <span
                      className={`rounded px-1.5 py-0.5 text-[10px] font-bold ${
                        ret.returnType === 'FULL_RETURN'
                          ? 'bg-rose-100 text-rose-800'
                          : 'bg-blue-100 text-blue-800'
                      }`}
                    >
                      {ret.returnType === 'FULL_RETURN'
                        ? isBangla ? 'সম্পূর্ণ ফেরত' : 'Full Return'
                        : isBangla ? 'এক্সচেঞ্জ' : 'Exchange'}
                    </span>
                  </div>

                  <p className="mt-1 font-semibold text-slate-700">
                    {ret.productName} × {ret.returnedQuantity}
                  </p>

                  <div className="mt-2 flex items-center justify-between text-[11px] text-slate-500 border-t border-slate-200/60 pt-1.5">
                    <span>
                      {ret.refundAmount > 0
                        ? `Refund: ৳${ret.refundAmount.toFixed(2)}`
                        : `Price Diff: ৳${ret.priceDifference.toFixed(2)}`}
                    </span>
                    <span>{new Date(ret.timestamp).toLocaleDateString()}</span>
                  </div>
                  {ret.reason && (
                    <p className="mt-1 text-[10px] text-slate-400 italic">
                      "{ret.reason}"
                    </p>
                  )}
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
