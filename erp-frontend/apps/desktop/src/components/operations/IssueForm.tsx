import { FormEvent } from "react";
import type { InventoryIssueRecord, MaterialRecord } from "../../types/operations";
import type { IssueFormState } from "./operationConfig";
import { MaterialSearch, MaterialSelect, RecentRecords } from "./OperationShared";

export function IssueForm({
  busy,
  form,
  materialQuery,
  materials,
  onChange,
  onMaterialQueryChange,
  onMaterialSearch,
  onSubmit,
  recentIssues
}: {
  busy: boolean;
  form: IssueFormState;
  materialQuery: string;
  materials: MaterialRecord[];
  onChange: (form: IssueFormState) => void;
  onMaterialQueryChange: (value: string) => void;
  onMaterialSearch: () => void;
  onSubmit: (event: FormEvent<HTMLFormElement>) => void;
  recentIssues: InventoryIssueRecord[];
}) {
  return (
    <form className="desktop-mini-form desktop-panel-form" onSubmit={onSubmit}>
      <MaterialSearch query={materialQuery} busy={busy} onQueryChange={onMaterialQueryChange} onSearch={onMaterialSearch} />
      <MaterialSelect materials={materials} value={form.materialId} onChange={(materialId) => onChange({ ...form, materialId })} stockLabel="库存" />
      <label>出库数量<input min="0.0001" required step="0.0001" type="number" value={form.quantity} onChange={(event) => onChange({ ...form, quantity: event.target.value })} /></label>
      <label>来源单号<input value={form.sourceOrderNo} onChange={(event) => onChange({ ...form, sourceOrderNo: event.target.value })} /></label>
      <label className="desktop-field-wide">备注<input value={form.remark} onChange={(event) => onChange({ ...form, remark: event.target.value })} /></label>
      <button className="desktop-primary-button" disabled={busy} type="submit">确认扫码出库</button>
      <RecentRecords title="最近出库单" records={recentIssues.map((item) => `${item.issueNo} · ${item.totalQuantity} · ${item.status}`)} />
    </form>
  );
}
