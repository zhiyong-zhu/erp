import React, { FormEvent, useEffect, useMemo, useState } from "react";
import ReactDOM from "react-dom/client";
import { APP_NAME, formatTitle } from "@erp/shared";
import { fetchUserInfo, login, logout } from "./api/auth";
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
} from "./api/operations";
import { saveAccessToken, saveUser } from "./store/auth";
import type { UserInfo } from "./types/auth";
import type {
  InventoryCheckRecord,
  InventoryIssueRecord,
  InventoryReceiptRecord,
  MaterialRecord,
  ProductPackageRecord,
  ProductRecord,
  ProductionBatchRecord,
  ProductionBoxRecord
} from "./types/operations";
import "./styles.css";

document.title = formatTitle(`${APP_NAME} Desktop`);

type OperationKey = "batch" | "production" | "receipt" | "issue" | "check" | "boxPrint" | "documentPrint";

const operationCards: Array<{ key: OperationKey; title: string; description: string; tag: string }> = [
  { key: "batch", title: "创建工单", description: "按产品、数量、计划时间创建生产工单。", tag: "生产准备" },
  { key: "production", title: "生产执行", description: "投产、报工、完工入库和进度刷新。", tag: "车间操作" },
  { key: "receipt", title: "扫码入库", description: "输入/扫描工单或箱码，确认完工入库。", tag: "仓库操作" },
  { key: "issue", title: "扫码出库", description: "输入/扫描物料，创建出库单并扣减库存。", tag: "仓库操作" },
  { key: "check", title: "仓库盘点", description: "输入实盘数量，生成盘盈/盘亏流水。", tag: "库存校准" },
  { key: "boxPrint", title: "箱码打印", description: "按工单和包装规格生成箱码并预览打印。", tag: "标签打印" },
  { key: "documentPrint", title: "出入库单据打印", description: "预览并打印入库、出库、盘点单据。", tag: "单据打印" }
];

function App() {
  const [username, setUsername] = useState("admin");
  const [password, setPassword] = useState("password");
  const [user, setUser] = useState<UserInfo | null>(null);
  const [loading, setLoading] = useState(false);
  const [booting, setBooting] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchUserInfo()
      .then((info) => {
        saveUser(info);
        setUser(info);
      })
      .catch(() => {
        saveAccessToken(null);
        saveUser(null);
      })
      .finally(() => setBooting(false));
  }, []);

  async function handleLogin(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const result = await login({ username, password });
      saveAccessToken(result.accessToken);
      const info = await fetchUserInfo();
      saveUser(info);
      setUser(info);
    } catch (err) {
      setError(err instanceof Error ? err.message : "登录失败，请检查用户名和密码");
    } finally {
      setLoading(false);
    }
  }

  async function handleLogout() {
    setLoading(true);
    try {
      await logout();
    } finally {
      saveAccessToken(null);
      saveUser(null);
      setUser(null);
      setLoading(false);
    }
  }

  if (booting) {
    return <main className="desktop-shell"><div className="desktop-card">正在检查登录状态...</div></main>;
  }

  if (user) {
    return <OperationWorkspace loading={loading} user={user} onLogout={() => void handleLogout()} />;
  }

  return (
    <main className="desktop-shell">
      <section className="desktop-card desktop-login-card">
        <span className="desktop-badge">ERP Desktop</span>
        <h1>全渠道 ERP 桌面端登录</h1>
        <p>连接本机后端服务，使用管理员账号进入桌面作业台。</p>
        {error ? <div className="desktop-alert">{error}</div> : null}
        <form className="desktop-form" onSubmit={(event) => void handleLogin(event)}>
          <label>用户名<input value={username} onChange={(event) => setUsername(event.target.value)} placeholder="请输入用户名" required /></label>
          <label>密码<input value={password} onChange={(event) => setPassword(event.target.value)} placeholder="请输入密码" required type="password" /></label>
          <button className="desktop-primary-button" disabled={loading} type="submit">{loading ? "登录中..." : "登录"}</button>
        </form>
      </section>
    </main>
  );
}

function OperationWorkspace({ loading, user, onLogout }: { loading: boolean; user: UserInfo; onLogout: () => void }) {
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
  const [batchForm, setBatchForm] = useState({
    batchNo: defaultBatchNo(),
    productId: "",
    plannedQuantity: "1",
    unit: "",
    plannedStartDate: today(),
    plannedEndDate: "",
    remark: ""
  });
  const [reportForm, setReportForm] = useState({
    reportQuantity: "1",
    goodQuantity: "1",
    defectQuantity: "0",
    operatorName: user.realName || user.username,
    remark: ""
  });
  const [issueForm, setIssueForm] = useState({
    sourceOrderNo: "",
    materialId: "",
    quantity: "1",
    remark: ""
  });
  const [checkForm, setCheckForm] = useState({
    materialId: "",
    actualQuantity: "0",
    remark: ""
  });
  const [boxForm, setBoxForm] = useState({
    batchId: "",
    packageId: "",
    quantity: "1",
    remark: ""
  });

  const selectedBatch = useMemo(
    () => batches.find((batch) => batch.id === selectedBatchId) ?? batches.find((batch) => batch.id === boxForm.batchId) ?? null,
    [batches, boxForm.batchId, selectedBatchId]
  );

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
      const firstBatch = batchPage.records[0];
      if (firstBatch) {
        setSelectedBatchId(firstBatch.id);
        setBoxForm((value) => ({ ...value, batchId: firstBatch.id }));
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

  const selectedOperation = operationCards.find((operation) => operation.key === activeOperation) ?? operationCards[0];

  return (
    <main className="desktop-shell desktop-workspace-shell">
      <section className="desktop-card desktop-workspace">
        <div className="desktop-workspace-header">
          <div>
            <span className="desktop-badge">ERP Desktop</span>
            <h1>生产仓储作业台</h1>
            <p>欢迎回来，{user.realName || user.username}。桌面端聚焦工单、生产执行、扫码出入库、盘点和打印。</p>
          </div>
          <button className="desktop-secondary-button" disabled={loading || busy} onClick={onLogout}>{loading ? "退出中..." : "退出登录"}</button>
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
            {activeOperation === "batch" ? (
              <form className="desktop-mini-form desktop-panel-form" onSubmit={(event) => void handleCreateBatch(event)}>
                <label>工单号<input required value={batchForm.batchNo} onChange={(event) => setBatchForm({ ...batchForm, batchNo: event.target.value })} /></label>
                <label>产品<select required value={batchForm.productId} onChange={(event) => {
                  const product = products.find((item) => item.id === event.target.value);
                  setBatchForm({ ...batchForm, productId: event.target.value, unit: product?.unit ?? "" });
                }}>
                  <option value="">请选择产品</option>
                  {products.map((product) => <option key={product.id} value={product.id}>{product.code} / {product.name}</option>)}
                </select></label>
                <label>计划数量<input required min="0.0001" step="0.0001" type="number" value={batchForm.plannedQuantity} onChange={(event) => setBatchForm({ ...batchForm, plannedQuantity: event.target.value })} /></label>
                <label>单位<input value={batchForm.unit} onChange={(event) => setBatchForm({ ...batchForm, unit: event.target.value })} /></label>
                <label>计划开始<input type="date" value={batchForm.plannedStartDate} onChange={(event) => setBatchForm({ ...batchForm, plannedStartDate: event.target.value })} /></label>
                <label>计划结束<input type="date" value={batchForm.plannedEndDate} onChange={(event) => setBatchForm({ ...batchForm, plannedEndDate: event.target.value })} /></label>
                <label className="desktop-field-wide">备注<input value={batchForm.remark} onChange={(event) => setBatchForm({ ...batchForm, remark: event.target.value })} /></label>
                <button className="desktop-primary-button" disabled={busy} type="submit">创建工单</button>
              </form>
            ) : null}
            {activeOperation === "production" || activeOperation === "receipt" ? (
              <div className="desktop-panel-stack">
                <div className="desktop-inline-search">
                  <input placeholder="输入/扫描工单号" value={batchNoQuery} onChange={(event) => setBatchNoQuery(event.target.value)} />
                  <button className="desktop-secondary-button" disabled={busy} type="button" onClick={() => void refreshBatches(batchNoQuery)}>查询</button>
                </div>
                <BatchPicker batches={batches} selectedId={selectedBatchId} onSelect={(batch) => {
                  setSelectedBatchId(batch.id);
                  setBoxForm((value) => ({ ...value, batchId: batch.id }));
                }} />
                {selectedBatch ? <SelectedBatchCard batch={selectedBatch} /> : <p>请先查询并选择工单。</p>}
                {activeOperation === "production" ? (
                  <>
                    <div className="desktop-list-actions">
                      <button className="desktop-primary-button" disabled={busy || !selectedBatch} type="button" onClick={() => void handleStartBatch()}>投产</button>
                      <button className="desktop-secondary-button" disabled={busy || !selectedBatch} type="button" onClick={() => void handleReceiveBatch()}>完工入库</button>
                    </div>
                    <form className="desktop-mini-form desktop-panel-form" onSubmit={(event) => void handleReport(event)}>
                      <label>报工数量<input min="0.0001" required step="0.0001" type="number" value={reportForm.reportQuantity} onChange={(event) => setReportForm({ ...reportForm, reportQuantity: event.target.value })} /></label>
                      <label>良品数量<input min="0" step="0.0001" type="number" value={reportForm.goodQuantity} onChange={(event) => setReportForm({ ...reportForm, goodQuantity: event.target.value })} /></label>
                      <label>不良数量<input min="0" step="0.0001" type="number" value={reportForm.defectQuantity} onChange={(event) => setReportForm({ ...reportForm, defectQuantity: event.target.value })} /></label>
                      <label>操作人<input value={reportForm.operatorName} onChange={(event) => setReportForm({ ...reportForm, operatorName: event.target.value })} /></label>
                      <label className="desktop-field-wide">备注<input value={reportForm.remark} onChange={(event) => setReportForm({ ...reportForm, remark: event.target.value })} /></label>
                      <button className="desktop-primary-button" disabled={busy || !selectedBatch} type="submit">提交报工</button>
                    </form>
                  </>
                ) : (
                  <button className="desktop-primary-button" disabled={busy || !selectedBatch} type="button" onClick={() => void handleReceiveBatch()}>确认扫码入库</button>
                )}
              </div>
            ) : null}
            {activeOperation === "issue" ? (
              <form className="desktop-mini-form desktop-panel-form" onSubmit={(event) => void handleCreateIssue(event)}>
                <div className="desktop-inline-search desktop-field-wide">
                  <input placeholder="输入/扫描物料编码或名称" value={materialQuery} onChange={(event) => setMaterialQuery(event.target.value)} />
                  <button className="desktop-secondary-button" disabled={busy} type="button" onClick={() => void refreshMaterials(materialQuery)}>查询</button>
                </div>
                <label className="desktop-field-wide">物料<select required value={issueForm.materialId} onChange={(event) => setIssueForm({ ...issueForm, materialId: event.target.value })}>
                  <option value="">请选择物料</option>
                  {materials.map((material) => <option key={material.id} value={material.id}>{material.code} / {material.name}（库存 {material.currentStock ?? 0}{material.unit}）</option>)}
                </select></label>
                <label>出库数量<input min="0.0001" required step="0.0001" type="number" value={issueForm.quantity} onChange={(event) => setIssueForm({ ...issueForm, quantity: event.target.value })} /></label>
                <label>来源单号<input value={issueForm.sourceOrderNo} onChange={(event) => setIssueForm({ ...issueForm, sourceOrderNo: event.target.value })} /></label>
                <label className="desktop-field-wide">备注<input value={issueForm.remark} onChange={(event) => setIssueForm({ ...issueForm, remark: event.target.value })} /></label>
                <button className="desktop-primary-button" disabled={busy} type="submit">确认扫码出库</button>
                <RecentRecords title="最近出库单" records={issues.map((item) => `${item.issueNo} · ${item.totalQuantity} · ${item.status}`)} />
              </form>
            ) : null}
            {activeOperation === "check" ? (
              <form className="desktop-mini-form desktop-panel-form" onSubmit={(event) => void handleCreateCheck(event)}>
                <div className="desktop-inline-search desktop-field-wide">
                  <input placeholder="输入/扫描物料编码或名称" value={materialQuery} onChange={(event) => setMaterialQuery(event.target.value)} />
                  <button className="desktop-secondary-button" disabled={busy} type="button" onClick={() => void refreshMaterials(materialQuery)}>查询</button>
                </div>
                <label className="desktop-field-wide">物料<select required value={checkForm.materialId} onChange={(event) => setCheckForm({ ...checkForm, materialId: event.target.value })}>
                  <option value="">请选择物料</option>
                  {materials.map((material) => <option key={material.id} value={material.id}>{material.code} / {material.name}（账面 {material.currentStock ?? 0}{material.unit}）</option>)}
                </select></label>
                <label>实盘数量<input min="0" required step="0.0001" type="number" value={checkForm.actualQuantity} onChange={(event) => setCheckForm({ ...checkForm, actualQuantity: event.target.value })} /></label>
                <label className="desktop-field-wide">备注<input value={checkForm.remark} onChange={(event) => setCheckForm({ ...checkForm, remark: event.target.value })} /></label>
                <button className="desktop-primary-button" disabled={busy} type="submit">提交盘点</button>
                <RecentRecords title="最近盘点单" records={checks.map((item) => `${item.checkNo} · 差异 ${item.totalDifference} · ${item.status}`)} />
              </form>
            ) : null}
            {activeOperation === "boxPrint" ? (
              <form className="desktop-mini-form desktop-panel-form" onSubmit={(event) => void handleCreateBox(event)}>
                <label className="desktop-field-wide">工单<select required value={boxForm.batchId} onChange={(event) => {
                  setSelectedBatchId(event.target.value);
                  setBoxForm({ ...boxForm, batchId: event.target.value, packageId: "" });
                }}>
                  <option value="">请选择工单</option>
                  {batches.map((batch) => <option key={batch.id} value={batch.id}>{batch.batchNo} / {batch.productName ?? batch.productCode}</option>)}
                </select></label>
                <label className="desktop-field-wide">包装规格<select required value={boxForm.packageId} onChange={(event) => setBoxForm({ ...boxForm, packageId: event.target.value })}>
                  <option value="">请选择包装规格</option>
                  {packages.map((item) => <option key={item.id} value={item.id}>{item.name} / 每箱 {item.quantity}</option>)}
                </select></label>
                <label>装箱数量<input min="0.0001" required step="0.0001" type="number" value={boxForm.quantity} onChange={(event) => setBoxForm({ ...boxForm, quantity: event.target.value })} /></label>
                <label className="desktop-field-wide">备注<input value={boxForm.remark} onChange={(event) => setBoxForm({ ...boxForm, remark: event.target.value })} /></label>
                <button className="desktop-primary-button" disabled={busy} type="submit">生成并打印箱码</button>
                <RecentRecords title="最近箱码" records={boxes.map((item) => `${item.boxCode} · ${item.batchNo} · ${item.quantity}`)} />
              </form>
            ) : null}
            {activeOperation === "documentPrint" ? (
              <div className="desktop-panel-stack">
                <label className="desktop-doc-select">单据类型<select value={selectedDocumentType} onChange={(event) => setSelectedDocumentType(event.target.value as "receipt" | "issue" | "check")}>
                  <option value="receipt">入库单</option>
                  <option value="issue">出库单</option>
                  <option value="check">盘点单</option>
                </select></label>
                <DocumentList type={selectedDocumentType} receipts={receipts} issues={issues} checks={checks} />
              </div>
            ) : null}
          </section>
        </div>
      </section>
    </main>
  );
}

function BatchPicker({ batches, selectedId, onSelect }: { batches: ProductionBatchRecord[]; selectedId: string; onSelect: (batch: ProductionBatchRecord) => void }) {
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

function SelectedBatchCard({ batch }: { batch: ProductionBatchRecord }) {
  return (
    <div className="desktop-selected-card">
      <strong>{batch.batchNo}</strong>
      <span>{batch.productName ?? batch.productCode ?? batch.productId}</span>
      <small>状态：{formatStatus(batch.status)}；完成：{batch.completedQuantity ?? 0}/{batch.plannedQuantity}{batch.unit ?? ""}</small>
    </div>
  );
}

function RecentRecords({ title, records }: { title: string; records: string[] }) {
  return (
    <div className="desktop-recent desktop-field-wide">
      <strong>{title}</strong>
      {records.length ? records.slice(0, 5).map((record) => <span key={record}>{record}</span>) : <span>暂无记录</span>}
    </div>
  );
}

function DocumentList({
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

function defaultBatchNo() {
  const now = new Date();
  return `WO-${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, "0")}${String(now.getDate()).padStart(2, "0")}-${String(now.getHours()).padStart(2, "0")}${String(now.getMinutes()).padStart(2, "0")}${String(now.getSeconds()).padStart(2, "0")}`;
}

function today() {
  return new Date().toISOString().slice(0, 10);
}

function toNumber(value: string) {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
}

function formatStatus(status: string) {
  const statusMap: Record<string, string> = {
    DRAFT: "草稿",
    RELEASED: "已下达",
    IN_PROGRESS: "生产中",
    COMPLETED: "已完工",
    CLOSED: "已关闭"
  };
  return statusMap[status] ?? status;
}

function documentNo(type: "receipt" | "issue" | "check", record: InventoryReceiptRecord | InventoryIssueRecord | InventoryCheckRecord) {
  if (type === "receipt") return (record as InventoryReceiptRecord).receiptNo;
  if (type === "issue") return (record as InventoryIssueRecord).issueNo;
  return (record as InventoryCheckRecord).checkNo;
}

function documentSummary(type: "receipt" | "issue" | "check", record: InventoryReceiptRecord | InventoryIssueRecord | InventoryCheckRecord) {
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

function printDocument(type: "receipt" | "issue" | "check", record: InventoryReceiptRecord | InventoryIssueRecord | InventoryCheckRecord) {
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

function openPrintWindow(title: string, html: string) {
  const popup = window.open("", "_blank", "width=640,height=720");
  if (!popup) return;
  popup.document.write(`<html><head><title>${title}</title></head><body>${html}<script>window.onload=()=>window.print()</script></body></html>`);
  popup.document.close();
}

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
