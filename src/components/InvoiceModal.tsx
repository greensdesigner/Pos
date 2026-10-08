import React, { useMemo, useState } from 'react';
import { useApp } from '../context/AppContext';
import { ThermalReceiptFormatter } from '../utils/receiptFormatter';
import { Printer, Copy, Check, X, Share2, Smartphone, Mail, Send } from 'lucide-react';

export const InvoiceModal: React.FC = () => {
  const {
    activeInvoice,
    closeInvoiceDialog,
    storeConfig,
    isBangla,
    sendReceiptEmail,
    currentUser,
  } = useApp();
  const [widthCols, setWidthCols] = useState<number>(32); // 32 for 58mm, 48 for 80mm
  const [copied, setCopied] = useState(false);
  const [emailInput, setEmailInput] = useState('');
  const [showEmailBox, setShowEmailBox] = useState(false);
  const [isSendingEmail, setIsSendingEmail] = useState(false);
  const [emailSent, setEmailSent] = useState(false);

  if (!activeInvoice) return null;

  const { sale, items } = activeInvoice;

  const receiptText = useMemo(() => {
    return ThermalReceiptFormatter.generateMonospaceReceipt(
      sale,
      items,
      storeConfig,
      widthCols,
      isBangla
    );
  }, [sale, items, storeConfig, widthCols, isBangla]);

  const handleCopy = () => {
    navigator.clipboard.writeText(receiptText);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handlePrint = () => {
    window.print();
  };

  const handleShareWhatsApp = () => {
    const text = encodeURIComponent(receiptText);
    window.open(`https://api.whatsapp.com/send?text=${text}`, '_blank');
  };

  const handleSendEmail = async (e: React.FormEvent) => {
    e.preventDefault();
    const target = (emailInput || currentUser.email).trim();
    if (!target) return;

    setIsSendingEmail(true);
    const res = await sendReceiptEmail(target, sale.invoiceNumber, receiptText, sale.netPayable);
    setIsSendingEmail(false);
    if (res.success) {
      setEmailSent(true);
      setTimeout(() => {
        setEmailSent(false);
        setShowEmailBox(false);
      }, 2500);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-xs">
      <div className="relative flex max-h-[90vh] w-full max-w-lg flex-col rounded-2xl bg-white shadow-2xl">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-slate-200 px-6 py-4">
          <div>
            <h2 className="text-lg font-bold text-slate-900">
              {isBangla ? 'বিক্রয় ইনভয়েস / রশিদ' : 'Sales Receipt & Invoice'}
            </h2>
            <p className="text-xs text-slate-500">
              #{sale.invoiceNumber} • {new Date(sale.timestamp).toLocaleTimeString()}
            </p>
          </div>
          <button
            onClick={closeInvoiceDialog}
            className="rounded-lg p-1.5 text-slate-400 hover:bg-slate-100 hover:text-slate-700"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Paper Format Selector */}
        <div className="flex items-center justify-between bg-slate-50 px-6 py-2.5 border-b border-slate-200">
          <span className="text-xs font-semibold text-slate-600">
            {isBangla ? 'রশিদ ফরম্যাট:' : 'Receipt Width:'}
          </span>
          <div className="flex items-center gap-2">
            <button
              onClick={() => setWidthCols(32)}
              className={`rounded-lg px-2.5 py-1 text-xs font-semibold transition ${
                widthCols === 32
                  ? 'bg-emerald-600 text-white'
                  : 'bg-white text-slate-600 border border-slate-200 hover:bg-slate-100'
              }`}
            >
              58mm (POS)
            </button>
            <button
              onClick={() => setWidthCols(48)}
              className={`rounded-lg px-2.5 py-1 text-xs font-semibold transition ${
                widthCols === 48
                  ? 'bg-emerald-600 text-white'
                  : 'bg-white text-slate-600 border border-slate-200 hover:bg-slate-100'
              }`}
            >
              80mm (Desktop)
            </button>
          </div>
        </div>

        {/* Monospace Thermal Receipt Preview */}
        <div className="flex-1 overflow-y-auto p-6 bg-slate-100/70">
          <div
            id="printable-receipt"
            className="mx-auto rounded-xl border border-slate-300 bg-white p-5 font-mono text-xs shadow-sm"
            style={{ maxWidth: widthCols === 32 ? '300px' : '420px' }}
          >
            <pre className="whitespace-pre-wrap leading-relaxed text-slate-800">
              {receiptText}
            </pre>
          </div>
        </div>

        {/* Footer Actions */}
        <div className="flex flex-wrap items-center justify-between gap-2 border-t border-slate-200 bg-white px-6 py-4">
          <div className="flex items-center gap-2">
            <button
              onClick={handleCopy}
              className="flex items-center gap-1.5 rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-xs font-semibold text-slate-700 transition hover:bg-slate-100"
            >
              {copied ? (
                <>
                  <Check className="h-4 w-4 text-emerald-600" />
                  <span>{isBangla ? 'কপি হয়েছে' : 'Copied'}</span>
                </>
              ) : (
                <>
                  <Copy className="h-4 w-4 text-slate-600" />
                  <span>{isBangla ? 'কপি টেক্সট' : 'Copy'}</span>
                </>
              )}
            </button>

            <button
              onClick={handleShareWhatsApp}
              className="flex items-center gap-1.5 rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-xs font-semibold text-emerald-700 transition hover:bg-emerald-50"
              title="Share on WhatsApp"
            >
              <Smartphone className="h-4 w-4 text-emerald-600" />
              <span>WhatsApp</span>
            </button>

            <button
              onClick={() => setShowEmailBox(!showEmailBox)}
              className="flex items-center gap-1.5 rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-xs font-semibold text-blue-700 transition hover:bg-blue-50"
              title="Send via Hostinger SMTP Email"
            >
              <Mail className="h-4 w-4 text-blue-600" />
              <span>{isBangla ? 'ইমেইল রশিদ' : 'Email'}</span>
            </button>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={handlePrint}
              className="flex items-center gap-1.5 rounded-xl bg-slate-900 px-4 py-2 text-xs font-semibold text-white shadow-sm hover:bg-slate-800"
            >
              <Printer className="h-4 w-4" />
              <span>{isBangla ? 'প্রিন্ট রশিদ' : 'Print'}</span>
            </button>

            <button
              onClick={closeInvoiceDialog}
              className="flex items-center gap-1.5 rounded-xl bg-emerald-600 px-4 py-2 text-xs font-semibold text-white shadow-sm shadow-emerald-200 hover:bg-emerald-700"
            >
              <span>{isBangla ? 'নতুন বিক্রয় শুরু' : 'New Sale'}</span>
            </button>
          </div>
        </div>

        {/* Email Sending Popover */}
        {showEmailBox && (
          <form
            onSubmit={handleSendEmail}
            className="border-t border-slate-200 bg-blue-50/50 px-6 py-3 flex items-center gap-2 text-xs"
          >
            <Mail className="h-4 w-4 text-blue-600 shrink-0" />
            <input
              type="email"
              value={emailInput}
              onChange={(e) => setEmailInput(e.target.value)}
              placeholder={currentUser.email}
              className="flex-1 rounded-xl border border-blue-200 bg-white px-3 py-1.5 text-xs focus:border-blue-500 focus:outline-hidden"
            />
            <button
              type="submit"
              disabled={isSendingEmail}
              className="flex items-center gap-1 rounded-xl bg-blue-600 px-3 py-1.5 font-bold text-white shadow-xs hover:bg-blue-700 disabled:opacity-50"
            >
              {emailSent ? (
                <>
                  <Check className="h-3.5 w-3.5 text-white" />
                  <span>{isBangla ? 'পাঠানো হয়েছে' : 'Sent'}</span>
                </>
              ) : isSendingEmail ? (
                <span>{isBangla ? 'পাঠাচ্ছে...' : 'Sending...'}</span>
              ) : (
                <>
                  <Send className="h-3.5 w-3.5" />
                  <span>{isBangla ? 'পাঠান' : 'Send'}</span>
                </>
              )}
            </button>
          </form>
        )}
      </div>
    </div>
  );
};
