export type OperationKey = "batch" | "production" | "receipt" | "issue" | "check" | "boxPrint" | "documentPrint";

export const operationCards: Array<{ key: OperationKey; title: string; description: string; tag: string }> = [
  { key: "batch", title: "创建工单", description: "按产品、数量、计划时间创建生产工单。", tag: "生产准备" },
  { key: "production", title: "生产执行", description: "投产、报工、完工入库和进度刷新。", tag: "车间操作" },
  { key: "receipt", title: "扫码入库", description: "输入/扫描工单或箱码，确认完工入库。", tag: "仓库操作" },
  { key: "issue", title: "扫码出库", description: "输入/扫描物料，创建出库单并扣减库存。", tag: "仓库操作" },
  { key: "check", title: "仓库盘点", description: "输入实盘数量，生成盘盈/盘亏流水。", tag: "库存校准" },
  { key: "boxPrint", title: "箱码打印", description: "按工单和包装规格生成箱码并预览打印。", tag: "标签打印" },
  { key: "documentPrint", title: "出入库单据打印", description: "预览并打印入库、出库、盘点单据。", tag: "单据打印" }
];

export interface BatchFormState {
  batchNo: string;
  productId: string;
  plannedQuantity: string;
  unit: string;
  plannedStartDate: string;
  plannedEndDate: string;
  remark: string;
}

export interface ReportFormState {
  reportQuantity: string;
  goodQuantity: string;
  defectQuantity: string;
  operatorName: string;
  remark: string;
}

export interface IssueFormState {
  sourceOrderNo: string;
  materialId: string;
  quantity: string;
  remark: string;
}

export interface CheckFormState {
  materialId: string;
  actualQuantity: string;
  remark: string;
}

export interface BoxFormState {
  batchId: string;
  packageId: string;
  quantity: string;
  remark: string;
}
