import React, { useEffect, useMemo, useState } from "react";
import { ActivityIndicator, Pressable, Text, TextInput, View } from "react-native";
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
} from "../../api/operations";
import type { UserInfo } from "../../types/auth";
import type {
  InventoryCheckRecord,
  InventoryIssueRecord,
  InventoryReceiptRecord,
  MaterialRecord,
  ProductPackageRecord,
  ProductRecord,
  ProductionBatchRecord,
  ProductionBoxRecord
} from "../../types/operations";
import { styles } from "../../styles";
import type { OperationCard } from "./operationConfig";
import { ChoiceList, defaultBatchNo, formatStatus, RecordPreview, toNumber } from "./OperationShared";

export function OperationDetail({ operation, user }: { operation: OperationCard; user: UserInfo }) {
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
