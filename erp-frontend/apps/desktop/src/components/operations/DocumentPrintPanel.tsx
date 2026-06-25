import type { InventoryCheckRecord, InventoryIssueRecord, InventoryReceiptRecord, InventoryTransferRecord } from "../../types/operations";
import { DocumentList } from "./OperationShared";
import type { PrintableDocumentType } from "./operationUtils";

export function DocumentPrintPanel({
  checks,
  issues,
  receipts,
  transfers,
  selectedDocumentType,
  onDocumentTypeChange
}: {
  checks: InventoryCheckRecord[];
  issues: InventoryIssueRecord[];
  receipts: InventoryReceiptRecord[];
  transfers: InventoryTransferRecord[];
  selectedDocumentType: PrintableDocumentType;
  onDocumentTypeChange: (type: PrintableDocumentType) => void;
}) {
  return (
    <div className="desktop-panel-stack">
      <label className="desktop-doc-select">单据类型<select value={selectedDocumentType} onChange={(event) => onDocumentTypeChange(event.target.value as PrintableDocumentType)}>
        <option value="receipt">入库单</option>
        <option value="issue">出库单</option>
        <option value="transfer">调拨单</option>
        <option value="check">盘点单</option>
      </select></label>
      <DocumentList type={selectedDocumentType} receipts={receipts} issues={issues} transfers={transfers} checks={checks} />
    </div>
  );
}
