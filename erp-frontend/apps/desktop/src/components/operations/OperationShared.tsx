import type {
  InventoryCheckRecord,
  InventoryIssueRecord,
  InventoryReceiptRecord,
  MaterialRecord,
  ProductionBatchRecord
} from "../../types/operations";
import { documentNo, documentSummary, formatStatus, printDocument } from "./operationUtils";

export function MaterialSearch({ busy, query, onQueryChange, onSearch }: { busy: boolean; query: string; onQueryChange: (value: string) => void; onSearch: () => void }) {
  return (
    <div className="desktop-inline-search desktop-field-wide">
      <input placeholder="输入/扫描物料编码或名称" value={query} onChange={(event) => onQueryChange(event.target.value)} />
      <button className="desktop-secondary-button" disabled={busy} type="button" onClick={onSearch}>查询</button>
    </div>
  );
}

export function MaterialSelect({ materials, value, onChange, stockLabel }: { materials: MaterialRecord[]; value: string; onChange: (materialId: string) => void; stockLabel: string }) {
  return (
    <label className="desktop-field-wide">物料<select required value={value} onChange={(event) => onChange(event.target.value)}>
      <option value="">请选择物料</option>
      {materials.map((material) => (
        <option key={material.id} value={material.id}>
          {material.code} / {material.name}（{stockLabel} {material.currentStock ?? 0}{material.unit}）
        </option>
      ))}
    </select></label>
  );
}

export function BatchPicker({ batches, selectedId, onSelect }: { batches: ProductionBatchRecord[]; selectedId: string; onSelect: (batch: ProductionBatchRecord) => void }) {
  return (
    <div className="desktop-record-grid">
      {batches.map((batch) => (
        <button key={batch.id} className={selectedId === batch.id ? "desktop-record-card active" : "desktop-record-card"} type="button" onClick={() => onSelect(batch)}>
          <strong>{batch.batchNo}</strong>
          <span>{batch.productName ?? batch.productCode ?? batch.productId}</span>
          <small>{formatStatus(batch.status)} · {batch.completedQuantity ?? 0}/{batch.plannedQuantity}{batch.unit ?? ""}</small>
        </button>
      ))}
    </div>
  );
}

export function SelectedBatchCard({ batch }: { batch: ProductionBatchRecord }) {
  return (
    <div className="desktop-selected-card">
      <strong>{batch.batchNo}</strong>
      <span>{batch.productName ?? batch.productCode ?? batch.productId}</span>
      <small>状态：{formatStatus(batch.status)}；完成：{batch.completedQuantity ?? 0}/{batch.plannedQuantity}{batch.unit ?? ""}</small>
    </div>
  );
}

export function RecentRecords({ title, records }: { title: string; records: string[] }) {
  return (
    <div className="desktop-recent desktop-field-wide">
      <strong>{title}</strong>
      {records.length ? records.slice(0, 5).map((record) => <span key={record}>{record}</span>) : <span>暂无记录</span>}
    </div>
  );
}

export function DocumentList({
  type,
  receipts,
  issues,
  checks
}: {
  type: "receipt" | "issue" | "check";
  receipts: InventoryReceiptRecord[];
  issues: InventoryIssueRecord[];
  checks: InventoryCheckRecord[];
}) {
  const records = type === "receipt" ? receipts : type === "issue" ? issues : checks;
  return (
    <div className="desktop-record-list">
      {records.length ? records.map((record) => (
        <button key={record.id} className="desktop-list-item" type="button" onClick={() => printDocument(type, record)}>
          <div>
            <strong>{documentNo(type, record)}</strong>
            <span>{documentSummary(type, record)}</span>
          </div>
          <span>预览打印</span>
        </button>
      )) : <p>暂无单据。</p>}
    </div>
  );
}
