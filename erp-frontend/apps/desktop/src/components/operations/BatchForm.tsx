import { FormEvent } from "react";
import type { ProductRecord } from "../../types/operations";
import type { BatchFormState } from "./operationConfig";

export function BatchForm({
  busy,
  form,
  products,
  onChange,
  onSubmit
}: {
  busy: boolean;
  form: BatchFormState;
  products: ProductRecord[];
  onChange: (form: BatchFormState) => void;
  onSubmit: (event: FormEvent<HTMLFormElement>) => void;
}) {
  return (
    <form className="desktop-mini-form desktop-panel-form" onSubmit={onSubmit}>
      <label>工单号<input required value={form.batchNo} onChange={(event) => onChange({ ...form, batchNo: event.target.value })} /></label>
      <label>产品<select required value={form.productId} onChange={(event) => {
        const product = products.find((item) => item.id === event.target.value);
        onChange({ ...form, productId: event.target.value, unit: product?.unit ?? "" });
      }}>
        <option value="">请选择产品</option>
        {products.map((product) => <option key={product.id} value={product.id}>{product.code} / {product.name}</option>)}
      </select></label>
      <label>计划数量<input required min="0.0001" step="0.0001" type="number" value={form.plannedQuantity} onChange={(event) => onChange({ ...form, plannedQuantity: event.target.value })} /></label>
      <label>单位<input value={form.unit} onChange={(event) => onChange({ ...form, unit: event.target.value })} /></label>
      <label>计划开始<input type="date" value={form.plannedStartDate} onChange={(event) => onChange({ ...form, plannedStartDate: event.target.value })} /></label>
      <label>计划结束<input type="date" value={form.plannedEndDate} onChange={(event) => onChange({ ...form, plannedEndDate: event.target.value })} /></label>
      <label className="desktop-field-wide">备注<input value={form.remark} onChange={(event) => onChange({ ...form, remark: event.target.value })} /></label>
      <button className="desktop-primary-button" disabled={busy} type="submit">创建工单</button>
    </form>
  );
}
