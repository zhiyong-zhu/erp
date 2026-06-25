import { existsSync, readdirSync, readFileSync } from "node:fs";
import { join } from "node:path";

const root = new URL("..", import.meta.url).pathname;
const checks = [
  { file: "dist/index.html", includes: ["<div id=\"root\"></div>", "assets/index-"] },
  { file: "src/router/AppRouter.tsx", includes: ["/login", "/dashboard", "/inventory/warehouses", "/inventory/locations", "/inventory/transactions", "/sales/shipping", "/production/reports"] },
  { file: "src/pages/login/LoginPage.tsx", includes: ["登录"] },
  { file: "src/pages/dashboard/DashboardPage.tsx", includes: ["运营 Dashboard"] },
  { file: "src/pages/material/inventory/MaterialInventoryPage.tsx", includes: ["导出余额Excel", "库存余额"] },
  { file: "src/pages/inventory/transactions/InventoryTransactionPage.tsx", includes: ["导出流水Excel", "库存流水"] },
  { file: "src/pages/inventory/warehouses/InventoryWarehousePage.tsx", includes: ["仓库管理", "新建仓库"] },
  { file: "src/pages/inventory/locations/InventoryLocationPage.tsx", includes: ["库位管理", "新建库位"] },
  { file: "src/pages/inventory/receipts/InventoryReceiptPage.tsx", includes: ["printInventoryDocument(\"receipt\"", "正式入库单"] },
  { file: "src/pages/inventory/issues/InventoryIssuePage.tsx", includes: ["printInventoryDocument(\"issue\"", "出库管理"] },
  { file: "src/pages/inventory/transfers/InventoryTransferPage.tsx", includes: ["printInventoryDocument(\"transfer\"", "调拨管理"] },
  { file: "src/pages/inventory/checks/InventoryCheckPage.tsx", includes: ["printInventoryDocument(\"check\"", "盘点管理"] },
  { file: "src/pages/sales/orders/SaleOrderPage.tsx", includes: ["新建订单", "确认", "发货", "提交发货单", "本次序列号"] },
  { file: "src/pages/sales/shipping/ShippingPage.tsx", includes: ["导出发货Excel", "发货单明细", "复核出库", "序列号"] },
  { file: "src/pages/production/reports/ProductionReportPage.tsx", includes: ["投产", "生产领料", "生产退料", "生产报工", "装箱", "确认入库", "领退料单据", "成品库存"] }
];

const assetDir = join(root, "dist/assets");
if (!existsSync(assetDir) || !readdirSync(assetDir).some((file) => file.endsWith(".js"))) {
  throw new Error("Web smoke failed: dist/assets JS files are missing");
}

for (const check of checks) {
  const filepath = join(root, check.file);
  if (!existsSync(filepath)) {
    throw new Error(`Web smoke failed: missing ${check.file}`);
  }
  const content = readFileSync(filepath, "utf8");
  for (const expected of check.includes) {
    if (!content.includes(expected)) {
      throw new Error(`Web smoke failed: ${check.file} does not include ${expected}`);
    }
  }
}

console.log("Web smoke passed: build output, key routes, and primary operation buttons are present.");
