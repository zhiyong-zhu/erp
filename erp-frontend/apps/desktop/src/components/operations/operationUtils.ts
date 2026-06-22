import type { InventoryCheckRecord, InventoryIssueRecord, InventoryReceiptRecord } from "../../types/operations";

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

export function documentNo(type: "receipt" | "issue" | "check", record: InventoryReceiptRecord | InventoryIssueRecord | InventoryCheckRecord) {
  if (type === "receipt") return (record as InventoryReceiptRecord).receiptNo;
  if (type === "issue") return (record as InventoryIssueRecord).issueNo;
  return (record as InventoryCheckRecord).checkNo;
}

export function documentSummary(type: "receipt" | "issue" | "check", record: InventoryReceiptRecord | InventoryIssueRecord | InventoryCheckRecord) {
  if (type === "receipt") {
    const receipt = record as InventoryReceiptRecord;
    return `${receipt.sourceType} · ${receipt.sourceOrderNo ?? "-"} · ${receipt.status}`;
  }
  if (type === "issue") {
    const issue = record as InventoryIssueRecord;
    return `${issue.issueType} · 数量 ${issue.totalQuantity} · ${issue.status}`;
  }
  const check = record as InventoryCheckRecord;
  return `${check.checkType} · 差异 ${check.totalDifference} · ${check.status}`;
}

export function printDocument(type: "receipt" | "issue" | "check", record: InventoryReceiptRecord | InventoryIssueRecord | InventoryCheckRecord) {
  const title = type === "receipt" ? "入库单" : type === "issue" ? "出库单" : "盘点单";
  const html = `
    <main style="font-family: system-ui, sans-serif; padding: 24px;">
      <h1>${title}</h1>
      <p>单号：${documentNo(type, record)}</p>
      <p>${documentSummary(type, record)}</p>
      <p>打印时间：${new Date().toLocaleString()}</p>
    </main>
  `;
  openPrintWindow(title, html);
}

export function openPrintWindow(title: string, html: string) {
  const popup = window.open("", "_blank", "width=640,height=720");
  if (!popup) return;
  popup.document.write(`<html><head><title>${title}</title></head><body>${html}<script>window.onload=()=>window.print()</script></body></html>`);
  popup.document.close();
}
