import React, { useEffect, useMemo, useState } from "react";
import {
  ActivityIndicator,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View
} from "react-native";
import { StatusBar } from "expo-status-bar";
import { mobileShellTitle } from "@erp/ui-mobile";
import { fetchUserInfo, login, logout } from "./src/api/auth";
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
} from "./src/api/operations";
import { saveAccessToken, saveUser } from "./src/store/auth";
import type { UserInfo } from "./src/types/auth";
import type {
  InventoryCheckRecord,
  InventoryIssueRecord,
  InventoryReceiptRecord,
  MaterialRecord,
  ProductPackageRecord,
  ProductRecord,
  ProductionBatchRecord,
  ProductionBoxRecord
} from "./src/types/operations";

type OperationKey = "batch" | "production" | "receipt" | "issue" | "check" | "boxPrint" | "documentPrint";

const operationCards: Array<{ key: OperationKey; title: string; description: string; tag: string }> = [
  { key: "batch", title: "创建工单", description: "按产品、数量和计划时间创建生产工单。", tag: "工单" },
  { key: "production", title: "生产执行", description: "扫描工单并提交生产报工。", tag: "车间" },
  { key: "receipt", title: "扫码入库", description: "扫描工单或箱码后确认入库。", tag: "入库" },
  { key: "issue", title: "扫码出库", description: "扫描物料并创建出库单。", tag: "出库" },
  { key: "check", title: "仓库盘点", description: "按物料扫码录入实盘数量。", tag: "盘点" },
  { key: "boxPrint", title: "箱码打印", description: "按工单生成箱码标签。", tag: "打印" },
  { key: "documentPrint", title: "出入库单据打印", description: "预览入库、出库、盘点单。", tag: "单据" }
];

export default function App() {
  const [username, setUsername] = useState("admin");
  const [password, setPassword] = useState("password");
  const [user, setUser] = useState<UserInfo | null>(null);
  const [activeOperation, setActiveOperation] = useState<OperationKey>("production");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleLogin() {
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

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar style="dark" />
      <KeyboardAvoidingView behavior={Platform.OS === "ios" ? "padding" : undefined} style={styles.keyboardView}>
        <ScrollView contentContainerStyle={styles.scrollContent} keyboardShouldPersistTaps="handled">
          {user ? (
            <View style={[styles.panel, styles.dashboardPanel]}>
              <Text style={styles.badge}>ERP Mobile</Text>
              <Text style={styles.title}>移动作业台</Text>
              <Text style={styles.subtitle}>欢迎回来，{user.realName || user.username}。移动端聚焦现场扫码、盘点、生产执行和打印预览。</Text>
              <View style={styles.operationGrid}>
                {operationCards.map((operation) => (
                  <Pressable
                    key={operation.key}
                    onPress={() => setActiveOperation(operation.key)}
                    style={[styles.operationCard, activeOperation === operation.key && styles.activeOperationCard]}
                  >
                    <Text style={[styles.operationTag, activeOperation === operation.key && styles.activeOperationText]}>{operation.tag}</Text>
                    <Text style={[styles.operationTitle, activeOperation === operation.key && styles.activeOperationText]}>{operation.title}</Text>
                    <Text style={[styles.operationDescription, activeOperation === operation.key && styles.activeOperationText]}>{operation.description}</Text>
                  </Pressable>
                ))}
              </View>
              <OperationDetail user={user} operation={operationCards.find((operation) => operation.key === activeOperation) ?? operationCards[0]} />
              <Pressable disabled={loading} onPress={() => void handleLogout()} style={[styles.secondaryButton, loading && styles.disabledButton]}><Text style={styles.secondaryButtonText}>{loading ? "退出中..." : "退出登录"}</Text></Pressable>
            </View>
          ) : (
            <View style={styles.panel}>
              <Text style={styles.badge}>{mobileShellTitle()}</Text>
              <Text style={styles.title}>全渠道 ERP 移动端登录</Text>
              <Text style={styles.subtitle}>连接后端服务，使用管理员账号进入移动作业台。</Text>
              {error ? <Text style={styles.error}>{error}</Text> : null}
              <View style={styles.form}>
                <View style={styles.field}><Text style={styles.label}>用户名</Text><TextInput autoCapitalize="none" autoCorrect={false} editable={!loading} onChangeText={setUsername} placeholder="请输入用户名" style={styles.input} value={username} /></View>
                <View style={styles.field}><Text style={styles.label}>密码</Text><TextInput editable={!loading} onChangeText={setPassword} placeholder="请输入密码" secureTextEntry style={styles.input} value={password} /></View>
                <Pressable disabled={loading} onPress={() => void handleLogin()} style={[styles.primaryButton, loading && styles.disabledButton]}>{loading ? <ActivityIndicator color="#ffffff" /> : <Text style={styles.primaryButtonText}>登录</Text>}</Pressable>
              </View>
            </View>
          )}
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

function OperationDetail({ operation, user }: { operation: (typeof operationCards)[number]; user: UserInfo }) {
  const [busy, setBusy] = useState(false);
  const [notice, setNotice] = useState<string | null>(null);
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
  const [selectedProductId, setSelectedProductId] = useState("");
  const [selectedBatchId, setSelectedBatchId] = useState("");
  const [selectedMaterialId, setSelectedMaterialId] = useState("");
  const [selectedPackageId, setSelectedPackageId] = useState("");
  const [batchNo, setBatchNo] = useState(defaultBatchNo());
  const [plannedQuantity, setPlannedQuantity] = useState("1");
  const [reportQuantity, setReportQuantity] = useState("1");
  const [goodQuantity, setGoodQuantity] = useState("1");
  const [defectQuantity, setDefectQuantity] = useState("0");
  const [quantity, setQuantity] = useState("1");
  const [sourceOrderNo, setSourceOrderNo] = useState("");
  const [remark, setRemark] = useState("");

  const selectedProduct = useMemo(() => products.find((item) => item.id === selectedProductId) ?? null, [products, selectedProductId]);
  const selectedBatch = useMemo(() => batches.find((item) => item.id === selectedBatchId) ?? null, [batches, selectedBatchId]);
  const selectedMaterial = useMemo(() => materials.find((item) => item.id === selectedMaterialId) ?? null, [materials, selectedMaterialId]);

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

  async function run(action: () => Promise<void>, successText: string) {
    setBusy(true);
    setNotice(null);
    try {
      await action();
      setNotice(successText);
    } catch (err) {
      setNotice(err instanceof Error ? err.message : "操作失败");
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
      setSelectedProductId(productPage.records[0]?.id ?? "");
      setSelectedMaterialId(materialPage.records[0]?.id ?? "");
      setSelectedBatchId(batchPage.records[0]?.id ?? "");
    } catch (err) {
      setNotice(err instanceof Error ? err.message : "加载作业数据失败");
    } finally {
      setBusy(false);
    }
  }

  async function refreshBatches(query = batchNoQuery) {
    const [batchPage, boxPage, receiptPage] = await Promise.all([fetchProductionBatches(query), fetchProductionBoxes(query), fetchInventoryReceipts()]);
    setBatches(batchPage.records);
    setBoxes(boxPage.records);
    setReceipts(receiptPage.records);
    setSelectedBatchId(batchPage.records[0]?.id ?? "");
  }

  async function refreshMaterials(query = materialQuery) {
    const materialPage = await fetchMaterials(query);
    setMaterials(materialPage.records);
    setSelectedMaterialId(materialPage.records[0]?.id ?? "");
  }

  return (
    <View style={styles.detailCard}>
      <Text style={styles.detailTag}>{operation.tag}</Text>
      <Text style={styles.sectionTitle}>{operation.title}</Text>
      <Text style={styles.detailDescription}>{operation.description}</Text>
      {notice ? <Text style={styles.notice}>{notice}</Text> : null}
      {operation.key === "batch" ? (
        <View style={styles.form}>
          <TextInput onChangeText={setBatchNo} placeholder="工单号" style={styles.input} value={batchNo} />
          <TextInput keyboardType="decimal-pad" onChangeText={setPlannedQuantity} placeholder="计划数量" style={styles.input} value={plannedQuantity} />
          <Text style={styles.label}>选择产品</Text>
          <ChoiceList
            items={products.map((item) => ({ id: item.id, title: `${item.code} / ${item.name}`, subtitle: item.unit }))}
            selectedId={selectedProductId}
            onSelect={setSelectedProductId}
          />
          <Pressable disabled={busy || !selectedProduct} onPress={() => void run(async () => {
            await createProductionBatch({
              batchNo,
              productId: selectedProductId,
              plannedQuantity: toNumber(plannedQuantity),
              unit: selectedProduct?.unit,
              status: "DRAFT",
              remark
            });
            setBatchNo(defaultBatchNo());
            await refreshBatches("");
          }, "生产工单已创建")} style={[styles.primaryButton, (busy || !selectedProduct) && styles.disabledButton]}>
            <Text style={styles.primaryButtonText}>创建工单</Text>
          </Pressable>
        </View>
      ) : null}
      {operation.key === "production" || operation.key === "receipt" ? (
        <View style={styles.form}>
          <View style={styles.inlineRow}>
            <TextInput onChangeText={setBatchNoQuery} placeholder="输入/扫描工单号" style={[styles.input, styles.inlineInput]} value={batchNoQuery} />
            <Pressable disabled={busy} onPress={() => void refreshBatches(batchNoQuery)} style={styles.smallButton}><Text style={styles.smallButtonText}>查询</Text></Pressable>
          </View>
          <ChoiceList
            items={batches.map((item) => ({ id: item.id, title: item.batchNo, subtitle: `${item.productName ?? item.productCode ?? item.productId} · ${formatStatus(item.status)} · ${item.completedQuantity ?? 0}/${item.plannedQuantity}` }))}
            selectedId={selectedBatchId}
            onSelect={setSelectedBatchId}
          />
          {selectedBatch ? <Text style={styles.summary}>当前工单：{selectedBatch.batchNo} / {formatStatus(selectedBatch.status)}</Text> : null}
          {operation.key === "production" ? (
            <>
              <View style={styles.inlineRow}>
                <Pressable disabled={busy || !selectedBatch} onPress={() => void run(async () => {
                  if (!selectedBatch) return;
                  await startProductionBatch(selectedBatch.id);
                  await refreshBatches();
                }, "已投产")} style={[styles.primaryButton, styles.flexButton]}><Text style={styles.primaryButtonText}>投产</Text></Pressable>
                <Pressable disabled={busy || !selectedBatch} onPress={() => void run(async () => {
                  if (!selectedBatch) return;
                  await receiveProductionBatch(selectedBatch.id);
                  await refreshBatches();
                }, "已完工入库")} style={[styles.secondaryButton, styles.flexButton]}><Text style={styles.secondaryButtonText}>入库</Text></Pressable>
              </View>
              <TextInput keyboardType="decimal-pad" onChangeText={setReportQuantity} placeholder="报工数量" style={styles.input} value={reportQuantity} />
              <TextInput keyboardType="decimal-pad" onChangeText={setGoodQuantity} placeholder="良品数量" style={styles.input} value={goodQuantity} />
              <TextInput keyboardType="decimal-pad" onChangeText={setDefectQuantity} placeholder="不良数量" style={styles.input} value={defectQuantity} />
              <Pressable disabled={busy || !selectedBatch} onPress={() => void run(async () => {
                if (!selectedBatch) return;
                await createProductionReport({
                  batchId: selectedBatch.id,
                  reportQuantity: toNumber(reportQuantity),
                  goodQuantity: toNumber(goodQuantity),
                  defectQuantity: toNumber(defectQuantity),
                  operatorName: user.realName || user.username,
                  remark
                });
                await refreshBatches();
              }, "生产报工已提交")} style={[styles.primaryButton, (busy || !selectedBatch) && styles.disabledButton]}><Text style={styles.primaryButtonText}>提交报工</Text></Pressable>
            </>
          ) : (
            <Pressable disabled={busy || !selectedBatch} onPress={() => void run(async () => {
              if (!selectedBatch) return;
              await receiveProductionBatch(selectedBatch.id);
              await refreshBatches();
            }, "扫码入库已确认")} style={[styles.primaryButton, (busy || !selectedBatch) && styles.disabledButton]}><Text style={styles.primaryButtonText}>确认扫码入库</Text></Pressable>
          )}
        </View>
      ) : null}
      {operation.key === "issue" || operation.key === "check" ? (
        <View style={styles.form}>
          <View style={styles.inlineRow}>
            <TextInput onChangeText={setMaterialQuery} placeholder="输入/扫描物料编码或名称" style={[styles.input, styles.inlineInput]} value={materialQuery} />
            <Pressable disabled={busy} onPress={() => void refreshMaterials(materialQuery)} style={styles.smallButton}><Text style={styles.smallButtonText}>查询</Text></Pressable>
          </View>
          <ChoiceList
            items={materials.map((item) => ({ id: item.id, title: `${item.code} / ${item.name}`, subtitle: `库存 ${item.currentStock ?? 0}${item.unit}` }))}
            selectedId={selectedMaterialId}
            onSelect={setSelectedMaterialId}
          />
          {selectedMaterial ? <Text style={styles.summary}>当前物料：{selectedMaterial.name}，库存 {selectedMaterial.currentStock ?? 0}{selectedMaterial.unit}</Text> : null}
          <TextInput keyboardType="decimal-pad" onChangeText={setQuantity} placeholder={operation.key === "issue" ? "出库数量" : "实盘数量"} style={styles.input} value={quantity} />
          {operation.key === "issue" ? <TextInput onChangeText={setSourceOrderNo} placeholder="来源单号，可选" style={styles.input} value={sourceOrderNo} /> : null}
          <TextInput onChangeText={setRemark} placeholder="备注" style={styles.input} value={remark} />
          <Pressable disabled={busy || !selectedMaterial} onPress={() => void run(async () => {
            if (operation.key === "issue") {
              await createInventoryIssue({ materialId: selectedMaterialId, quantity: toNumber(quantity), sourceOrderNo, remark });
              const issuePage = await fetchInventoryIssues();
              setIssues(issuePage.records);
            } else {
              await createInventoryCheck({ materialId: selectedMaterialId, quantity: toNumber(quantity), remark });
              const checkPage = await fetchInventoryChecks();
              setChecks(checkPage.records);
            }
            await refreshMaterials(materialQuery);
          }, operation.key === "issue" ? "扫码出库已完成" : "仓库盘点已提交")} style={[styles.primaryButton, (busy || !selectedMaterial) && styles.disabledButton]}>
            <Text style={styles.primaryButtonText}>{operation.key === "issue" ? "确认扫码出库" : "提交盘点"}</Text>
          </Pressable>
        </View>
      ) : null}
      {operation.key === "boxPrint" ? (
        <View style={styles.form}>
          <ChoiceList
            items={batches.map((item) => ({ id: item.id, title: item.batchNo, subtitle: item.productName ?? item.productCode ?? item.productId }))}
            selectedId={selectedBatchId}
            onSelect={setSelectedBatchId}
          />
          <ChoiceList
            items={packages.map((item) => ({ id: item.id ?? "", title: item.name, subtitle: `每箱 ${item.quantity}` }))}
            selectedId={selectedPackageId}
            onSelect={setSelectedPackageId}
          />
          <TextInput keyboardType="decimal-pad" onChangeText={setQuantity} placeholder="装箱数量" style={styles.input} value={quantity} />
          <Pressable disabled={busy || !selectedBatch || !selectedPackageId} onPress={() => void run(async () => {
            if (!selectedBatch) return;
            await createProductionBox({ batchId: selectedBatch.id, packageId: selectedPackageId, quantity: toNumber(quantity), remark });
            const boxPage = await fetchProductionBoxes(selectedBatch.batchNo);
            setBoxes(boxPage.records);
          }, "箱码已生成，可交给桌面端/云打印")} style={[styles.primaryButton, (busy || !selectedBatch || !selectedPackageId) && styles.disabledButton]}>
            <Text style={styles.primaryButtonText}>生成箱码</Text>
          </Pressable>
          <RecordPreview title="最近箱码" records={boxes.map((item) => `${item.boxCode} · ${item.quantity}`)} />
        </View>
      ) : null}
      {operation.key === "documentPrint" ? (
        <View style={styles.form}>
          <RecordPreview title="入库单" records={receipts.map((item) => `${item.receiptNo} · ${item.sourceOrderNo ?? "-"} · ${item.status}`)} />
          <RecordPreview title="出库单" records={issues.map((item) => `${item.issueNo} · ${item.totalQuantity} · ${item.status}`)} />
          <RecordPreview title="盘点单" records={checks.map((item) => `${item.checkNo} · 差异 ${item.totalDifference} · ${item.status}`)} />
          <Text style={styles.summary}>移动端当前提供单据预览，正式打印交给桌面端或后续云打印服务。</Text>
        </View>
      ) : null}
      {busy ? <ActivityIndicator color="#0f766e" /> : null}
    </View>
  );
}

function ChoiceList({ items, selectedId, onSelect }: { items: Array<{ id: string; title: string; subtitle?: string | null }>; selectedId: string; onSelect: (id: string) => void }) {
  return (
    <View style={styles.choiceList}>
      {items.length ? items.slice(0, 6).map((item) => (
        <Pressable key={item.id} onPress={() => onSelect(item.id)} style={[styles.choiceItem, selectedId === item.id && styles.activeChoiceItem]}>
          <Text style={[styles.choiceTitle, selectedId === item.id && styles.activeOperationText]}>{item.title}</Text>
          {item.subtitle ? <Text style={[styles.choiceSubtitle, selectedId === item.id && styles.activeOperationText]}>{item.subtitle}</Text> : null}
        </Pressable>
      )) : <Text style={styles.summary}>暂无可选数据</Text>}
    </View>
  );
}

function RecordPreview({ title, records }: { title: string; records: string[] }) {
  return (
    <View style={styles.previewCard}>
      <Text style={styles.label}>{title}</Text>
      {records.length ? records.slice(0, 4).map((record) => <Text key={record} style={styles.previewText}>{record}</Text>) : <Text style={styles.previewText}>暂无记录</Text>}
    </View>
  );
}

function defaultBatchNo() {
  const now = new Date();
  return `WO-${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, "0")}${String(now.getDate()).padStart(2, "0")}-${String(now.getHours()).padStart(2, "0")}${String(now.getMinutes()).padStart(2, "0")}`;
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

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: "#eef6fb" },
  keyboardView: { flex: 1 },
  scrollContent: { flexGrow: 1, justifyContent: "center", padding: 20 },
  panel: { padding: 24, borderRadius: 28, backgroundColor: "#ffffff", shadowColor: "#0f172a", shadowOffset: { width: 0, height: 18 }, shadowOpacity: 0.12, shadowRadius: 32, elevation: 8 },
  dashboardPanel: { gap: 18 },
  badge: { alignSelf: "flex-start", paddingHorizontal: 12, paddingVertical: 6, borderRadius: 999, backgroundColor: "#0f766e", color: "#ffffff", overflow: "hidden", fontSize: 12, fontWeight: "700", letterSpacing: 1 },
  title: { marginTop: 18, fontSize: 28, fontWeight: "700", color: "#10263d" },
  subtitle: { marginTop: 8, fontSize: 16, lineHeight: 24, color: "#5b7089" },
  form: { marginTop: 12, gap: 12 },
  field: { gap: 8 },
  label: { fontSize: 14, fontWeight: "700", color: "#334155" },
  input: { height: 48, borderWidth: 1, borderColor: "#cbd5e1", borderRadius: 14, paddingHorizontal: 14, backgroundColor: "#ffffff", color: "#0f172a" },
  primaryButton: { height: 48, alignItems: "center", justifyContent: "center", borderRadius: 14, backgroundColor: "#0f766e" },
  primaryButtonText: { color: "#ffffff", fontSize: 16, fontWeight: "700" },
  secondaryButton: { height: 46, alignItems: "center", justifyContent: "center", borderRadius: 14, backgroundColor: "#e2e8f0" },
  secondaryButtonText: { color: "#0f172a", fontSize: 15, fontWeight: "700" },
  smallButton: { width: 72, height: 48, alignItems: "center", justifyContent: "center", borderRadius: 14, backgroundColor: "#e2e8f0" },
  smallButtonText: { color: "#0f172a", fontWeight: "800" },
  disabledButton: { opacity: 0.7 },
  error: { marginTop: 10, padding: 12, borderRadius: 14, overflow: "hidden", backgroundColor: "#fef2f2", color: "#b91c1c" },
  notice: { padding: 12, borderRadius: 14, overflow: "hidden", backgroundColor: "#ecfdf5", color: "#047857", fontWeight: "700" },
  operationGrid: { flexDirection: "row", flexWrap: "wrap", gap: 10 },
  operationCard: { width: "48%", minHeight: 136, gap: 8, padding: 14, borderRadius: 18, backgroundColor: "#f8fafc", borderWidth: 1, borderColor: "#e2e8f0" },
  activeOperationCard: { backgroundColor: "#0f766e", borderColor: "#0f766e" },
  operationTag: { color: "#0f766e", fontSize: 12, fontWeight: "800" },
  operationTitle: { color: "#10263d", fontSize: 17, fontWeight: "800" },
  operationDescription: { color: "#64748b", fontSize: 13, lineHeight: 19 },
  activeOperationText: { color: "#ffffff" },
  detailCard: { gap: 12, padding: 18, borderRadius: 20, backgroundColor: "#ecfeff" },
  detailTag: { color: "#0f766e", fontSize: 12, fontWeight: "800" },
  sectionTitle: { fontSize: 20, fontWeight: "800", color: "#10263d" },
  detailDescription: { color: "#475569", fontSize: 15, lineHeight: 22 },
  inlineRow: { flexDirection: "row", gap: 10, alignItems: "center" },
  inlineInput: { flex: 1 },
  flexButton: { flex: 1 },
  summary: { color: "#475569", fontSize: 14, lineHeight: 20 },
  choiceList: { gap: 8 },
  choiceItem: { padding: 12, borderRadius: 14, backgroundColor: "#ffffff", borderWidth: 1, borderColor: "#dbe7ee" },
  activeChoiceItem: { backgroundColor: "#0f766e", borderColor: "#0f766e" },
  choiceTitle: { color: "#10263d", fontSize: 15, fontWeight: "800" },
  choiceSubtitle: { marginTop: 4, color: "#64748b", fontSize: 12, lineHeight: 18 },
  previewCard: { gap: 8, padding: 12, borderRadius: 14, backgroundColor: "#ffffff" },
  previewText: { color: "#475569", lineHeight: 20 }
});
