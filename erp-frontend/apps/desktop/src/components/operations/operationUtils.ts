import type { InventoryCheckRecord, InventoryIssueRecord, InventoryReceiptRecord, InventoryTransferRecord } from "../../types/operations";

export function defaultBatchNo() {
  const now = new Date();
  return `WO-${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, "0")}${String(now.getDate()).padStart(2, "0")}-${String(now.getHours()).padStart(2, "0")}${String(now.getMinutes()).padStart(2, "0")}${String(now.getSeconds()).padStart(2, "0")}`;
}

export function today() {
  return new Date().toISOString().slice(0, 10);
}

export function toNumber(value: string) {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
}

export function formatStatus(status: string) {
  const statusMap: Record<string, string> = {
    DRAFT: "草稿",
    RELEASED: "已下达",
    IN_PROGRESS: "生产中",
    COMPLETED: "已完工",
    CLOSED: "已关闭"
  };
  return statusMap[status] ?? status;
}

export type PrintableDocumentType = "receipt" | "issue" | "transfer" | "check";
export type PrintableDocumentRecord = InventoryReceiptRecord | InventoryIssueRecord | InventoryTransferRecord | InventoryCheckRecord;

export function documentNo(type: PrintableDocumentType, record: PrintableDocumentRecord) {
  if (type === "receipt") return (record as InventoryReceiptRecord).receiptNo;
  if (type === "issue") return (record as InventoryIssueRecord).issueNo;
  if (type === "transfer") return (record as InventoryTransferRecord).transferNo;
  return (record as InventoryCheckRecord).checkNo;
}

export function documentSummary(type: PrintableDocumentType, record: PrintableDocumentRecord) {
  if (type === "receipt") {
    const receipt = record as InventoryReceiptRecord;
    return `${receipt.sourceType} · ${receipt.sourceOrderNo ?? "-"} · ${receipt.status}`;
  }
  if (type === "issue") {
    const issue = record as InventoryIssueRecord;
    return `${issue.issueType} · 数量 ${issue.totalQuantity} · ${issue.status}`;
  }
  if (type === "transfer") {
    const transfer = record as InventoryTransferRecord;
    return `${transfer.fromLocation} → ${transfer.toLocation} · 数量 ${transfer.totalQuantity} · ${transfer.status}`;
  }
  const check = record as InventoryCheckRecord;
  return `${check.checkType} · 差异 ${check.totalDifference} · ${check.status}`;
}

export function printDocument(type: PrintableDocumentType, record: PrintableDocumentRecord) {
  const title = type === "receipt" ? "入库单" : type === "issue" ? "出库单" : type === "transfer" ? "调拨单" : "盘点单";
  const html = `
    <main style="font-family: system-ui, sans-serif; padding: 24px; color: #111827;">
      <h1 style="text-align:center;margin:0 0 24px;">${title}</h1>
      <table style="width:100%;border-collapse:collapse;font-size:14px;">
        <tbody>
          <tr><th style="${cellStyle()}">单号</th><td style="${cellStyle()}">${documentNo(type, record)}</td></tr>
          <tr><th style="${cellStyle()}">摘要</th><td style="${cellStyle()}">${documentSummary(type, record)}</td></tr>
          <tr><th style="${cellStyle()}">备注</th><td style="${cellStyle()}">${"remark" in record && record.remark ? record.remark : "-"}</td></tr>
          <tr><th style="${cellStyle()}">打印时间</th><td style="${cellStyle()}">${new Date().toLocaleString()}</td></tr>
        </tbody>
      </table>
      <div style="display:flex;justify-content:space-between;margin-top:48px;">
        <span>制单：</span><span>复核：</span><span>经办：</span>
      </div>
    </main>
  `;
  openPrintWindow(title, html);
}

function cellStyle() {
  return "border:1px solid #d1d5db;padding:10px;text-align:left;";
}

export function openPrintWindow(title: string, html: string) {
  const popup = window.open("", "_blank", "width=640,height=720");
  if (!popup) return;
  popup.document.write(`<html><head><title>${title}</title></head><body>${html}<script>window.onload=()=>window.print()</script></body></html>`);
  popup.document.close();
}
