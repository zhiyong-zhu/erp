import { DeleteOutlined, PlusOutlined } from "@ant-design/icons";
import { ModalForm, ProFormDigit, ProFormSelect, ProFormText, ProFormTextArea } from "@ant-design/pro-components";
import { App, Button, Card, Descriptions, Empty, Input, Popconfirm, Space, Switch, Table, Tabs, Tag, Typography } from "antd";
import { PRODUCT_PERMISSIONS } from "@erp/shared";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useMemo, useState } from "react";
import { createProduct, fetchProductCategoryTree, fetchProductDetail, fetchProducts, updateProduct, updateProductStatus } from "../../../api/product";
import { ProductPackageTab } from "./ProductPackageTab";
import { ProductLabelTemplateTab } from "./ProductLabelTemplateTab";
import { hasPermission } from "../../../store/auth";
import type { ProductCategoryRecord, ProductPayload, ProductRecord, ProductSkuRecord, ProductUpdatePayload } from "../../../types/product";

const { Title, Text } = Typography;

const productStatusOptions = [
  { label: "草稿", value: 0 },
  { label: "在售", value: 1 },
  { label: "停用", value: 2 }
];

const skuStatusOptions = [
  { label: "启用", value: 1 },
  { label: "禁用", value: 0 }
];

const productSpecificationsExample = '{"颜色":["银色","黑色"],"容量":["500ml"]}';
const skuAttributesExample = '{"颜色":"银色","容量":"500ml"}';

type EditableSkuRow = ProductSkuRecord & { rowId: string };
type EditableSpecificationRow = { rowId: string; key: string; valuesText: string };
type EditableAttributeRow = { rowId: string; key: string; value: string };

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
  const canViewPackages = hasPermission(PRODUCT_PERMISSIONS.PACKAGE_LIST);
  const canUpdatePackages = hasPermission(PRODUCT_PERMISSIONS.PACKAGE_UPDATE);
  const canViewLabels = hasPermission(PRODUCT_PERMISSIONS.LABEL_LIST);
  const canUpdateLabels = hasPermission(PRODUCT_PERMISSIONS.LABEL_UPDATE);

  const categoryOptions = useMemo(
    () => flattenCategories(categories).map((category) => ({ label: category.name, value: category.id })),
    [categories]
  );

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
      await updateProductStatus(product.id, checked ? 1 : 2);
      message.success(checked ? "产品已上架" : "产品已停用");
      await loadProducts();
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "状态更新失败");
    }
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
      render: (_, record) => <Switch checked={record.status === 1} checkedChildren="在售" unCheckedChildren="停用" disabled={!canUpdate} onChange={(checked) => void handleToggleStatus(record, checked)} />
    },
    {
      title: "操作",
      key: "actions",
      render: (_, record) => (
        <Space size="small">
          <Button type="link" disabled={!canViewDetail} onClick={() => void handleOpenView(record)}>查看</Button>
          <Button type="link" disabled={!canUpdate} onClick={() => void handleOpenEdit(record)}>编辑</Button>
        </Space>
      )
    }
  ];

  const splitHeight = Math.max(viewportHeight - 260, 520);
  const topPanelHeight = viewingProduct ? Math.max(Math.floor(splitHeight * 0.7), 320) : splitHeight;
  const detailPanelHeight = viewingProduct ? Math.max(splitHeight - topPanelHeight, 180) : 0;
  const listScrollY = Math.max(topPanelHeight - 170, 220);
  const detailBodyHeight = Math.max(detailPanelHeight - 110, 120);

  return (
    <section>
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
          <Button type="primary" icon={<PlusOutlined />} disabled={!canCreate} onClick={() => setCreateOpen(true)}>新建产品</Button>
        </Space>
      </div>

      <div className="product-page-split" style={{ height: splitHeight }}>
        <div className="product-page-panel" style={{ height: topPanelHeight }}>
          <Card size="small" className="product-list-panel">
            <Table
              rowKey="id"
              columns={columns}
              dataSource={products}
              loading={loading}
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
          <div className="product-page-panel" style={{ height: detailPanelHeight }}>
            <ProductDetailPanel
              product={viewingProduct}
              activeTab={detailTab}
              canViewCost={canViewCost}
              detailBodyHeight={detailBodyHeight}
              canViewPackages={canViewPackages}
              canUpdatePackages={canUpdatePackages}
              canViewLabels={canViewLabels}
              canUpdateLabels={canUpdateLabels}
              onTabChange={setDetailTab}
            />
          </div>
        ) : null}
      </div>

      <ProductForm
        title="新建产品"
        open={createOpen}
        categoryOptions={categoryOptions}
        canViewCost={canViewCost}
        onCancel={() => setCreateOpen(false)}
        onFinish={handleCreate}
      />
      <ProductForm
        title="编辑产品"
        open={!!editingProduct}
        categoryOptions={categoryOptions}
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

function ProductDetailPanel({ product, activeTab, canViewCost, canViewPackages, canUpdatePackages, canViewLabels, canUpdateLabels, detailBodyHeight, onTabChange }: {
  product: ProductRecord | null;
  activeTab: string;
  canViewCost: boolean;
  detailBodyHeight: number;
  canViewPackages: boolean;
  canUpdatePackages: boolean;
  canViewLabels: boolean;
  canUpdateLabels: boolean;
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
    >
      {product ? (
        <Tabs
          activeKey={activeTab}
          onChange={onTabChange}
          items={[
            {
              key: "base",
              label: "基础信息",
              children: (
                <div className="product-detail-tab-scroll" style={{ height: detailBodyHeight }}>
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
                </div>
              )
            },
            {
              key: "sku",
              label: `SKU列表 (${product.skus?.length ?? 0})`,
              children: product.skus && product.skus.length > 0 ? (
                <div className="product-detail-tab-scroll" style={{ height: detailBodyHeight }}>
                  <Table
                    rowKey={(record) => record.id ?? record.skuCode}
                    columns={skuColumns}
                    dataSource={product.skus}
                    pagination={false}
                    scroll={{ x: 860, y: Math.max(detailBodyHeight - 24, 100) }}
                  />
                </div>
              ) : (
                <div className="product-detail-tab-scroll" style={{ height: detailBodyHeight }}><Empty description="暂无SKU" /></div>
              )
            },
            {
              key: "package",
              label: "包装规格",
              children: canViewPackages
                ? <ProductPackageTab productId={product.id} canUpdatePackages={canUpdatePackages} height={detailBodyHeight} />
                : <div className="product-detail-tab-scroll" style={{ height: detailBodyHeight }}><Empty description="无包装规格查看权限" /></div>
            },
            {
              key: "bom",
              label: "BOM",
              children: <div className="product-detail-tab-scroll" style={{ height: detailBodyHeight }}><Empty description="BOM模块待接入" /></div>
            },
            {
              key: "label",
              label: "标签模板",
              children: canViewLabels
                ? <ProductLabelTemplateTab height={detailBodyHeight} canUpdate={canUpdateLabels} />
                : <div className="product-detail-tab-scroll" style={{ height: detailBodyHeight }}><Empty description="无标签模板查看权限" /></div>
            }
          ]}
        />
      ) : (
        <Empty description="请选择上方产品行查看详情" />
      )}
    </Card>
  );
}

function ProductForm({ title, open, categoryOptions, canViewCost, initialValues, onCancel, onFinish, editing = false }: {
  title: string;
  open: boolean;
  categoryOptions: Array<{ label: string; value: string }>;
  canViewCost: boolean;
  initialValues?: Partial<ProductPayload & ProductUpdatePayload>;
  onCancel: () => void;
  onFinish: (values: any) => Promise<boolean>;
  editing?: boolean;
}) {
  const { message } = App.useApp();
  const [skuRows, setSkuRows] = useState<EditableSkuRow[]>([]);
  const [specRows, setSpecRows] = useState<EditableSpecificationRow[]>([]);
  const [skuModalOpen, setSkuModalOpen] = useState(false);
  const [editingSkuRowId, setEditingSkuRowId] = useState<string | null>(null);
  const [skuAttributeRows, setSkuAttributeRows] = useState<EditableAttributeRow[]>([]);

  useEffect(() => {
    if (!open) {
      setSkuRows([]);
      setSpecRows([]);
      setSkuModalOpen(false);
      setEditingSkuRowId(null);
      setSkuAttributeRows([]);
      return;
    }

    const nextRows = (initialValues?.skus as ProductSkuRecord[] | undefined)?.map((sku) => ({
      ...sku,
      rowId: sku.rowId ?? createRowId()
    })) ?? [];
    setSkuRows(nextRows);
    setSpecRows(parseSpecifications(initialValues?.specifications));
  }, [initialValues?.skus, initialValues?.specifications, open]);

  const editingSku = editingSkuRowId ? skuRows.find((row) => row.rowId === editingSkuRowId) ?? null : null;

  const skuColumns: ColumnsType<EditableSkuRow> = [
    { title: "SKU编码", dataIndex: "skuCode", key: "skuCode", width: 160 },
    {
      title: "属性",
      dataIndex: "attributes",
      key: "attributes",
      render: (value) => <code>{value}</code>
    },
    { title: "条码", dataIndex: "barcode", key: "barcode", width: 140 },
    { title: "售价", dataIndex: "price", key: "price", width: 90 },
    ...(canViewCost ? [{ title: "成本价", dataIndex: "costPrice", key: "costPrice", width: 90 } as ColumnsType<EditableSkuRow>[number]] : []),
    { title: "重量", dataIndex: "weight", key: "weight", width: 90 },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 90,
      render: (value) => <Tag color={value === 1 ? "success" : "default"}>{value === 1 ? "启用" : "禁用"}</Tag>
    },
    {
      title: "操作",
      key: "actions",
      width: 140,
      render: (_, record) => (
        <Space size="small">
          <Button type="link" onClick={() => openEditSku(record.rowId)}>编辑</Button>
          <Button type="link" danger onClick={() => removeSku(record.rowId)}>删除</Button>
        </Space>
      )
    }
  ];

  function openCreateSku() {
    setEditingSkuRowId(null);
    setSkuAttributeRows([createEmptyAttributeRow()]);
    setSkuModalOpen(true);
  }

  function openEditSku(rowId: string) {
    setEditingSkuRowId(rowId);
    const row = skuRows.find((item) => item.rowId === rowId);
    setSkuAttributeRows(parseAttributes(row?.attributes));
    setSkuModalOpen(true);
  }

  function removeSku(rowId: string) {
    setSkuRows((rows) => rows.filter((row) => row.rowId !== rowId));
  }

  async function handleSubmit(values: any) {
    const normalizedSkus = skuRows
      .filter((row) => row.skuCode && row.attributes)
      .map(({ rowId, ...sku }) => sku);

    if (normalizedSkus.length === 0) {
      message.error("至少需要维护一个SKU");
      return false;
    }

    const payload = {
      ...values,
      specifications: serializeSpecifications(specRows),
      skus: normalizedSkus
    };
    return onFinish(payload);
  }

  async function handleSaveSku(values: EditableSkuRow) {
    const attributes = serializeAttributes(skuAttributeRows);
    if (!attributes) {
      message.error("至少需要维护一个SKU属性");
      return false;
    }

    const nextRow: EditableSkuRow = {
      ...values,
      attributes,
      rowId: editingSkuRowId ?? createRowId()
    };

    setSkuRows((rows) => {
      if (editingSkuRowId) {
        return rows.map((row) => (row.rowId === editingSkuRowId ? nextRow : row));
      }
      return [...rows, nextRow];
    });
    setSkuModalOpen(false);
    setEditingSkuRowId(null);
    setSkuAttributeRows([]);
    return true;
  }

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

  return (
    <>
      <ModalForm<any>
        title={title}
        open={open}
        width={960}
        initialValues={initialValues ?? { status: 0 }}
        modalProps={{ destroyOnClose: true, onCancel }}
        onFinish={handleSubmit}
      >
        {!editing ? <ProFormText name="code" label="产品编码" rules={[{ required: true }]} /> : null}
        <ProFormText name="name" label="产品名称" rules={[{ required: true }]} />
        <ProFormSelect name="categoryId" label="产品分类" options={categoryOptions} />
        <ProFormText name="brand" label="品牌" />
        <ProFormText name="unit" label="单位" rules={[{ required: true }]} />
        <ProFormTextArea name="description" label="描述" fieldProps={{ autoSize: { minRows: 3, maxRows: 5 } }} />

        <Card
          size="small"
          title="规格定义"
          className="product-spec-card"
          extra={
            <Space>
              <Button icon={<PlusOutlined />} onClick={() => setSpecRows((rows) => [...rows, createEmptySpecificationRow()])}>新增规格</Button>
              <Button onClick={() => handleGenerateSkus("append")}>补全SKU候选</Button>
              <Popconfirm
                title="重建SKU候选"
                description="会按当前规格组合重建SKU列表，已手工维护的价格、条码、重量会被覆盖，确定继续吗？"
                okText="重建"
                cancelText="取消"
                onConfirm={() => handleGenerateSkus("replace")}
              >
                <Button danger>重建SKU候选</Button>
              </Popconfirm>
            </Space>
          }
        >
          <Text type="secondary" className="product-sku-hint">
            结构化维护规格名称和可选值，提交时会自动转换为 JSON。示例：<code>{productSpecificationsExample}</code>
          </Text>
          {specRows.length === 0 ? (
            <div className="product-spec-empty">暂无规格定义，可按需新增。</div>
          ) : (
            specRows.map((row) => (
              <div key={row.rowId} className="product-spec-row">
                <Input
                  value={row.key}
                  placeholder="规格名称，如 颜色"
                  onChange={(event) => updateSpecificationRow(row.rowId, "key", event.target.value, setSpecRows)}
                />
                <Input
                  value={row.valuesText}
                  placeholder="可选值，用逗号分隔，如 银色, 黑色"
                  onChange={(event) => updateSpecificationRow(row.rowId, "valuesText", event.target.value, setSpecRows)}
                />
                <Button
                  danger
                  icon={<DeleteOutlined />}
                  onClick={() => setSpecRows((rows) => rows.filter((item) => item.rowId !== row.rowId))}
                />
              </div>
            ))
          )}
        </Card>

        <ProFormSelect name="status" label="状态" options={productStatusOptions} />

        <div className="product-sku-section">
          <Text type="secondary" className="product-sku-hint">
            SKU 独立表维护。点击新增/编辑在弹窗中处理单条 SKU，避免多条时页面错乱。
          </Text>
        </div>
        <Card
          size="small"
          title="SKU表"
          className="product-sku-card"
          extra={<Button type="primary" onClick={openCreateSku}>新增SKU</Button>}
        >
          <Table
            rowKey="rowId"
            columns={skuColumns}
            dataSource={skuRows}
            pagination={false}
            locale={{ emptyText: "暂无SKU，请新增" }}
            scroll={{ x: 1000 }}
          />
        </Card>
      </ModalForm>

      <ModalForm<EditableSkuRow>
        title={editingSku ? "编辑SKU" : "新增SKU"}
        open={skuModalOpen}
        initialValues={editingSku ?? createEmptySkuRow()}
        modalProps={{
          destroyOnClose: true,
          onCancel: () => {
            setSkuModalOpen(false);
            setEditingSkuRowId(null);
            setSkuAttributeRows([]);
          }
        }}
        onFinish={handleSaveSku}
      >
        <ProFormText name="skuCode" label="SKU编码" rules={[{ required: true }]} />
        <ProFormText name="barcode" label="条码" />

        <Card
          size="small"
          title="SKU属性"
          className="product-spec-card"
          extra={<Button icon={<PlusOutlined />} onClick={() => setSkuAttributeRows((rows) => [...rows, createEmptyAttributeRow()])}>新增属性</Button>}
        >
          <Text type="secondary" className="product-sku-hint">
            使用键值对维护 SKU 属性，提交时会自动转换为 JSON。示例：<code>{skuAttributesExample}</code>
          </Text>
          {skuAttributeRows.map((row) => (
            <div key={row.rowId} className="product-spec-row">
              <Input
                value={row.key}
                placeholder="属性名，如 颜色"
                onChange={(event) => updateAttributeRow(row.rowId, "key", event.target.value, setSkuAttributeRows)}
              />
              <Input
                value={row.value}
                placeholder="属性值，如 银色"
                onChange={(event) => updateAttributeRow(row.rowId, "value", event.target.value, setSkuAttributeRows)}
              />
              <Button
                danger
                icon={<DeleteOutlined />}
                onClick={() => setSkuAttributeRows((rows) => rows.length > 1 ? rows.filter((item) => item.rowId !== row.rowId) : [createEmptyAttributeRow()])}
              />
            </div>
          ))}
        </Card>

        <ProFormDigit name="price" label="售价" min={0} fieldProps={{ precision: 2 }} />
        {canViewCost ? <ProFormDigit name="costPrice" label="成本价" min={0} fieldProps={{ precision: 2 }} /> : null}
        <ProFormDigit name="weight" label="重量" min={0} fieldProps={{ precision: 3 }} />
        <ProFormSelect name="status" label="状态" options={skuStatusOptions} />
      </ModalForm>
    </>
  );
}

function createRowId() {
  if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
    return crypto.randomUUID();
  }
  return `row-${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

function createEmptySkuRow(): EditableSkuRow {
  return {
    rowId: createRowId(),
    skuCode: "",
    attributes: "{}",
    barcode: "",
    price: undefined,
    costPrice: undefined,
    weight: undefined,
    status: 1
  };
}

function createEmptySpecificationRow(): EditableSpecificationRow {
  return {
    rowId: createRowId(),
    key: "",
    valuesText: ""
  };
}

function createEmptyAttributeRow(): EditableAttributeRow {
  return {
    rowId: createRowId(),
    key: "",
    value: ""
  };
}

function parseSpecifications(raw?: string | null): EditableSpecificationRow[] {
  if (!raw) {
    return [];
  }
  try {
    const parsed = JSON.parse(raw) as Record<string, unknown>;
    return Object.entries(parsed).map(([key, value]) => ({
      rowId: createRowId(),
      key,
      valuesText: Array.isArray(value) ? value.join(", ") : ""
    }));
  } catch {
    return [];
  }
}

function serializeSpecifications(rows: EditableSpecificationRow[]): string | undefined {
  const result: Record<string, string[]> = {};
  for (const row of rows) {
    const key = row.key.trim();
    if (!key) {
      continue;
    }
    const values = row.valuesText
      .split(/[,，]/)
      .map((value) => value.trim())
      .filter(Boolean);
    if (values.length > 0) {
      result[key] = values;
    }
  }
  return Object.keys(result).length > 0 ? JSON.stringify(result) : undefined;
}

function parseAttributes(raw?: string | null): EditableAttributeRow[] {
  if (!raw) {
    return [createEmptyAttributeRow()];
  }
  try {
    const parsed = JSON.parse(raw) as Record<string, unknown>;
    const rows = Object.entries(parsed).map(([key, value]) => ({
      rowId: createRowId(),
      key,
      value: value == null ? "" : String(value)
    }));
    return rows.length > 0 ? rows : [createEmptyAttributeRow()];
  } catch {
    return [createEmptyAttributeRow()];
  }
}

function serializeAttributes(rows: EditableAttributeRow[]): string | null {
  const result: Record<string, string> = {};
  for (const row of rows) {
    const key = row.key.trim();
    const value = row.value.trim();
    if (!key || !value) {
      continue;
    }
    result[key] = value;
  }
  return Object.keys(result).length > 0 ? JSON.stringify(result) : null;
}

function buildSkuRowsFromSpecifications(rows: EditableSpecificationRow[]): EditableSkuRow[] {
  const normalizedSpecs = rows
    .map((row) => ({
      key: row.key.trim(),
      values: row.valuesText
        .split(/[,，]/)
        .map((value) => value.trim())
        .filter(Boolean)
    }))
    .filter((row) => row.key && row.values.length > 0);

  if (normalizedSpecs.length === 0) {
    return [];
  }

  const combinations = buildAttributeCombinations(normalizedSpecs);
  return combinations.map((attributes) => {
    const serialized = JSON.stringify(attributes);
    return {
      rowId: createRowId(),
      skuCode: buildGeneratedSkuCode(attributes),
      attributes: serialized,
      barcode: "",
      price: undefined,
      costPrice: undefined,
      weight: undefined,
      status: 1
    };
  });
}

function buildAttributeCombinations(specs: Array<{ key: string; values: string[] }>) {
  let results: Array<Record<string, string>> = [{}];
  for (const spec of specs) {
    const nextResults: Array<Record<string, string>> = [];
    for (const result of results) {
      for (const value of spec.values) {
        nextResults.push({ ...result, [spec.key]: value });
      }
    }
    results = nextResults;
  }
  return results;
}

function buildGeneratedSkuCode(attributes: Record<string, string>) {
  const suffix = Object.values(attributes)
    .map((value) => value.replace(/\s+/g, "").slice(0, 4).toUpperCase())
    .join("-");
  return suffix ? `AUTO-${suffix}` : `AUTO-${Date.now()}`;
}

function updateSpecificationRow(
  rowId: string,
  field: keyof Omit<EditableSpecificationRow, "rowId">,
  value: string,
  setRows: React.Dispatch<React.SetStateAction<EditableSpecificationRow[]>>
) {
  setRows((rows) => rows.map((row) => (row.rowId === rowId ? { ...row, [field]: value } : row)));
}

function updateAttributeRow(
  rowId: string,
  field: keyof Omit<EditableAttributeRow, "rowId">,
  value: string,
  setRows: React.Dispatch<React.SetStateAction<EditableAttributeRow[]>>
) {
  setRows((rows) => rows.map((row) => (row.rowId === rowId ? { ...row, [field]: value } : row)));
}
