import { SaleEntity, SaleItemEntity, StoreConfigEntity } from '../types';
import { Localization } from './localization';

export const ThermalReceiptFormatter = {
  generateMonospaceReceipt(
    sale: SaleEntity,
    items: SaleItemEntity[],
    config: StoreConfigEntity | null,
    widthCols = 32, // 32 for 58mm, 48 for 80mm
    isBangla = false
  ): string {
    const storeName = isBangla
      ? config?.banglaStoreName || 'গ্রিনসস্টক'
      : config?.storeName || 'GreensStock Supermart';
    const phone = config?.phone || '+880 1712-345678';
    const address = config?.address || 'Dhaka, Bangladesh';
    const footer = config?.receiptFooter || 'Thank you! Please visit again.';

    const center = (text: string): string => {
      if (text.length >= widthCols) return text;
      const padding = Math.floor((widthCols - text.length) / 2);
      return ' '.repeat(padding) + text;
    };

    const divider = (ch = '-'): string => ch.repeat(widthCols);

    const row = (left: string, right: string): string => {
      const space = widthCols - left.length - right.length;
      if (space > 0) {
        return left + ' '.repeat(space) + right;
      }
      return `${left} ${right}`;
    };

    const lines: string[] = [];

    // Header
    lines.push(center(storeName));
    lines.push(center(address));
    lines.push(center(`Tel: ${phone}`));
    lines.push(divider('='));

    // Invoice Meta
    const dateStr = Localization.formatDate(sale.timestamp, isBangla);
    lines.push(row(`Invoice: ${sale.invoiceNumber}`, ''));
    lines.push(row(`Date: ${dateStr}`, ''));
    lines.push(row(`Customer: ${sale.customerName}`, ''));
    lines.push(row(`Cashier: ${sale.cashierRole}`, ''));
    lines.push(divider('-'));

    // Items Header
    if (widthCols === 32) {
      lines.push(row('Item (Qty x Price)', 'Total'));
    } else {
      lines.push(row('Item Description', 'Qty   Rate     Total'));
    }
    lines.push(divider('-'));

    // Items
    for (const item of items) {
      const shortName = item.productName.slice(0, 18);
      const itemLine = `${shortName} (${item.quantity}x${Math.round(item.unitPrice)})`;
      const totalStr = Localization.formatCompactCurrency(item.subtotal, isBangla);
      lines.push(row(itemLine, totalStr));
    }
    lines.push(divider('-'));

    // Totals
    lines.push(row('Subtotal:', Localization.formatCurrency(sale.subtotal, isBangla)));
    if (sale.discountAmount > 0) {
      lines.push(row('Discount (-):', Localization.formatCurrency(sale.discountAmount, isBangla)));
    }
    if (sale.vatAmount > 0) {
      lines.push(row('VAT/Tax (+):', Localization.formatCurrency(sale.vatAmount, isBangla)));
    }
    lines.push(divider('='));
    lines.push(row('NET PAYABLE:', Localization.formatCurrency(sale.netPayable, isBangla)));
    lines.push(row(`Paid (${sale.paymentMethod}):`, Localization.formatCurrency(sale.paidAmount, isBangla)));

    if (sale.dueAmount > 0) {
      lines.push(row('DUE BALANCE:', Localization.formatCurrency(sale.dueAmount, isBangla)));
    } else {
      const change = Math.max(0, sale.paidAmount - sale.netPayable);
      if (change > 0) {
        lines.push(row('Change Returned:', Localization.formatCurrency(change, isBangla)));
      }
    }

    // Footer
    lines.push(divider('-'));
    lines.push(center(footer));
    lines.push(center('*** Powered by GreensStock POS ***'));
    lines.push('');

    return lines.join('\n');
  },
};
