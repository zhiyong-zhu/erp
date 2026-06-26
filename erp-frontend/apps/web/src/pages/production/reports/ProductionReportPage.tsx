import { PlayCircleOutlined, ToolOutlined } from "@ant-design/icons";
import { App, Button, Descriptions, Divider, Drawer, Input, Modal, Space, Table, Tag, Typography } from "antd";
import { PRODUCTION_PERMISSIONS } from "@erp/shared";
import { CreateForm } from "../../../components/CreateForm";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import {
  createProductionBox,
  exportProductionReports,
  fetchProductionBoms,
  createProductionReport,
  fetchProductionMaterialMovements,
  fetchProductionBatches,
  fetchProductionBoxes,
  fetchProductionProductStock,
  fetchProductionReports,
  generateBatchSerialNumbers,
  pickProductionMaterials,
  receiveProductionBatch,
  returnProductionMaterials,
  startProductionBatch
} from "../../../api/production";
import { hasPermission } from "../../../store/auth";
import { downloadBlob } from "../../../utils/export";
import type {
  ProductionBatchRecord,
  ProductionBoxPayload,
  ProductionBoxRecord,
  ProductionMaterialMovementPayload,
  ProductionMaterialMovementRecord,
  ProductionProductStockRecord,
  ProductionReportPayload,
  ProductionReportRecord,
  SerialNumberGeneratePayload
} from "../../../types/production";

const { Title, Text } = Typography;

const batchStatusMap: Record<string, { text: string; color?: string }> = {
  DRAFT: { text: "草稿", color: "default" },
  RELEASED: { text: "已下达", color: "processing" },
  IN_PROGRESS: { text: "生产中", color: "blue" },
  COMPLETED: { text: "已完工", color: "success" },
  CLOSED: { text: "已关闭", color: "default" }
};

const reportStatusMap: Record<string, { text: string; color?: string }> = {
  SUBMITTED: { text: "已提交", color: "success" }
};

const movementTypeMap: Record<string, { text: string; color?: string }> = {
  PICK: { text: "生产领料", color: "blue" },
  RETURN: { text: "生产退料", color: "green" }
};

function canGenerateSerials(status: string) {
  return status !== "CLOSED";
}

function canStartBatch(status: string) {
  return status === "DRAFT" || status === "RELEASED";
}

function canReportBatch(status: string) {
  return status === "IN_PROGRESS";
}

function canPackBatch(status: string) {
  return status === "IN_PROGRESS" || status === "COMPLETED";
}

function canReceiveBatch(status: string) {
  return status === "COMPLETED";
}

function canMoveMaterials(status: string) {
  return status === "IN_PROGRESS" || status === "COMPLETED";
}

interface MaterialMovementItem {
  materialId: string;
  materialCode?: string | null;
  materialName?: string | null;
  quantity: number;
  remark?: string;
}

interface MaterialMovementValues {
  warehouseCode?: string;
  warehouseName?: string;
  locationCode?: string;
  locationName?: string;
  batchNo?: string;
  remark?: string;
  items: MaterialMovementItem[];
}

export function ProductionReportPage() {
  const [batchLoading, setBatchLoading] = useState(false);
  const [reportLoading, setReportLoading] = useState(false);
  const [batches, setBatches] = useState<ProductionBatchRecord[]>([]);
  const [reports, setReports] = useState<ProductionReportRecord[]>([]);
  const [boxes, setBoxes] = useState<ProductionBoxRecord[]>([]);
  const [movements, setMovements] = useState<ProductionMaterialMovementRecord[]>([]);
  const [stocks, setStocks] = useState<ProductionProductStockRecord[]>([]);
  const [batchPageNum, setBatchPageNum] = useState(1);
  const [batchPageSize, setBatchPageSize] = useState(10);
  const [batchTotal, setBatchTotal] = useState(0);
  const [reportPageNum, setReportPageNum] = useState(1);
  const [reportPageSize, setReportPageSize] = useState(10);
  const [reportTotal, setReportTotal] = useState(0);
  const [movementLoading, setMovementLoading] = useState(false);
  const [movementPageNum, setMovementPageNum] = useState(1);
  const [movementPageSize, setMovementPageSize] = useState(10);
  const [movementTotal, setMovementTotal] = useState(0);
  const [keyword, setKeyword] = useState("");
  const [viewingMovement, setViewingMovement] = useState<ProductionMaterialMovementRecord | null>(null);
  const [reportingBatch, setReportingBatch] = useState<ProductionBatchRecord | null>(null);
  const [serialBatch, setSerialBatch] = useState<ProductionBatchRecord | null>(null);
  const [packingBatch, setPackingBatch] = useState<ProductionBatchRecord | null>(null);
  const [pickingBatch, setPickingBatch] = useState<ProductionBatchRecord | null>(null);
  const [returningBatch, setReturningBatch] = useState<ProductionBatchRecord | null>(null);
  const [movementItems, setMovementItems] = useState<MaterialMovementItem[]>([]);
  const { message } = App.useApp();
  const canCreateReport = hasPermission(PRODUCTION_PERMISSIONS.REPORT_CREATE);

  useEffect(() => {
    void loadBatches();
    void loadReports();
    void loadBoxes();
    void loadMovements();
    void loadStocks();
  }, []);

  async function loadBatches(nextPageNum = batchPageNum, nextPageSize = batchPageSize, batchNo = keyword) {
    setBatchLoading(true);
    try {
      const data = await fetchProductionBatches({ pageNum: nextPageNum, pageSize: nextPageSize, batchNo });
      setBatches(data.records);
      setBatchPageNum(data.pageNum);
      setBatchPageSize(data.pageSize);
      setBatchTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载生产批次失败");
    } finally {
      setBatchLoading(false);
    }
  }

  async function loadReports(nextPageNum = reportPageNum, nextPageSize = reportPageSize, batchNo = keyword) {
    setReportLoading(true);
    try {
      const data = await fetchProductionReports({ pageNum: nextPageNum, pageSize: nextPageSize, batchNo });
      setReports(data.records);
      setReportPageNum(data.pageNum);
      setReportPageSize(data.pageSize);
      setReportTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载报工记录失败");
    } finally {
      setReportLoading(false);
    }
  }

  async function handleExportReports() {
    try {
      const blob = await exportProductionReports({ batchNo: keyword || undefined });
      downloadBlob("production-reports.xlsx", blob);
      message.success("报工记录导出成功");
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "报工记录导出失败");
    }
  }

  async function loadBoxes(batchNo = keyword) {
    const data = await fetchProductionBoxes({ pageNum: 1, pageSize: 10, batchNo });
    setBoxes(data.records);
  }

  async function loadMovements(nextPageNum = movementPageNum, nextPageSize = movementPageSize) {
    setMovementLoading(true);
    try {
      const data = await fetchProductionMaterialMovements({ pageNum: nextPageNum, pageSize: nextPageSize });
      setMovements(data.records);
      setMovementPageNum(data.pageNum);
      setMovementPageSize(data.pageSize);
      setMovementTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载领退料单失败");
    } finally {
      setMovementLoading(false);
    }
  }

  async function loadStocks(productName = "") {
    const data = await fetchProductionProductStock({ pageNum: 1, pageSize: 10, productName });
    setStocks(data.records);
  }

  async function handleSearch(value: string) {
    setKeyword(value);
    await Promise.all([loadBatches(1, batchPageSize, value), loadReports(1, reportPageSize, value), loadBoxes(value), loadMovements(1, movementPageSize)]);
  }

  async function handleStart(batch: ProductionBatchRecord) {
    try {
      await startProductionBatch(batch.id);
      message.success("已投产");
      await loadBatches();
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "投产失败");
    }
  }

  async function openMaterialMovement(batch: ProductionBatchRecord, direction: "pick" | "return") {
    setMovementItems(await buildBomMovementItems(batch));
    if (direction === "pick") {
      setPickingBatch(batch);
    } else {
      setReturningBatch(batch);
    }
  }

  async function buildBomMovementItems(batch: ProductionBatchRecord): Promise<MaterialMovementItem[]> {
    try {
      const data = await fetchProductionBoms({ pageNum: 1, pageSize: 50, productId: batch.productId, status: 1 });
      const bom = data.records.find((item) => item.id === batch.bomId) ?? data.records[0];
      if (!bom?.items?.length) {
        return [{ materialId: "", quantity: 1 }];
      }
      return bom.items.map((item) => ({
        materialId: item.materialId,
        materialCode: item.materialCode,
        materialName: item.materialName,
        quantity: calculateRequiredQuantity(batch.plannedQuantity, item.quantity, item.lossRate),
        remark: `BOM ${bom.code}`
      }));
    } catch {
      return [{ materialId: "", quantity: 1 }];
    }
  }

  function confirmStart(batch: ProductionBatchRecord) {
    Modal.confirm({
      title: "确认投产？",
      content: `批次号：${batch.batchNo}`,
      okText: "确认",
      cancelText: "取消",
      onOk: () => handleStart(batch)
    });
  }

  async function handleGenerateSerials(values: SerialNumberGeneratePayload) {
    if (!serialBatch) return false;
    try {
      const created = await generateBatchSerialNumbers(serialBatch.id, values);
      message.success(`已生成 ${created.length} 个序列号`);
      setSerialBatch(null);
      await loadBatches();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "生成序列号失败");
      return false;
    }
  }

  async function handlePack(values: ProductionBoxPayload) {
    if (!packingBatch) return false;
    if ((values.quantity ?? 0) > (packingBatch.completedQuantity ?? 0)) {
      message.error("装箱数量不能超过当前已完成良品数量");
      return false;
    }
    try {
      await createProductionBox({ ...values, batchId: packingBatch.id });
      message.success("装箱完成，箱码已生成");
      setPackingBatch(null);
      await loadBoxes();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "装箱失败");
      return false;
    }
  }

  async function handleReceipt(batch: ProductionBatchRecord) {
    try {
      await receiveProductionBatch(batch.id);
      message.success("生产入库完成，成品库存已更新");
      await Promise.all([loadBatches(), loadStocks()]);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "生产入库失败");
    }
  }

  function confirmReceipt(batch: ProductionBatchRecord) {
    Modal.confirm({
      title: "确认完工入库？",
      content: `批次 ${batch.batchNo} 将以完成数 ${batch.completedQuantity ?? 0} 写入成品库存和库存流水。`,
      okText: "确认入库",
      cancelText: "取消",
      onOk: () => handleReceipt(batch)
    });
  }

  async function handleReport(values: ProductionReportPayload) {
    if (!reportingBatch) {
      return false;
    }
    const reportQuantity = values.reportQuantity ?? 0;
    const goodQuantity = values.goodQuantity ?? 0;
    const defectQuantity = values.defectQuantity ?? 0;
    if (Math.abs(goodQuantity + defectQuantity - reportQuantity) > 0.0001) {
      message.error("良品数量 + 不良数量必须等于报工数量");
      return false;
    }
    if ((reportingBatch.completedQuantity ?? 0) + goodQuantity > reportingBatch.plannedQuantity) {
      message.error("累计良品数量不能超过计划数量");
      return false;
    }
    try {
      await createProductionReport({ ...values, batchId: reportingBatch.id });
      message.success("报工已提交");
      setReportingBatch(null);
      await Promise.all([loadBatches(), loadReports(1, reportPageSize), loadStocks()]);
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "报工失败");
      return false;
    }
  }

  async function handlePickMaterials(values: MaterialMovementValues) {
    if (!pickingBatch) return false;
    const payload: ProductionMaterialMovementPayload = {
      batchId: pickingBatch.id,
      idempotencyKey: `production-pick-${pickingBatch.id}-${Date.now()}`,
      warehouseCode: values.warehouseCode,
      warehouseName: values.warehouseName,
      locationCode: values.locationCode,
      locationName: values.locationName,
      batchNo: values.batchNo,
      remark: values.remark,
      items: values.items.map((item) => ({
        materialId: item.materialId,
        quantity: item.quantity,
        warehouseCode: values.warehouseCode,
        warehouseName: values.warehouseName,
        locationCode: values.locationCode,
        locationName: values.locationName,
        batchNo: values.batchNo,
        remark: item.remark
      }))
    };
    try {
      await pickProductionMaterials(payload);
      message.success("生产领料完成，原料库存已扣减");
      setPickingBatch(null);
      await loadMovements(1, movementPageSize);
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "生产领料失败");
      return false;
    }
  }

  async function handleReturnMaterials(values: MaterialMovementValues) {
    if (!returningBatch) return false;
    const payload: ProductionMaterialMovementPayload = {
      batchId: returningBatch.id,
      idempotencyKey: `production-return-${returningBatch.id}-${Date.now()}`,
      warehouseCode: values.warehouseCode,
      warehouseName: values.warehouseName,
      locationCode: values.locationCode,
      locationName: values.locationName,
      batchNo: values.batchNo,
      remark: values.remark,
      items: values.items.map((item) => ({
        materialId: item.materialId,
        quantity: item.quantity,
        warehouseCode: values.warehouseCode,
        warehouseName: values.warehouseName,
        locationCode: values.locationCode,
        locationName: values.locationName,
        batchNo: values.batchNo,
        remark: item.remark
      }))
    };
    try {
      await returnProductionMaterials(payload);
      message.success("生产退料完成，原料库存已回补");
      setReturningBatch(null);
      await loadMovements(1, movementPageSize);
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "生产退料失败");
      return false;
    }
  }

  const batchColumns: ColumnsType<ProductionBatchRecord> = [
    { title: "批次号", dataIndex: "batchNo", key: "batchNo", width: 160 },
    { title: "产品", dataIndex: "productName", key: "productName", render: (value, record) => value ?? record.productCode ?? record.productId },
    { title: "计划数", dataIndex: "plannedQuantity", key: "plannedQuantity", width: 100 },
    { title: "完成数", dataIndex: "completedQuantity", key: "completedQuantity", width: 100, render: (value) => value ?? 0 },
    { title: "单位", dataIndex: "unit", key: "unit", width: 80 },
    { title: "工艺", dataIndex: "processName", key: "processName", width: 150, render: (value, record) => value ?? record.processId ?? "-" },
    { title: "BOM", dataIndex: "bomCode", key: "bomCode", width: 120, render: (value, record) => value ?? record.bomId ?? "-" },
    { title: "状态", dataIndex: "status", key: "status", width: 110, render: (value: string) => renderBatchStatus(value) },
    {
      title: "流程操作",
      key: "actions",
      width: 480,
      render: (_, record) => {
        return (
          <Space>
            <Button size="small" disabled={!canCreateReport || !canGenerateSerials(record.status)} onClick={() => setSerialBatch(record)}>
              生成码
            </Button>
            <Button size="small" icon={<PlayCircleOutlined />} disabled={!canCreateReport || !canStartBatch(record.status)} onClick={() => confirmStart(record)}>
              投产
            </Button>
            <Button size="small" type="primary" icon={<ToolOutlined />} disabled={!canCreateReport || !canReportBatch(record.status)} onClick={() => setReportingBatch(record)}>
              报工
            </Button>
            <Button size="small" disabled={!canCreateReport || !canMoveMaterials(record.status)} onClick={() => void openMaterialMovement(record, "pick")}>
              领料
            </Button>
            <Button size="small" disabled={!canCreateReport || !canMoveMaterials(record.status)} onClick={() => void openMaterialMovement(record, "return")}>
              退料
            </Button>
            <Button size="small" disabled={!canCreateReport || !canPackBatch(record.status)} onClick={() => setPackingBatch(record)}>
              装箱
            </Button>
            <Button size="small" disabled={!canCreateReport || !canReceiveBatch(record.status)} onClick={() => confirmReceipt(record)}>
              入库
            </Button>
          </Space>
        );
      }
    }
  ];

  const reportColumns: ColumnsType<ProductionReportRecord> = [
    { title: "报工单号", dataIndex: "reportNo", key: "reportNo", width: 160 },
    { title: "批次号", dataIndex: "batchNo", key: "batchNo", width: 150 },
    { title: "产品", dataIndex: "productName", key: "productName", render: (value, record) => value ?? record.productCode ?? record.productId },
    { title: "报工数", dataIndex: "reportQuantity", key: "reportQuantity", width: 100 },
    { title: "良品数", dataIndex: "goodQuantity", key: "goodQuantity", width: 100 },
    { title: "不良数", dataIndex: "defectQuantity", key: "defectQuantity", width: 100 },
    { title: "报工时间", dataIndex: "reportAt", key: "reportAt", width: 180, render: (value) => formatDateTime(value) },
    { title: "操作人", dataIndex: "operatorName", key: "operatorName", width: 120 },
    { title: "状态", dataIndex: "status", key: "status", width: 100, render: (value: string) => renderReportStatus(value) },
    { title: "备注", dataIndex: "remark", key: "remark" }
  ];

  const movementColumns: ColumnsType<ProductionMaterialMovementRecord> = [
    { title: "单据号", dataIndex: "movementNo", key: "movementNo", width: 180 },
    { title: "类型", dataIndex: "movementType", key: "movementType", width: 110, render: (value: string) => renderMovementType(value) },
    { title: "批次号", dataIndex: "batchNo", key: "batchNo", width: 150 },
    { title: "总数量", dataIndex: "totalQuantity", key: "totalQuantity", width: 100 },
    { title: "仓库", dataIndex: "warehouseName", key: "warehouseName", width: 120, render: (value, record) => value ?? record.warehouseCode ?? "-" },
    { title: "库位", dataIndex: "locationName", key: "locationName", width: 120, render: (value, record) => value ?? record.locationCode ?? "-" },
    { title: "库存单据", dataIndex: "inventoryDocumentNo", key: "inventoryDocumentNo", width: 160, render: (value, record) => value ?? record.inventoryDocumentId ?? "-" },
    { title: "创建时间", dataIndex: "createdAt", key: "createdAt", width: 180, render: (value) => formatDateTime(value) },
    { title: "备注", dataIndex: "remark", key: "remark", render: (value) => value || "-" },
    {
      title: "操作",
      key: "actions",
      width: 130,
      render: (_, record) => (
        <Space>
          <Button type="link" onClick={() => setViewingMovement(record)}>明细</Button>
          <Button type="link" onClick={() => printMaterialMovement(record)}>打印</Button>
        </Space>
      )
    }
  ];

  const boxColumns: ColumnsType<ProductionBoxRecord> = [
    { title: "箱码", dataIndex: "boxCode", key: "boxCode", width: 180 },
    { title: "批次号", dataIndex: "batchNo", key: "batchNo", width: 150 },
    { title: "产品", dataIndex: "productName", key: "productName", render: (value, record) => value ?? record.productCode ?? record.productId },
    { title: "包装规格", dataIndex: "packageName", key: "packageName", width: 140 },
    { title: "数量", dataIndex: "quantity", key: "quantity", width: 100 },
    { title: "状态", dataIndex: "status", key: "status", width: 100, render: () => <Tag color="success">已装箱</Tag> },
    { title: "标签预览", dataIndex: "labelHtml", key: "labelHtml", width: 110, render: (value) => <Button size="small" disabled={!value} onClick={() => previewLabel(value)}>预览</Button> }
  ];

  const stockColumns: ColumnsType<ProductionProductStockRecord> = [
    { title: "产品编码", dataIndex: "productCode", key: "productCode", width: 140 },
    { title: "产品名称", dataIndex: "productName", key: "productName" },
    { title: "成品库存", dataIndex: "currentStock", key: "currentStock", width: 120 }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>生产管理 / 生产执行</Title>
          <Text type="secondary">从生产工单到批次/序列号、投产报工、装箱打码、完工入库的完整执行链路。</Text>
        </div>
        <Input.Search
          allowClear
          placeholder="搜索批次号"
          value={keyword}
          onChange={(event) => setKeyword(event.target.value)}
          onSearch={(value) => void handleSearch(value)}
          style={{ width: 260 }}
        />
      </div>

      <Table
        rowKey="id"
        columns={batchColumns}
        dataSource={batches}
        loading={batchLoading}
        scroll={{ x: 1360 }}
        pagination={{
          current: batchPageNum,
          pageSize: batchPageSize,
          total: batchTotal,
          showSizeChanger: true,
          showTotal: (count) => `共 ${count} 条`,
          onChange: (nextPageNum, nextPageSize) => void loadBatches(nextPageNum, nextPageSize)
        }}
      />

      <Divider orientation="left">箱码与装箱</Divider>

      <Table rowKey="id" columns={boxColumns} dataSource={boxes} pagination={false} scroll={{ x: 900 }} />

      <Divider orientation="left">领退料单据</Divider>

      <Table
        rowKey="id"
        columns={movementColumns}
        dataSource={movements}
        loading={movementLoading}
        scroll={{ x: 1250 }}
        pagination={{
          current: movementPageNum,
          pageSize: movementPageSize,
          total: movementTotal,
          showSizeChanger: true,
          showTotal: (count) => `共 ${count} 条`,
          onChange: (nextPageNum, nextPageSize) => void loadMovements(nextPageNum, nextPageSize)
        }}
      />

      <Divider orientation="left">报工记录</Divider>
      <div style={{ display: "flex", justifyContent: "flex-end", marginBottom: 8 }}>
        <Button onClick={() => void handleExportReports()}>导出报工Excel</Button>
      </div>

      <Table
        rowKey="id"
        columns={reportColumns}
        dataSource={reports}
        loading={reportLoading}
        scroll={{ x: 1200 }}
        pagination={{
          current: reportPageNum,
          pageSize: reportPageSize,
          total: reportTotal,
          showSizeChanger: true,
          showTotal: (count) => `共 ${count} 条`,
          onChange: (nextPageNum, nextPageSize) => void loadReports(nextPageNum, nextPageSize)
        }}
      />

      <Divider orientation="left">成品库存</Divider>

      <Table rowKey="id" columns={stockColumns} dataSource={stocks} pagination={false} />

      <ReportForm batch={reportingBatch} onCancel={() => setReportingBatch(null)} onFinish={handleReport} />
      <MaterialMovementForm title="生产领料" batch={pickingBatch} items={movementItems} onCancel={() => setPickingBatch(null)} onFinish={handlePickMaterials} />
      <MaterialMovementForm title="生产退料" batch={returningBatch} items={movementItems} onCancel={() => setReturningBatch(null)} onFinish={handleReturnMaterials} />
      <SerialGenerateForm batch={serialBatch} onCancel={() => setSerialBatch(null)} onFinish={handleGenerateSerials} />
      <PackForm batch={packingBatch} onCancel={() => setPackingBatch(null)} onFinish={handlePack} />
      <MaterialMovementDrawer movement={viewingMovement} onClose={() => setViewingMovement(null)} onPrint={printMaterialMovement} />
    </section>
  );
}

function MaterialMovementDrawer({
  movement,
  onClose,
  onPrint
}: {
  movement: ProductionMaterialMovementRecord | null;
  onClose: () => void;
  onPrint: (record: ProductionMaterialMovementRecord) => void;
}) {
  const itemColumns: ColumnsType<NonNullable<ProductionMaterialMovementRecord["items"]>[number]> = [
    { title: "物料编码", dataIndex: "materialCode", key: "materialCode", width: 140, render: (value) => value || "-" },
    { title: "物料名称", dataIndex: "materialName", key: "materialName", width: 180, render: (value) => value || "-" },
    { title: "数量", dataIndex: "quantity", key: "quantity", width: 100 },
    { title: "仓库", dataIndex: "warehouseName", key: "warehouseName", width: 120, render: (value, record) => value ?? record.warehouseCode ?? "-" },
    { title: "库位", dataIndex: "locationName", key: "locationName", width: 120, render: (value, record) => value ?? record.locationCode ?? "-" },
    { title: "批次", dataIndex: "batchNo", key: "batchNo", width: 120, render: (value) => value || "-" },
    { title: "备注", dataIndex: "remark", key: "remark", render: (value) => value || "-" }
  ];

  return (
    <Drawer
      title={movement ? `${movement.movementNo} 明细` : "领退料单明细"}
      width={900}
      open={!!movement}
      onClose={onClose}
      destroyOnClose
      extra={movement ? <Button type="primary" onClick={() => onPrint(movement)}>打印</Button> : null}
    >
      <Descriptions bordered size="small" column={2} style={{ marginBottom: 16 }}>
        <Descriptions.Item label="单据号">{movement?.movementNo ?? "-"}</Descriptions.Item>
        <Descriptions.Item label="类型">{movement ? renderMovementType(movement.movementType) : "-"}</Descriptions.Item>
        <Descriptions.Item label="生产批次">{movement?.batchNo ?? "-"}</Descriptions.Item>
        <Descriptions.Item label="状态">{movement?.status ?? "-"}</Descriptions.Item>
        <Descriptions.Item label="库存单据">{movement?.inventoryDocumentNo ?? movement?.inventoryDocumentId ?? "-"}</Descriptions.Item>
        <Descriptions.Item label="总数量">{movement?.totalQuantity ?? "-"}</Descriptions.Item>
        <Descriptions.Item label="仓库">{movement?.warehouseName ?? movement?.warehouseCode ?? "-"}</Descriptions.Item>
        <Descriptions.Item label="库位">{movement?.locationName ?? movement?.locationCode ?? "-"}</Descriptions.Item>
        <Descriptions.Item label="库存批次">{movement?.batchNoInventory ?? "-"}</Descriptions.Item>
        <Descriptions.Item label="创建时间">{formatDateTime(movement?.createdAt)}</Descriptions.Item>
        <Descriptions.Item label="备注" span={2}>{movement?.remark || "-"}</Descriptions.Item>
      </Descriptions>
      <Table
        rowKey="id"
        columns={itemColumns}
        dataSource={movement?.items ?? []}
        pagination={false}
        scroll={{ x: 900 }}
      />
    </Drawer>
  );
}

function MaterialMovementForm({
  title,
  batch,
  items,
  onCancel,
  onFinish
}: {
  title: string;
  batch: ProductionBatchRecord | null;
  items: MaterialMovementItem[];
  onCancel: () => void;
  onFinish: (values: MaterialMovementValues) => Promise<boolean>;
}) {
  return (
    <ModalForm<MaterialMovementValues>
      title={batch ? `${batch.batchNo} ${title}` : title}
      open={!!batch}
      width={860}
      grid
      rowProps={{ gutter: 16 }}
      initialValues={{
        warehouseCode: "MAIN",
        warehouseName: "主仓",
        locationCode: "DEFAULT",
        locationName: "默认库位",
        batchNo: batch?.batchNo ?? "DEFAULT",
        items
      }}
      modalProps={{ destroyOnClose: true, onCancel }}
      onFinish={onFinish}
    >
      <ProFormText name="warehouseCode" label="仓库编码" rules={[{ required: true }]} colProps={{ xs: 24, md: 8 }} />
      <ProFormText name="warehouseName" label="仓库名称" colProps={{ xs: 24, md: 8 }} />
      <ProFormText name="locationCode" label="库位编码" rules={[{ required: true }]} colProps={{ xs: 24, md: 8 }} />
      <ProFormText name="locationName" label="库位名称" colProps={{ xs: 24, md: 8 }} />
      <ProFormText name="batchNo" label="批次" rules={[{ required: true }]} colProps={{ xs: 24, md: 8 }} />
      <ProFormTextArea name="remark" label="备注" colProps={{ span: 24 }} />
      <ProFormList name="items" label="物料明细" creatorButtonProps={{ creatorButtonText: "添加物料" }}>
        <ProFormText name="materialId" label="物料ID" rules={[{ required: true }]} />
        <ProFormText name="materialCode" label="物料编码" />
        <ProFormText name="materialName" label="物料名称" />
        <ProFormDigit name="quantity" label="数量" min={0.0001} fieldProps={{ precision: 4 }} rules={[{ required: true }]} />
        <ProFormText name="remark" label="备注" />
      </ProFormList>
    </ModalForm>
  );
}

function ReportForm({
  batch,
  onCancel,
  onFinish
}: {
  batch: ProductionBatchRecord | null;
  onCancel: () => void;
  onFinish: (values: ProductionReportPayload) => Promise<boolean>;
}) {
  return (
    <CreateForm
      title={batch ? `批次 ${batch.batchNo} 报工` : "生产报工"}
      open={!!batch}
      width={760}
      initialValues={{ batchId: batch?.id, reportQuantity: 1, goodQuantity: 1, defectQuantity: 0 }}
      onCancel={onCancel}
      onFinish={onFinish}
      sections={[
        {
          title: "报工信息",
          fields: [
            { type: "text", name: "batchId", label: "批次ID", colSpan: 24, fieldProps: { readOnly: true } },
            { type: "text", name: "reportNo", label: "报工单号", placeholder: "留空自动生成", colSpan: 12 },
            { type: "digit", name: "reportQuantity", label: "报工数量", min: 0.0001, precision: 4, rules: [{ required: true }], colSpan: 8 },
            { type: "digit", name: "goodQuantity", label: "良品数量", min: 0, precision: 4, colSpan: 8 },
            { type: "digit", name: "defectQuantity", label: "不良数量", min: 0, precision: 4, colSpan: 8 },
            { type: "text", name: "reportAt", label: "报工时间", placeholder: "YYYY-MM-DD HH:mm:ss，留空为当前时间", colSpan: 12 },
            { type: "text", name: "operatorName", label: "操作人", colSpan: 12 },
            { type: "textarea", name: "remark", label: "备注", colSpan: 24 }
          ]
        }
      ]}
    />
  );
}

function SerialGenerateForm({
  batch,
  onCancel,
  onFinish
}: {
  batch: ProductionBatchRecord | null;
  onCancel: () => void;
  onFinish: (values: SerialNumberGeneratePayload) => Promise<boolean>;
}) {
  return (
    <CreateForm
      title={batch ? `批次 ${batch.batchNo} 生成批次/序列号` : "生成序列号"}
      open={!!batch}
      width={520}
      initialValues={{ quantity: batch?.plannedQuantity, prefix: batch?.batchNo }}
      onCancel={onCancel}
      onFinish={onFinish}
      sections={[
        {
          title: "生成配置",
          fields: [
            { type: "digit", name: "quantity", label: "生成数量", min: 1, precision: 0, rules: [{ required: true }], colSpan: 24 },
            { type: "text", name: "prefix", label: "编码前缀", colSpan: 24 }
          ]
        }
      ]}
    />
  );
}

function PackForm({
  batch,
  onCancel,
  onFinish
}: {
  batch: ProductionBatchRecord | null;
  onCancel: () => void;
  onFinish: (values: ProductionBoxPayload) => Promise<boolean>;
}) {
  return (
    <CreateForm
      title={batch ? `批次 ${batch.batchNo} 装箱` : "装箱"}
      open={!!batch}
      width={720}
      initialValues={{ batchId: batch?.id, quantity: batch?.completedQuantity ?? 1 }}
      onCancel={onCancel}
      onFinish={onFinish}
      sections={[
        {
          title: "装箱信息",
          fields: [
            { type: "text", name: "batchId", label: "批次ID", colSpan: 24, fieldProps: { readOnly: true } },
            { type: "text", name: "packageId", label: "包装规格ID", rules: [{ required: true }], colSpan: 12 },
            { type: "digit", name: "quantity", label: "装箱数量", min: 0.0001, precision: 4, colSpan: 12 },
            { type: "textarea", name: "remark", label: "备注", colSpan: 24 }
          ]
        }
      ]}
    />
  );
}

function renderBatchStatus(status: string) {
  const item = batchStatusMap[status] ?? { text: status, color: "default" };
  return <Tag color={item.color}>{item.text}</Tag>;
}

function renderReportStatus(status: string) {
  const item = reportStatusMap[status] ?? { text: status, color: "default" };
  return <Tag color={item.color}>{item.text}</Tag>;
}

function renderMovementType(type: string) {
  const item = movementTypeMap[type] ?? { text: type, color: "default" };
  return <Tag color={item.color}>{item.text}</Tag>;
}

function formatDateTime(value?: string | null) {
  if (!value) return "-";
  return value.replace("T", " ").replace(/\+\d{2}:\d{2}$/, "");
}

function calculateRequiredQuantity(plannedQuantity: number, bomQuantity: number, lossRate?: number | null) {
  const rate = lossRate ? lossRate / 100 : 0;
  return Number((plannedQuantity * bomQuantity * (1 + rate)).toFixed(4));
}


function previewLabel(html?: string | null) {
  if (!html) return;
  const popup = window.open("", "_blank", "width=420,height=520");
  if (!popup) return;
  popup.document.write(`<html><head><title>箱码标签</title></head><body>${html}</body></html>`);
  popup.document.close();
}

function printMaterialMovement(record: ProductionMaterialMovementRecord) {
  const title = `${movementTypeMap[record.movementType]?.text ?? record.movementType}单`;
  const rows = (record.items ?? []).map((item, index) => `
    <tr>
      <td>${index + 1}</td>
      <td>${escapeHtml(item.materialCode ?? "")}</td>
      <td>${escapeHtml(item.materialName ?? "")}</td>
      <td>${item.quantity ?? ""}</td>
      <td>${escapeHtml(item.warehouseName ?? item.warehouseCode ?? "")}</td>
      <td>${escapeHtml(item.locationName ?? item.locationCode ?? "")}</td>
      <td>${escapeHtml(item.batchNo ?? "")}</td>
      <td>${escapeHtml(item.remark ?? "")}</td>
    </tr>
  `).join("");
  const html = `
    <html>
      <head>
        <title>${escapeHtml(title)}</title>
        <style>
          body { font-family: Arial, "Microsoft YaHei", sans-serif; padding: 24px; color: #111827; }
          h2 { text-align: center; margin: 0 0 18px; }
          .meta { display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px 24px; margin-bottom: 16px; font-size: 13px; }
          table { width: 100%; border-collapse: collapse; font-size: 12px; }
          th, td { border: 1px solid #d1d5db; padding: 7px; text-align: left; }
          th { background: #f3f4f6; }
          .sign { display: flex; justify-content: space-between; margin-top: 32px; font-size: 13px; }
        </style>
      </head>
      <body>
        <h2>${escapeHtml(title)}</h2>
        <div class="meta">
          <div>单据号：${escapeHtml(record.movementNo)}</div>
          <div>生产批次：${escapeHtml(record.batchNo)}</div>
          <div>类型：${escapeHtml(movementTypeMap[record.movementType]?.text ?? record.movementType)}</div>
          <div>库存单据：${escapeHtml(record.inventoryDocumentNo ?? record.inventoryDocumentId ?? "-")}</div>
          <div>仓库：${escapeHtml(record.warehouseName ?? record.warehouseCode ?? "-")}</div>
          <div>库位：${escapeHtml(record.locationName ?? record.locationCode ?? "-")}</div>
          <div>库存批次：${escapeHtml(record.batchNoInventory ?? "-")}</div>
          <div>打印时间：${new Date().toLocaleString()}</div>
          <div>备注：${escapeHtml(record.remark ?? "-")}</div>
        </div>
        <table>
          <thead><tr><th>#</th><th>物料编码</th><th>物料名称</th><th>数量</th><th>仓库</th><th>库位</th><th>批次</th><th>备注</th></tr></thead>
          <tbody>${rows || "<tr><td colspan='8'>无明细</td></tr>"}</tbody>
        </table>
        <div class="sign"><span>领/退料人：</span><span>仓库确认：</span><span>生产确认：</span></div>
        <script>window.onload=()=>window.print()</script>
      </body>
    </html>
  `;
  const popup = window.open("", "_blank", "width=900,height=720");
  if (!popup) return;
  popup.document.write(html);
  popup.document.close();
}

function escapeHtml(value: string) {
  return value.replace(/[&<>"']/g, (char) => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    "\"": "&quot;",
    "'": "&#39;"
  }[char] ?? char));
}
