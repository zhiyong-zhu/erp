import { FormEvent } from "react";
import type { ProductionBatchRecord } from "../../types/operations";
import type { OperationKey, ReportFormState } from "./operationConfig";
import { BatchPicker, SelectedBatchCard } from "./OperationShared";

export function ProductionPanel({
  activeOperation,
  batches,
  batchNoQuery,
  busy,
  reportForm,
  selectedBatch,
  selectedBatchId,
  onBatchQueryChange,
  onBatchSearch,
  onBatchSelect,
  onReceive,
  onReportChange,
  onReportSubmit,
  onStart
}: {
  activeOperation: Extract<OperationKey, "production" | "receipt">;
  batches: ProductionBatchRecord[];
  batchNoQuery: string;
  busy: boolean;
  reportForm: ReportFormState;
  selectedBatch: ProductionBatchRecord | null;
  selectedBatchId: string;
  onBatchQueryChange: (value: string) => void;
  onBatchSearch: () => void;
  onBatchSelect: (batch: ProductionBatchRecord) => void;
  onReceive: () => void;
  onReportChange: (form: ReportFormState) => void;
  onReportSubmit: (event: FormEvent<HTMLFormElement>) => void;
  onStart: () => void;
}) {
  return (
    <div className="desktop-panel-stack">
      <div className="desktop-inline-search">
        <input placeholder="输入/扫描工单号" value={batchNoQuery} onChange={(event) => onBatchQueryChange(event.target.value)} />
        <button className="desktop-secondary-button" disabled={busy} type="button" onClick={onBatchSearch}>查询</button>
      </div>
      <BatchPicker batches={batches} selectedId={selectedBatchId} onSelect={onBatchSelect} />
      {selectedBatch ? <SelectedBatchCard batch={selectedBatch} /> : <p>请先查询并选择工单。</p>}
      {activeOperation === "production" ? (
        <>
          <div className="desktop-list-actions">
            <button className="desktop-primary-button" disabled={busy || !selectedBatch} type="button" onClick={onStart}>投产</button>
            <button className="desktop-secondary-button" disabled={busy || !selectedBatch} type="button" onClick={onReceive}>完工入库</button>
          </div>
          <form className="desktop-mini-form desktop-panel-form" onSubmit={onReportSubmit}>
            <label>报工数量<input min="0.0001" required step="0.0001" type="number" value={reportForm.reportQuantity} onChange={(event) => onReportChange({ ...reportForm, reportQuantity: event.target.value })} /></label>
            <label>良品数量<input min="0" step="0.0001" type="number" value={reportForm.goodQuantity} onChange={(event) => onReportChange({ ...reportForm, goodQuantity: event.target.value })} /></label>
            <label>不良数量<input min="0" step="0.0001" type="number" value={reportForm.defectQuantity} onChange={(event) => onReportChange({ ...reportForm, defectQuantity: event.target.value })} /></label>
            <label>操作人<input value={reportForm.operatorName} onChange={(event) => onReportChange({ ...reportForm, operatorName: event.target.value })} /></label>
            <label className="desktop-field-wide">备注<input value={reportForm.remark} onChange={(event) => onReportChange({ ...reportForm, remark: event.target.value })} /></label>
            <button className="desktop-primary-button" disabled={busy || !selectedBatch} type="submit">提交报工</button>
          </form>
        </>
      ) : (
        <button className="desktop-primary-button" disabled={busy || !selectedBatch} type="button" onClick={onReceive}>确认扫码入库</button>
      )}
    </div>
  );
}
