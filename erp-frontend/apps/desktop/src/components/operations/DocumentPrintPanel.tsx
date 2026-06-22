import type { InventoryCheckRecord, InventoryIssueRecord, InventoryReceiptRecord } from "../../types/operations";
import { DocumentList } from "./OperationShared";

export function DocumentPrintPanel({
  checks,
  issues,
  receipts,
  selectedDocumentType,
  onDocumentTypeChange
}: {
  checks: InventoryCheckRecord[];
  issues: InventoryIssueRecord[];
  receipts: InventoryReceiptRecord[];
  selectedDocumentType: "receipt" | "issue" | "check";
  onDocumentTypeChange: (type: "receipt" | "issue" | "check") => void;
}) {
  return (
    <div className="desktop-panel-stack">
      <label className="desktop-doc-select">单据类型<select value={selectedDocumentType} onChange={(event) => onDocumentTypeChange(event.target.value as "receipt" | "issue" | "check")}>
        <option value="receipt">入库单</option>
        <option value="issue">出库单</option>
        <option value="check">盘点单</option>
      </select></label>
      <DocumentList type={selectedDocumentType} receipts={receipts} issues={issues} checks={checks} />
    </div>
  );
}
