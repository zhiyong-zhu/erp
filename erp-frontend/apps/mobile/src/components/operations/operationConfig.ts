export type OperationKey = "batch" | "production" | "receipt" | "issue" | "check" | "boxPrint" | "documentPrint";

export const operationCards: Array<{ key: OperationKey; title: string; description: string; tag: string }> = [
  { key: "batch", title: "创建工单", description: "按产品、数量和计划时间创建生产工单。", tag: "工单" },
  { key: "production", title: "生产执行", description: "扫描工单并提交生产报工。", tag: "车间" },
  { key: "receipt", title: "扫码入库", description: "扫描工单或箱码后确认入库。", tag: "入库" },
  { key: "issue", title: "扫码出库", description: "扫描物料并创建出库单。", tag: "出库" },
  { key: "check", title: "仓库盘点", description: "按物料扫码录入实盘数量。", tag: "盘点" },
  { key: "boxPrint", title: "箱码打印", description: "按工单生成箱码标签。", tag: "打印" },
  { key: "documentPrint", title: "出入库单据打印", description: "预览入库、出库、盘点单。", tag: "单据" }
];

export type OperationCard = (typeof operationCards)[number];
