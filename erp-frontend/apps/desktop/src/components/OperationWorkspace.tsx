import { FormEvent, useEffect, useMemo, useState } from "react";
import {
  createInventoryCheck,
  createInventoryIssue,
  createProductionBatch,
  createProductionBox,
  createProductionReport,
  fetchInventoryChecks,
  fetchInventoryIssues,
  fetchInventoryReceipts,
  fetchMaterials,
  fetchProductPackages,
  fetchProducts,
  fetchProductionBatches,
  fetchProductionBoxes,
  receiveProductionBatch,
  startProductionBatch
} from "../api/operations";
import type { UserInfo } from "../types/auth";
import type {
  InventoryCheckRecord,
  InventoryIssueRecord,
  InventoryReceiptRecord,
  MaterialRecord,
  ProductPackageRecord,
  ProductRecord,
  ProductionBatchRecord,
  ProductionBoxRecord
} from "../types/operations";
import { BatchForm } from "./operations/BatchForm";
import { BoxPrintForm } from "./operations/BoxPrintForm";
import { CheckForm } from "./operations/CheckForm";
import { DocumentPrintPanel } from "./operations/DocumentPrintPanel";
import { IssueForm } from "./operations/IssueForm";
import { ProductionPanel } from "./operations/ProductionPanel";
import type { BatchFormState, BoxFormState, CheckFormState, IssueFormState, OperationKey, ReportFormState } from "./operations/operationConfig";
import { operationCards } from "./operations/operationConfig";
import { defaultBatchNo, openPrintWindow, toNumber, today } from "./operations/operationUtils";

export function OperationWorkspace({ loading, user, onLogout }: { loading: boolean; user: UserInfo; onLogout: () => void }) {
  const [activeOperation, setActiveOperation] = useState<OperationKey>("production");
  const [busy, setBusy] = useState(false);
  const [notice, setNotice] = useState<{ type: "success" | "error"; text: string } | null>(null);
  const [products, setProducts] = useState<ProductRecord[]>([]);
  const [materials, setMaterials] = useState<MaterialRecord[]>([]);
  const [batches, setBatches] = useState<ProductionBatchRecord[]>([]);
  const [packages, setPackages] = useState<ProductPackageRecord[]>([]);
  const [boxes, setBoxes] = useState<ProductionBoxRecord[]>([]);
  const [receipts, setReceipts] = useState<InventoryReceiptRecord[]>([]);
  const [issues, setIssues] = useState<InventoryIssueRecord[]>([]);
  const [checks, setChecks] = useState<InventoryCheckRecord[]>([]);
  const [batchNoQuery, setBatchNoQuery] = useState("");
  const [materialQuery, setMaterialQuery] = useState("");
  const [selectedBatchId, setSelectedBatchId] = useState("");
  const [selectedDocumentType, setSelectedDocumentType] = useState<"receipt" | "issue" | "check">("receipt");
  const [batchForm, setBatchForm] = useState<BatchFormState>({
    batchNo: defaultBatchNo(),
    productId: "",
    plannedQuantity: "1",
    unit: "",
    plannedStartDate: today(),
    plannedEndDate: "",
    remark: ""
  });
  const [reportForm, setReportForm] = useState<ReportFormState>({
    reportQuantity: "1",
    goodQuantity: "1",
    defectQuantity: "0",
    operatorName: user.realName || user.username,
    remark: ""
  });
  const [issueForm, setIssueForm] = useState<IssueFormState>({ sourceOrderNo: "", materialId: "", quantity: "1", remark: "" });
  const [checkForm, setCheckForm] = useState<CheckFormState>({ materialId: "", actualQuantity: "0", remark: "" });
  const [boxForm, setBoxForm] = useState<BoxFormState>({ batchId: "", packageId: "", quantity: "1", remark: "" });

  const selectedBatch = useMemo(
    () => batches.find((batch) => batch.id === selectedBatchId) ?? batches.find((batch) => batch.id === boxForm.batchId) ?? null,
    [batches, boxForm.batchId, selectedBatchId]
  );
  const selectedOperation = operationCards.find((operation) => operation.key === activeOperation) ?? operationCards[0];

  useEffect(() => {
    void loadAll();
  }, []);

  useEffect(() => {
    if (!selectedBatch?.productId) {
      setPackages([]);
      return;
    }
    void fetchProductPackages(selectedBatch.productId).then(setPackages).catch(() => setPackages([]));
  }, [selectedBatch?.productId]);

  async function run<T>(action: () => Promise<T>, successText: string) {
    setBusy(true);
    setNotice(null);
    try {
      const result = await action();
      setNotice({ type: "success", text: successText });
      return result;
    } catch (err) {
      setNotice({ type: "error", text: err instanceof Error ? err.message : "操作失败" });
      return null;
    } finally {
      setBusy(false);
    }
  }

  async function loadAll() {
    setBusy(true);
    try {
      const [productPage, materialPage, batchPage, boxPage, receiptPage, issuePage, checkPage] = await Promise.all([
        fetchProducts(),
        fetchMaterials(),
        fetchProductionBatches(),
        fetchProductionBoxes(),
        fetchInventoryReceipts(),
        fetchInventoryIssues(),
        fetchInventoryChecks()
      ]);
      setProducts(productPage.records);
      setMaterials(materialPage.records);
      setBatches(batchPage.records);
      setBoxes(boxPage.records);
      setReceipts(receiptPage.records);
      setIssues(issuePage.records);
      setChecks(checkPage.records);
      if (batchPage.records[0]) {
        setSelectedBatchId(batchPage.records[0].id);
        setBoxForm((value) => ({ ...value, batchId: batchPage.records[0].id }));
      }
    } catch (err) {
      setNotice({ type: "error", text: err instanceof Error ? err.message : "加载作业数据失败" });
    } finally {
      setBusy(false);
    }
  }

  async function refreshBatches(query = batchNoQuery) {
    const [batchPage, boxPage, receiptPage] = await Promise.all([
      fetchProductionBatches(query),
      fetchProductionBoxes(query),
      fetchInventoryReceipts()
    ]);
    setBatches(batchPage.records);
    setBoxes(boxPage.records);
    setReceipts(receiptPage.records);
    if (batchPage.records[0]) {
      setSelectedBatchId(batchPage.records[0].id);
      setBoxForm((value) => ({ ...value, batchId: batchPage.records[0].id }));
    }
  }

  async function refreshMaterials(query = materialQuery) {
    const materialPage = await fetchMaterials(query);
    setMaterials(materialPage.records);
  }

  async function handleCreateBatch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const product = products.find((item) => item.id === batchForm.productId);
    await run(async () => {
      await createProductionBatch({
        batchNo: batchForm.batchNo,
        productId: batchForm.productId,
        plannedQuantity: toNumber(batchForm.plannedQuantity),
        completedQuantity: 0,
        unit: batchForm.unit || product?.unit,
        status: "DRAFT",
        plannedStartDate: batchForm.plannedStartDate,
        plannedEndDate: batchForm.plannedEndDate,
        remark: batchForm.remark
      });
      setBatchForm((value) => ({ ...value, batchNo: defaultBatchNo(), plannedQuantity: "1", remark: "" }));
      await refreshBatches("");
    }, "生产工单已创建");
  }

  async function handleStartBatch() {
    if (!selectedBatch) return;
    await run(async () => {
      await startProductionBatch(selectedBatch.id);
      await refreshBatches();
    }, `工单 ${selectedBatch.batchNo} 已投产`);
  }

  async function handleReport(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedBatch) return;
    await run(async () => {
      await createProductionReport({
        batchId: selectedBatch.id,
        reportQuantity: toNumber(reportForm.reportQuantity),
        goodQuantity: toNumber(reportForm.goodQuantity),
        defectQuantity: toNumber(reportForm.defectQuantity),
        operatorName: reportForm.operatorName,
        remark: reportForm.remark
      });
      await refreshBatches();
    }, "生产报工已提交");
  }

  async function handleReceiveBatch() {
    if (!selectedBatch) return;
    await run(async () => {
      await receiveProductionBatch(selectedBatch.id);
      await refreshBatches();
    }, `工单 ${selectedBatch.batchNo} 已完工入库`);
  }

  async function handleCreateIssue(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    await run(async () => {
      await createInventoryIssue({
        issueType: "MANUAL_OUT",
        sourceOrderNo: issueForm.sourceOrderNo,
        remark: issueForm.remark,
        items: [{ materialId: issueForm.materialId, quantity: toNumber(issueForm.quantity), remark: issueForm.remark }]
      });
      const [issuePage, materialPage] = await Promise.all([fetchInventoryIssues(), fetchMaterials(materialQuery)]);
      setIssues(issuePage.records);
      setMaterials(materialPage.records);
    }, "扫码出库已完成");
  }

  async function handleCreateCheck(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    await run(async () => {
      await createInventoryCheck({
        checkType: "FULL",
        remark: checkForm.remark,
        items: [{ materialId: checkForm.materialId, actualQuantity: toNumber(checkForm.actualQuantity), remark: checkForm.remark }]
      });
      const [checkPage, materialPage] = await Promise.all([fetchInventoryChecks(), fetchMaterials(materialQuery)]);
      setChecks(checkPage.records);
      setMaterials(materialPage.records);
    }, "仓库盘点已提交");
  }

  async function handleCreateBox(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    await run(async () => {
      const box = await createProductionBox({
        batchId: boxForm.batchId,
        packageId: boxForm.packageId,
        quantity: toNumber(boxForm.quantity),
        remark: boxForm.remark
      });
      await refreshBatches();
      if (box.labelHtml) {
        openPrintWindow("箱码标签", box.labelHtml);
      }
    }, "箱码已生成，可预览打印");
  }

  return (
    <main className="desktop-shell desktop-workspace-shell">
      <section className="desktop-card desktop-workspace">
        <div className="desktop-workspace-header">
          <div>
            <span className="desktop-badge">ERP Desktop</span>
            <h1>生产仓储作业台</h1>
            <p>欢迎回来，{user.realName || user.username}。桌面端聚焦工单、生产执行、扫码出入库、盘点和打印。</p>
          </div>
          <button className="desktop-secondary-button" disabled={loading || busy} onClick={onLogout}>
            {loading ? "退出中..." : "退出登录"}
          </button>
        </div>
        {notice ? <div className={notice.type === "success" ? "desktop-alert desktop-alert-success" : "desktop-alert"}>{notice.text}</div> : null}
        <div className="desktop-operation-layout">
          <div className="desktop-operation-grid">
            {operationCards.map((operation) => (
              <button
                key={operation.key}
                className={activeOperation === operation.key ? "desktop-operation-card active" : "desktop-operation-card"}
                type="button"
                onClick={() => setActiveOperation(operation.key)}
              >
                <span>{operation.tag}</span>
                <strong>{operation.title}</strong>
                <small>{operation.description}</small>
              </button>
            ))}
          </div>
          <section className="desktop-operation-panel">
            <span>{selectedOperation.tag}</span>
            <h2>{selectedOperation.title}</h2>
            <p>{selectedOperation.description}</p>
            {activeOperation === "batch" ? <BatchForm busy={busy} form={batchForm} products={products} onChange={setBatchForm} onSubmit={handleCreateBatch} /> : null}
            {activeOperation === "production" || activeOperation === "receipt" ? (
              <ProductionPanel
                activeOperation={activeOperation}
                batches={batches}
                batchNoQuery={batchNoQuery}
                busy={busy}
                reportForm={reportForm}
                selectedBatch={selectedBatch}
                selectedBatchId={selectedBatchId}
                onBatchQueryChange={setBatchNoQuery}
                onBatchSearch={() => void refreshBatches(batchNoQuery)}
                onBatchSelect={(batch) => {
                  setSelectedBatchId(batch.id);
                  setBoxForm((value) => ({ ...value, batchId: batch.id }));
                }}
                onReceive={() => void handleReceiveBatch()}
                onReportChange={setReportForm}
                onReportSubmit={handleReport}
                onStart={() => void handleStartBatch()}
              />
            ) : null}
            {activeOperation === "issue" ? (
              <IssueForm
                busy={busy}
                form={issueForm}
                materialQuery={materialQuery}
                materials={materials}
                onChange={setIssueForm}
                onMaterialQueryChange={setMaterialQuery}
                onMaterialSearch={() => void refreshMaterials(materialQuery)}
                onSubmit={handleCreateIssue}
                recentIssues={issues}
              />
            ) : null}
            {activeOperation === "check" ? (
              <CheckForm
                busy={busy}
                form={checkForm}
                materialQuery={materialQuery}
                materials={materials}
                onChange={setCheckForm}
                onMaterialQueryChange={setMaterialQuery}
                onMaterialSearch={() => void refreshMaterials(materialQuery)}
                onSubmit={handleCreateCheck}
                recentChecks={checks}
              />
            ) : null}
            {activeOperation === "boxPrint" ? (
              <BoxPrintForm
                batches={batches}
                busy={busy}
                form={boxForm}
                packages={packages}
                recentBoxes={boxes}
                onBatchSelect={(batchId) => {
                  setSelectedBatchId(batchId);
                  setBoxForm({ ...boxForm, batchId, packageId: "" });
                }}
                onChange={setBoxForm}
                onSubmit={handleCreateBox}
              />
            ) : null}
            {activeOperation === "documentPrint" ? (
              <DocumentPrintPanel
                checks={checks}
                issues={issues}
                receipts={receipts}
                selectedDocumentType={selectedDocumentType}
                onDocumentTypeChange={setSelectedDocumentType}
              />
            ) : null}
          </section>
        </div>
      </section>
    </main>
  );
}
