import type {
  InventoryCheckRecord,
  InventoryIssueRecord,
  InventoryReceiptRecord,
  InventoryTransferRecord
} from "../types/inventory";

export type PrintableInventoryDocumentType = "receipt" | "issue" | "transfer" | "check";
export type PrintableInventoryDocument =
  | InventoryReceiptRecord
  | InventoryIssueRecord
  | InventoryTransferRecord
  | InventoryCheckRecord;

export function printInventoryDocument(type: PrintableInventoryDocumentType, record: PrintableInventoryDocument) {
  const title = documentTitle(type);
  const html = `
    <main style="font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; padding: 28px; color: #111827;">
      <h1 style="margin: 0 0 24px; text-align: center; font-size: 24px;">${escapeHtml(title)}</h1>
      <table style="width: 100%; border-collapse: collapse; font-size: 14px;">
        <tbody>
          ${row("单据编号", documentNo(type, record))}
          ${row("业务摘要", documentSummary(type, record))}
          ${row("状态", statusOf(record))}
          ${row("备注", "remark" in record ? record.remark : undefined)}
          ${row("制单时间", "createdAt" in record ? record.createdAt : undefined)}
          ${row("打印时间", new Date().toLocaleString())}
        </tbody>
      </table>
      <section style="display: flex; justify-content: space-between; margin-top: 56px; font-size: 14px;">
        <span>制单：</span>
        <span>复核：</span>
        <span>经办：</span>
      </section>
    </main>
  `;
  openPrintWindow(title, html);
}

function documentTitle(type: PrintableInventoryDocumentType) {
  if (type === "receipt") return "入库单";
  if (type === "issue") return "出库单";
  if (type === "transfer") return "调拨单";
  return "盘点单";
}

function documentNo(type: PrintableInventoryDocumentType, record: PrintableInventoryDocument) {
  if (type === "receipt") return (record as InventoryReceiptRecord).receiptNo;
  if (type === "issue") return (record as InventoryIssueRecord).issueNo;
  if (type === "transfer") return (record as InventoryTransferRecord).transferNo;
  return (record as InventoryCheckRecord).checkNo;
}

function documentSummary(type: PrintableInventoryDocumentType, record: PrintableInventoryDocument) {
  if (type === "receipt") {
    const receipt = record as InventoryReceiptRecord;
    return `${receipt.sourceType || "-"} / 来源 ${receipt.sourceOrderNo || "-"} / 供应商 ${receipt.supplierName || "-"}`;
  }
  if (type === "issue") {
    const issue = record as InventoryIssueRecord;
    return `${issue.issueType || "-"} / 来源 ${issue.sourceOrderNo || "-"} / 数量 ${issue.totalQuantity ?? 0}`;
  }
  if (type === "transfer") {
    const transfer = record as InventoryTransferRecord;
    return `${transfer.fromLocation || "-"} → ${transfer.toLocation || "-"} / 数量 ${transfer.totalQuantity ?? 0}`;
  }
  const check = record as InventoryCheckRecord;
  return `${check.checkType || "-"} / 差异 ${check.totalDifference ?? 0}`;
}

function statusOf(record: PrintableInventoryDocument) {
  return "status" in record ? record.status : "-";
}

function row(label: string, value?: string | number | null) {
  const text = value === undefined || value === null || value === "" ? "-" : String(value);
  return `<tr><th style="${cellStyle()} width: 120px;">${escapeHtml(label)}</th><td style="${cellStyle()}">${escapeHtml(text)}</td></tr>`;
}

function cellStyle() {
  return "border: 1px solid #d1d5db; padding: 10px 12px; text-align: left;";
}

function openPrintWindow(title: string, html: string) {
  const popup = window.open("", "_blank", "width=720,height=820");
  if (!popup) return;
  popup.document.write(`<html><head><title>${escapeHtml(title)}</title></head><body>${html}<script>window.onload=()=>window.print()</script></body></html>`);
  popup.document.close();
}

function escapeHtml(value: string) {
  return value
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll("\"", "&quot;")
    .replaceAll("'", "&#039;");
}
