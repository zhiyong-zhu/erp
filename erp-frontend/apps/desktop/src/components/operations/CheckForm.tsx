import { FormEvent } from "react";
import type { InventoryCheckRecord, MaterialRecord } from "../../types/operations";
import type { CheckFormState } from "./operationConfig";
import { MaterialSearch, MaterialSelect, RecentRecords } from "./OperationShared";

export function CheckForm({
  busy,
  form,
  materialQuery,
  materials,
  onChange,
  onMaterialQueryChange,
  onMaterialSearch,
  onSubmit,
  recentChecks
}: {
  busy: boolean;
  form: CheckFormState;
  materialQuery: string;
  materials: MaterialRecord[];
  onChange: (form: CheckFormState) => void;
  onMaterialQueryChange: (value: string) => void;
  onMaterialSearch: () => void;
  onSubmit: (event: FormEvent<HTMLFormElement>) => void;
  recentChecks: InventoryCheckRecord[];
}) {
  return (
    <form className="desktop-mini-form desktop-panel-form" onSubmit={onSubmit}>
      <MaterialSearch query={materialQuery} busy={busy} onQueryChange={onMaterialQueryChange} onSearch={onMaterialSearch} />
      <MaterialSelect materials={materials} value={form.materialId} onChange={(materialId) => onChange({ ...form, materialId })} stockLabel="账面" />
      <label>实盘数量<input min="0" required step="0.0001" type="number" value={form.actualQuantity} onChange={(event) => onChange({ ...form, actualQuantity: event.target.value })} /></label>
      <label className="desktop-field-wide">备注<input value={form.remark} onChange={(event) => onChange({ ...form, remark: event.target.value })} /></label>
      <button className="desktop-primary-button" disabled={busy} type="submit">提交盘点</button>
      <RecentRecords title="最近盘点单" records={recentChecks.map((item) => `${item.checkNo} · 差异 ${item.totalDifference} · ${item.status}`)} />
    </form>
  );
}
