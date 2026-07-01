import { DeleteOutlined, PlusOutlined } from "@ant-design/icons";
import { App, Button, Card, Descriptions, Empty, Input, Space, Switch, Table, Tabs, Tag, Typography, Upload } from "antd";
import { PRODUCT_PERMISSIONS, PRODUCTION_PERMISSIONS } from "@erp/shared";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useMemo, useState } from "react";
import { changeProductStatusFlow, createProduct, exportProductsFile, fetchProductCategoryTree, fetchProductDetail, fetchProducts, importProductsFile, updateProduct } from "../../../api/product";
import { CreateForm } from "../../../components/CreateForm";
import type { FieldOption } from "../../../components/CreateForm";
import { ImageUploadField } from "../../../components/form/ImageUploadField";
import { SpecificationEditor } from "../../../components/form/SpecificationEditor";
import { SkuListField } from "../../../components/form/SkuListField";
import {
  buildSkuRowsFromSpecifications,
  createRowId,
  parseSpecifications,
  serializeSpecifications,
  type EditableSkuRow,
  type EditableSpecificationRow
} from "../../../components/form/types";
import { ProductBomTab } from "./ProductBomTab";
import { ProductPackageTab } from "./ProductPackageTab";
import { ProductLabelTemplateTab } from "./ProductLabelTemplateTab";
import { ProductLabelPrintTab } from "./ProductLabelPrintTab";
import { hasPermission } from "../../../store/auth";
import { useDictOptions } from "../../../hooks/useDictOptions";
import type { ProductCategoryRecord, ProductPayload, ProductRecord, ProductSkuRecord, ProductUpdatePayload } from "../../../types/product";

const { Title, Text } = Typography;

const productStatusOptions = [
  { label: "草稿", value: 0 },
  { label: "在售", value: 1 },
  { label: "停用", value: 2 }
];

function flattenCategories(categories: ProductCategoryRecord[]): ProductCategoryRecord[] {
  return categories.flatMap((category) => [category, ...flattenCategories(category.children ?? [])]);
}

export function ProductManagementPage() {
  const [loading, setLoading] = useState(false);
  const [products, setProducts] = useState<ProductRecord[]>([]);
  const [categories, setCategories] = useState<ProductCategoryRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [keyword, setKeyword] = useState("");
  const [createOpen, setCreateOpen] = useState(false);
  const [editingProduct, setEditingProduct] = useState<ProductRecord | null>(null);
  const [viewingProduct, setViewingProduct] = useState<ProductRecord | null>(null);
  const [detailTab, setDetailTab] = useState("base");
  const [viewportHeight, setViewportHeight] = useState(() => (typeof window === "undefined" ? 900 : window.innerHeight));
  const { message } = App.useApp();
  const canCreate = hasPermission(PRODUCT_PERMISSIONS.PRODUCT_CREATE);
  const canUpdate = hasPermission(PRODUCT_PERMISSIONS.PRODUCT_UPDATE);
  const canViewDetail = hasPermission(PRODUCT_PERMISSIONS.PRODUCT_DETAIL);
  const canViewCost = hasPermission(PRODUCT_PERMISSIONS.PRODUCT_COST);
  const canImport = hasPermission(PRODUCT_PERMISSIONS.PRODUCT_IMPORT);
  const canExport = hasPermission(PRODUCT_PERMISSIONS.PRODUCT_EXPORT);
  const canViewPackages = hasPermission(PRODUCT_PERMISSIONS.PACKAGE_LIST);
  const canUpdatePackages = hasPermission(PRODUCT_PERMISSIONS.PACKAGE_UPDATE);
  const canViewLabels = hasPermission(PRODUCT_PERMISSIONS.LABEL_LIST);
  const canUpdateLabels = hasPermission(PRODUCT_PERMISSIONS.LABEL_UPDATE);
  const canViewBom = hasPermission(PRODUCTION_PERMISSIONS.BOM_LIST);
  const canCreateBom = hasPermission(PRODUCTION_PERMISSIONS.BOM_CREATE);
  const canUpdateBom = hasPermission(PRODUCTION_PERMISSIONS.BOM_UPDATE);
  const canPrintLabels = hasPermission(PRODUCT_PERMISSIONS.LABEL_PRINT);

  const categoryOptions = useMemo(
    () => flattenCategories(categories).map((category) => ({ label: category.name, value: category.id })),
    [categories]
  );
  const { options: unitOptions } = useDictOptions("product_unit");

  useEffect(() => {
    void loadCategories();
    void loadProducts();
  }, []);

  useEffect(() => {
    function handleResize() {
      setViewportHeight(window.innerHeight);
    }
    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  async function loadCategories() {
    setCategories(await fetchProductCategoryTree());
  }

  async function loadProducts(nextPageNum = pageNum, nextPageSize = pageSize, name = keyword) {
    setLoading(true);
    try {
      const data = await fetchProducts({ pageNum: nextPageNum, pageSize: nextPageSize, name });
      setProducts(data.records);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
      setTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载产品失败");
    } finally {
      setLoading(false);
    }
  }

  async function handleCreate(values: ProductPayload) {
    try {
      await createProduct(values);
      message.success("产品创建成功");
      setCreateOpen(false);
      await loadProducts(1, pageSize);
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "产品创建失败");
      return false;
    }
  }

  async function handleOpenEdit(record: ProductRecord) {
    try {
      const detail = await fetchProductDetail(record.id);
      setEditingProduct(detail);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载产品详情失败");
    }
  }

  async function handleOpenView(record: ProductRecord) {
    try {
      const detail = await fetchProductDetail(record.id);
      setViewingProduct(detail);
      setDetailTab("base");
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载产品详情失败");
    }
  }

  async function handleUpdate(values: ProductUpdatePayload) {
    if (!editingProduct) {
      return false;
    }
    try {
      await updateProduct(editingProduct.id, values);
      message.success("产品更新成功");
      setEditingProduct(null);
      await loadProducts();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "产品更新失败");
      return false;
    }
  }

  async function handleToggleStatus(product: ProductRecord, checked: boolean) {
    try {
      await changeProductStatusFlow(product.id, { action: checked ? "enable" : "disable" });
      message.success(checked ? "产品已上架" : "产品已停用");
      await loadProducts();
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "状态更新失败");
    }
  }

  async function handleStatusAction(product: ProductRecord, action: "submit" | "reject") {
    try {
      await changeProductStatusFlow(product.id, { action });
      message.success(action === "submit" ? "产品已提交上架" : "产品已驳回为草稿");
      await loadProducts();
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "状态流转失败");
    }
  }

  async function handleExport() {
    try {
      const blob = await exportProductsFile();
      const url = URL.createObjectURL(blob);
      const anchor = document.createElement("a");
      anchor.href = url;
      anchor.download = "products.xlsx";
      anchor.click();
      URL.revokeObjectURL(url);
      message.success("产品导出成功");
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "产品导出失败");
    }
  }

  async function handleImport(file: File) {
    try {
      await importProductsFile(file);
      message.success("产品导入成功");
      await loadProducts();
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "产品导入失败");
    }
    return false;
  }

  const columns: ColumnsType<ProductRecord> = [
    { title: "产品编码", dataIndex: "code", key: "code" },
    { title: "产品名称", dataIndex: "name", key: "name" },
    { title: "分类", dataIndex: "categoryName", key: "categoryName" },
    { title: "品牌", dataIndex: "brand", key: "brand" },
    { title: "单位", dataIndex: "unit", key: "unit", width: 80 },
    { title: "SKU数", dataIndex: "skuCount", key: "skuCount", width: 90 },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      render: (_, record) => <Switch checked={record.status === 1} checkedChildren="在售" unCheckedChildren="停用" disabled={!canUpdate} onChange={(checked: boolean) => void handleToggleStatus(record, checked)} />
    },
    {
      title: "半成品",
      dataIndex: "isSemifinished",
      key: "isSemifinished",
      width: 80,
      render: (value) => value ? <Tag color="orange">半成品</Tag> : <Tag>成品</Tag>
    },
    {
      title: "操作",
      key: "actions",
      render: (_, record) => (
        <Space size="small">
          <Button type="link" disabled={!canViewDetail} onClick={() => void handleOpenView(record)}>查看</Button>
          <Button type="link" disabled={!canUpdate} onClick={() => void handleOpenEdit(record)}>编辑</Button>
          {record.status === 0 ? <Button type="link" disabled={!canUpdate} onClick={() => void handleStatusAction(record, "submit")}>提交上架</Button> : null}
          {record.status === 1 ? <Button type="link" disabled={!canUpdate} onClick={() => void handleStatusAction(record, "reject")}>驳回草稿</Button> : null}
        </Space>
      )
    }
  ];

  // 可用高度估算，用于列表表格 scroll.y 和详情内容区高度
  const availableHeight = Math.max(viewportHeight - 200, 480);
  // 未选产品时列表占满；选中后上下各占约一半
  const halfHeight = Math.floor((availableHeight - 16) / 2);
  const listScrollY = Math.max((viewingProduct ? halfHeight : availableHeight) - 110, 220);
  const detailBodyHeight = Math.max(halfHeight - 90, 120);

  return (
    <section style={{ height: "100%", display: "flex", flexDirection: "column", minHeight: 0 }}>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>产品管理 / 产品列表</Title>
          <Text type="secondary">维护产品基础资料和 SKU 信息，作为后续库存、销售、采购的主数据。</Text>
        </div>
        <Space>
          <Input.Search
            allowClear
            placeholder="搜索产品名称"
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
            onSearch={(value) => {
              setKeyword(value);
              void loadProducts(1, pageSize, value);
            }}
            style={{ width: 240 }}
          />
          <Upload beforeUpload={handleImport} showUploadList={false} disabled={!canImport}>
            <Button disabled={!canImport}>导入产品</Button>
          </Upload>
          <Button disabled={!canExport} onClick={() => void handleExport()}>导出产品</Button>
          <Button type="primary" icon={<PlusOutlined />} disabled={!canCreate} onClick={() => setCreateOpen(true)}>新建产品</Button>
        </Space>
      </div>

      <div className="product-page-split">
        <div className="product-page-panel" style={{ flex: viewingProduct ? "1 1 0" : "1 1 100%" }}>
          <Card size="small" className="product-list-panel" styles={{ body: { padding: 0 } }}>
            <Table
              rowKey="id"
              columns={columns}
              dataSource={products}
              loading={loading}
              size="small"
              onRow={(record) => ({
                onClick: () => {
                  if (canViewDetail) {
                    void handleOpenView(record);
                  }
                }
              })}
              rowClassName={(record) => record.id === viewingProduct?.id ? "erp-table-row-active" : ""}
              pagination={{
                current: pageNum,
                pageSize,
                total,
                showSizeChanger: true,
                showTotal: (count) => `共 ${count} 条`,
                onChange: (nextPageNum, nextPageSize) => void loadProducts(nextPageNum, nextPageSize)
              }}
              scroll={{ y: listScrollY }}
            />
          </Card>
        </div>

        {viewingProduct ? (
          <div className="product-page-panel" style={{ flex: "1 1 0" }}>
            <ProductDetailPanel
              product={viewingProduct}
              activeTab={detailTab}
              canViewCost={canViewCost}
              detailBodyHeight={detailBodyHeight}
              canViewPackages={canViewPackages}
              canUpdatePackages={canUpdatePackages}
              canViewLabels={canViewLabels}
              canUpdateLabels={canUpdateLabels}
              canViewBom={canViewBom}
              canCreateBom={canCreateBom}
              canUpdateBom={canUpdateBom}
              canPrintLabels={canPrintLabels}
              onTabChange={setDetailTab}
            />
          </div>
        ) : null}
      </div>

      <ProductForm
        title="新建产品"
        open={createOpen}
        categoryOptions={categoryOptions}
        unitOptions={unitOptions}
        canViewCost={canViewCost}
        onCancel={() => setCreateOpen(false)}
        onFinish={handleCreate}
      />
      <ProductForm
        title="编辑产品"
        open={!!editingProduct}
        categoryOptions={categoryOptions}
        unitOptions={unitOptions}
        canViewCost={canViewCost}
        initialValues={editingProduct ? {
          code: editingProduct.code,
          name: editingProduct.name,
          categoryId: editingProduct.categoryId ?? undefined,
          brand: editingProduct.brand ?? "",
          unit: editingProduct.unit,
          description: editingProduct.description ?? "",
          images: editingProduct.images,
          specifications: editingProduct.specifications ?? "",
          status: editingProduct.status,
          isSemifinished: editingProduct.isSemifinished ?? false,
          skus: editingProduct.skus?.map((sku) => ({
            rowId: createRowId(),
            skuCode: sku.skuCode,
            attributes: sku.attributes,
            barcode: sku.barcode ?? "",
            price: sku.price ?? undefined,
            costPrice: sku.costPrice ?? undefined,
            weight: sku.weight ?? undefined,
            status: sku.status ?? 1
          }))
        } : undefined}
        onCancel={() => setEditingProduct(null)}
        onFinish={handleUpdate}
        editing
      />
    </section>
  );
}

function ProductDetailPanel({ product, activeTab, canViewCost, canViewPackages, canUpdatePackages, canViewLabels, canUpdateLabels, canViewBom, canCreateBom, canUpdateBom, canPrintLabels, detailBodyHeight, onTabChange }: {
  product: ProductRecord | null;
  activeTab: string;
  canViewCost: boolean;
  detailBodyHeight: number;
  canViewPackages: boolean;
  canUpdatePackages: boolean;
  canViewLabels: boolean;
  canUpdateLabels: boolean;
  canViewBom: boolean;
  canCreateBom: boolean;
  canUpdateBom: boolean;
  canPrintLabels: boolean;
  onTabChange: (key: string) => void;
}) {
  const skuColumns: ColumnsType<ProductSkuRecord> = [
    { title: "SKU编码", dataIndex: "skuCode", key: "skuCode", width: 160 },
    { title: "属性", dataIndex: "attributes", key: "attributes", render: (value) => <code>{value}</code> },
    { title: "条码", dataIndex: "barcode", key: "barcode", width: 140 },
    { title: "售价", dataIndex: "price", key: "price", width: 100 },
    ...(canViewCost ? [{ title: "成本价", dataIndex: "costPrice", key: "costPrice", width: 100 } as ColumnsType<ProductSkuRecord>[number]] : []),
    { title: "重量", dataIndex: "weight", key: "weight", width: 100 },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 90,
      render: (value) => <Tag color={value === 1 ? "success" : "default"}>{value === 1 ? "启用" : "禁用"}</Tag>
    }
  ];

  return (
    <Card
      size="small"
      title={product ? `产品详情 · ${product.name}` : "产品详情"}
      className="product-detail-panel"
      styles={{ body: { padding: "0 12px 12px", height: "100%", display: "flex", flexDirection: "column", overflow: "hidden" } }}
    >
      {product ? (
        <Tabs
          activeKey={activeTab}
          onChange={onTabChange}
          className="product-detail-tabs"
          items={[
            {
              key: "base",
              label: "基础信息",
              children: (
                <Descriptions column={2} bordered size="small">
                  <Descriptions.Item label="产品编码">{product.code}</Descriptions.Item>
                  <Descriptions.Item label="产品名称">{product.name}</Descriptions.Item>
                  <Descriptions.Item label="分类">{product.categoryName ?? "-"}</Descriptions.Item>
                  <Descriptions.Item label="品牌">{product.brand ?? "-"}</Descriptions.Item>
                  <Descriptions.Item label="单位">{product.unit}</Descriptions.Item>
                  <Descriptions.Item label="状态">
                    <Tag color={product.status === 1 ? "success" : product.status === 2 ? "default" : "processing"}>
                      {product.status === 1 ? "在售" : product.status === 2 ? "停用" : "草稿"}
                    </Tag>
                  </Descriptions.Item>
                  <Descriptions.Item label="描述" span={2}>{product.description || "-"}</Descriptions.Item>
                  <Descriptions.Item label="规格定义" span={2}>
                    {product.specifications ? <code>{product.specifications}</code> : "-"}
                  </Descriptions.Item>
                </Descriptions>
              )
            },
            {
              key: "sku",
              label: `SKU列表 (${product.skus?.length ?? 0})`,
              children: product.skus && product.skus.length > 0 ? (
                <Table
                  rowKey={(record) => record.id ?? record.skuCode}
                  columns={skuColumns}
                  dataSource={product.skus}
                  pagination={false}
                  size="small"
                  scroll={{ x: 860 }}
                />
              ) : (
                <Empty description="暂无SKU" />
              )
            },
            {
              key: "package",
              label: "包装规格",
              children: canViewPackages
                ? <ProductPackageTab productId={product.id} canUpdatePackages={canUpdatePackages} height={detailBodyHeight} />
                : <Empty description="无包装规格查看权限" />
            },
            {
              key: "bom",
              label: "BOM",
              children: canViewBom
                ? <ProductBomTab productId={product.id} productName={product.name} canCreate={canCreateBom} canUpdate={canUpdateBom} height={detailBodyHeight} />
                : <Empty description="无BOM查看权限" />
            },
            {
              key: "label",
              label: "标签模板",
              children: canViewLabels
                ? <ProductLabelTemplateTab height={detailBodyHeight} canUpdate={canUpdateLabels} />
                : <Empty description="无标签模板查看权限" />
            },
            {
              key: "print",
              label: "标签打印",
              children: canPrintLabels
                ? <ProductLabelPrintTab product={product} height={detailBodyHeight} canPrint={canPrintLabels} />
                : <Empty description="无标签打印权限" />
            }
          ]}
        />
      ) : (
        <Empty description="请选择上方产品行查看详情" />
      )}
    </Card>
  );
}

function ProductForm({ title, open, categoryOptions, unitOptions, canViewCost, initialValues, onCancel, onFinish, editing = false }: {
  title: string;
  open: boolean;
  categoryOptions: Array<{ label: string; value: string }>;
  unitOptions: FieldOption[];
  canViewCost: boolean;
  initialValues?: Partial<ProductPayload & ProductUpdatePayload>;
  onCancel: () => void;
  onFinish: (values: any) => Promise<boolean>;
  editing?: boolean;
}) {
  const { message } = App.useApp();
  const [skuRows, setSkuRows] = useState<EditableSkuRow[]>([]);
  const [specRows, setSpecRows] = useState<EditableSpecificationRow[]>([]);
  const [imageUrls, setImageUrls] = useState<string[]>([]);

  useEffect(() => {
    if (!open) {
      setSkuRows([]);
      setSpecRows([]);
      return;
    }
    const nextRows = (initialValues?.skus as EditableSkuRow[] | undefined)?.map((sku) => ({
      ...sku,
      rowId: sku.rowId ?? createRowId()
    })) ?? [];
    setSkuRows(nextRows);
    setSpecRows(parseSpecifications(initialValues?.specifications));
    setImageUrls((initialValues?.images as string[] | undefined) ?? []);
  }, [initialValues?.skus, initialValues?.specifications, initialValues?.images, open]);

  function handleGenerateSkus(mode: "append" | "replace") {
    const generatedRows = buildSkuRowsFromSpecifications(specRows);
    if (generatedRows.length === 0) {
      message.warning("请先维护至少一组有效规格和值");
      return;
    }
    setSkuRows((rows) => {
      if (mode === "replace") {
        message.success(`已按当前规格重建 ${generatedRows.length} 条SKU候选`);
        return generatedRows;
      }
      const existingAttributes = new Set(rows.map((row) => row.attributes));
      const newRows = generatedRows.filter((row) => !existingAttributes.has(row.attributes));
      if (newRows.length === 0) {
        message.info("当前规格组合已全部存在，无需补充");
        return rows;
      }
      message.success(`已补充 ${newRows.length} 条SKU候选`);
      return [...rows, ...newRows];
    });
  }

  async function handleSubmit(values: any) {
    const normalizedSkus = skuRows
      .filter((row) => row.skuCode && row.attributes)
      .map(({ rowId, ...sku }) => sku);
    if (normalizedSkus.length === 0) {
      message.error("至少需要维护一个SKU");
      return false;
    }
    return onFinish({
      ...values,
      images: imageUrls,
      specifications: serializeSpecifications(specRows),
      skus: normalizedSkus
    });
  }

  return (
    <CreateForm
      title={title}
      open={open}
      width={1080}
      initialValues={initialValues ?? { status: 0, isSemifinished: false }}
      onCancel={onCancel}
      onFinish={handleSubmit}
      sections={[
        {
          title: "基本信息",
          fields: [
            ...(editing ? [] : [{ type: "text" as const, name: "code", label: "产品编码", rules: [{ required: true }], colSpan: 12 }]),
            { type: "text", name: "name", label: "产品名称", rules: [{ required: true }], colSpan: 12 },
            { type: "select", name: "categoryId", label: "产品分类", options: categoryOptions, colSpan: 12 },
            { type: "text", name: "brand", label: "品牌", colSpan: 12 },
            { type: "select", name: "unit", label: "单位", options: unitOptions, rules: [{ required: true }], colSpan: 12 },
            { type: "select", name: "status", label: "状态", options: productStatusOptions, colSpan: 12 },
            { type: "switch", name: "isSemifinished", label: "是否半成品", defaultChecked: false, colSpan: 12 },
            { type: "textarea", name: "description", label: "描述", colSpan: 24 }
          ]
        },
        {
          title: "产品资料",
          slot: (
            <>
              <ImageUploadField value={imageUrls} onChange={setImageUrls} />
              <SpecificationEditor
                value={specRows}
                onChange={setSpecRows}
                onAppendSkus={() => handleGenerateSkus("append")}
                onRebuildSkus={() => handleGenerateSkus("replace")}
              />
            </>
          )
        },
        {
          title: "SKU 列表",
          slot: <SkuListField value={skuRows} onChange={setSkuRows} canViewCost={canViewCost} />
        }
      ]}
    />
  );
}
