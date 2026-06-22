import { FormEvent } from "react";
import type { ProductPackageRecord, ProductionBatchRecord, ProductionBoxRecord } from "../../types/operations";
import type { BoxFormState } from "./operationConfig";
import { RecentRecords } from "./OperationShared";

export function BoxPrintForm({
  batches,
  busy,
  form,
  packages,
  recentBoxes,
  onBatchSelect,
  onChange,
  onSubmit
}: {
  batches: ProductionBatchRecord[];
  busy: boolean;
  form: BoxFormState;
  packages: ProductPackageRecord[];
  recentBoxes: ProductionBoxRecord[];
  onBatchSelect: (batchId: string) => void;
  onChange: (form: BoxFormState) => void;
  onSubmit: (event: FormEvent<HTMLFormElement>) => void;
}) {
  return (
    <form className="desktop-mini-form desktop-panel-form" onSubmit={onSubmit}>
      <label className="desktop-field-wide">工单<select required value={form.batchId} onChange={(event) => onBatchSelect(event.target.value)}>
        <option value="">请选择工单</option>
        {batches.map((batch) => <option key={batch.id} value={batch.id}>{batch.batchNo} / {batch.productName ?? batch.productCode}</option>)}
      </select></label>
      <label className="desktop-field-wide">包装规格<select required value={form.packageId} onChange={(event) => onChange({ ...form, packageId: event.target.value })}>
        <option value="">请选择包装规格</option>
        {packages.map((item) => <option key={item.id} value={item.id}>{item.name} / 每箱 {item.quantity}</option>)}
      </select></label>
      <label>装箱数量<input min="0.0001" required step="0.0001" type="number" value={form.quantity} onChange={(event) => onChange({ ...form, quantity: event.target.value })} /></label>
      <label className="desktop-field-wide">备注<input value={form.remark} onChange={(event) => onChange({ ...form, remark: event.target.value })} /></label>
      <button className="desktop-primary-button" disabled={busy} type="submit">生成并打印箱码</button>
      <RecentRecords title="最近箱码" records={recentBoxes.map((item) => `${item.boxCode} · ${item.batchNo} · ${item.quantity}`)} />
    </form>
  );
}
