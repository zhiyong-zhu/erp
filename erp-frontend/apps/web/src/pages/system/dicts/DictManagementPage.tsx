import { PlusOutlined } from "@ant-design/icons";
import { ModalForm, ProFormDigit, ProFormSelect, ProFormText } from "@ant-design/pro-components";
import { App, Button, Card, Empty, Flex, List, Space, Switch, Table, Tag, Typography } from "antd";
import { SYSTEM_PERMISSIONS } from "@erp/shared";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import {
  createDictData,
  createDictType,
  fetchDictData,
  fetchDictTypes,
  updateDictData,
  updateDictType
} from "../../../api/system";
import { hasPermission } from "../../../store/auth";
import type { DictDataPayload, DictDataRecord, DictTypePayload, DictTypeRecord } from "../../../types/system";

const { Title, Text } = Typography;

const statusOptions = [
  { label: "启用", value: 1 },
  { label: "禁用", value: 0 }
];

export function DictManagementPage() {
  const [loading, setLoading] = useState(false);
  const [dictTypes, setDictTypes] = useState<DictTypeRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [selectedType, setSelectedType] = useState<DictTypeRecord | null>(null);
  const [dictItems, setDictItems] = useState<DictDataRecord[]>([]);
  const [itemsLoading, setItemsLoading] = useState(false);
  const [createTypeOpen, setCreateTypeOpen] = useState(false);
  const [editingType, setEditingType] = useState<DictTypeRecord | null>(null);
  const [createItemOpen, setCreateItemOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<DictDataRecord | null>(null);
  const { message } = App.useApp();
  const canCreate = hasPermission(SYSTEM_PERMISSIONS.DICT_CREATE);
  const canUpdate = hasPermission(SYSTEM_PERMISSIONS.DICT_UPDATE);

  useEffect(() => {
    void loadDictTypes();
  }, []);

  useEffect(() => {
    if (selectedType?.code) {
      void loadDictItems(selectedType.code);
    } else {
      setDictItems([]);
    }
  }, [selectedType?.code]);

  async function loadDictTypes(nextPageNum = pageNum, nextPageSize = pageSize) {
    setLoading(true);
    try {
      const data = await fetchDictTypes({ pageNum: nextPageNum, pageSize: nextPageSize });
      setDictTypes(data.records);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
      setTotal(data.total);
      if (!selectedType && data.records.length > 0) {
        setSelectedType(data.records[0]);
      } else if (selectedType) {
        const matched = data.records.find((item) => item.id === selectedType.id);
        if (matched) {
          setSelectedType(matched);
        }
      }
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载字典类型失败");
    } finally {
      setLoading(false);
    }
  }

  async function loadDictItems(code: string) {
    setItemsLoading(true);
    try {
      setDictItems(await fetchDictData(code));
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载字典项失败");
    } finally {
      setItemsLoading(false);
    }
  }

  async function handleCreateType(values: DictTypePayload) {
    try {
      const created = await createDictType(values);
      message.success("字典类型创建成功");
      setCreateTypeOpen(false);
      await loadDictTypes(1, pageSize);
      setSelectedType(created);
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "字典类型创建失败");
      return false;
    }
  }

  async function handleUpdateType(values: DictTypePayload) {
    if (!editingType) {
      return false;
    }
    try {
      const updated = await updateDictType(editingType.id, values);
      message.success("字典类型更新成功");
      setEditingType(null);
      await loadDictTypes();
      if (selectedType?.id === updated.id) {
        setSelectedType(updated);
      }
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "字典类型更新失败");
      return false;
    }
  }

  async function handleCreateItem(values: DictDataPayload) {
    if (!selectedType) {
      return false;
    }
    try {
      await createDictData(selectedType.code, values);
      message.success("字典项创建成功");
      setCreateItemOpen(false);
      await loadDictItems(selectedType.code);
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "字典项创建失败");
      return false;
    }
  }

  async function handleUpdateItem(values: DictDataPayload) {
    if (!editingItem || !selectedType) {
      return false;
    }
    try {
      await updateDictData(editingItem.id, values);
      message.success("字典项更新成功");
      setEditingItem(null);
      await loadDictItems(selectedType.code);
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "字典项更新失败");
      return false;
    }
  }

  const dictTypeColumns: ColumnsType<DictTypeRecord> = [
    { title: "字典名称", dataIndex: "name", key: "name" },
    { title: "字典编码", dataIndex: "code", key: "code" },
    { title: "描述", dataIndex: "description", key: "description" },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      render: (value: number) => <Tag color={value === 1 ? "success" : "default"}>{value === 1 ? "启用" : "禁用"}</Tag>
    },
    {
      title: "操作",
      key: "actions",
      render: (_, record) => (
        <Button type="link" disabled={!canUpdate} onClick={() => setEditingType(record)}>
          编辑
        </Button>
      )
    }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>系统管理 / 数据字典</Title>
          <Text type="secondary">维护系统枚举值和基础选项，供当前系统管理模块复用。</Text>
        </div>
        <Button type="primary" icon={<PlusOutlined />} disabled={!canCreate} onClick={() => setCreateTypeOpen(true)}>
          新建字典类型
        </Button>
      </div>

      <div className="erp-two-column-grid">
        <Card title="字典类型">
          <Table
            rowKey="id"
            columns={dictTypeColumns}
            dataSource={dictTypes}
            loading={loading}
            pagination={{
              current: pageNum,
              pageSize,
              total,
              showSizeChanger: true,
              showTotal: (count) => `共 ${count} 条`,
              onChange: (nextPageNum, nextPageSize) => void loadDictTypes(nextPageNum, nextPageSize)
            }}
            rowSelection={{
              type: "radio",
              selectedRowKeys: selectedType ? [selectedType.id] : [],
              onChange: (_, rows) => setSelectedType(rows[0] ?? null)
            }}
          />
        </Card>

        <Card
          title={selectedType ? `字典项: ${selectedType.name}` : "字典项"}
          extra={
            <Button type="primary" icon={<PlusOutlined />} disabled={!selectedType || !canCreate} onClick={() => setCreateItemOpen(true)}>
              新建字典项
            </Button>
          }
        >
          {selectedType ? (
            <List
              loading={itemsLoading}
              dataSource={dictItems}
              locale={{ emptyText: <Empty description="暂无字典项" /> }}
              renderItem={(item) => (
                <List.Item
                  actions={[
                    <Button key="edit" type="link" disabled={!canUpdate} onClick={() => setEditingItem(item)}>
                      编辑
                    </Button>
                  ]}
                >
                  <List.Item.Meta
                    title={
                      <Flex gap={8} align="center">
                        <span>{item.label}</span>
                        <Tag>{item.value}</Tag>
                        <Tag color={item.status === 1 ? "success" : "default"}>{item.status === 1 ? "启用" : "禁用"}</Tag>
                      </Flex>
                    }
                    description={`排序 ${item.sortOrder}${item.cssClass ? ` · 样式 ${item.cssClass}` : ""}`}
                  />
                </List.Item>
              )}
            />
          ) : (
            <Empty description="请选择字典类型" />
          )}
        </Card>
      </div>

      <DictTypeForm title="新建字典类型" open={createTypeOpen} onCancel={() => setCreateTypeOpen(false)} onFinish={handleCreateType} />
      <DictTypeForm
        title="编辑字典类型"
        open={!!editingType}
        initialValues={editingType ? {
          name: editingType.name,
          code: editingType.code,
          description: editingType.description ?? "",
          status: editingType.status
        } : undefined}
        onCancel={() => setEditingType(null)}
        onFinish={handleUpdateType}
      />

      <DictDataForm title="新建字典项" open={createItemOpen} onCancel={() => setCreateItemOpen(false)} onFinish={handleCreateItem} />
      <DictDataForm
        title="编辑字典项"
        open={!!editingItem}
        initialValues={editingItem ? {
          label: editingItem.label,
          value: editingItem.value,
          sortOrder: editingItem.sortOrder,
          cssClass: editingItem.cssClass ?? "",
          status: editingItem.status
        } : undefined}
        onCancel={() => setEditingItem(null)}
        onFinish={handleUpdateItem}
      />
    </section>
  );
}

function DictTypeForm({ title, open, initialValues, onCancel, onFinish }: {
  title: string;
  open: boolean;
  initialValues?: Partial<DictTypePayload>;
  onCancel: () => void;
  onFinish: (values: DictTypePayload) => Promise<boolean>;
}) {
  return (
    <ModalForm<DictTypePayload> title={title} open={open} initialValues={initialValues ?? { status: 1 }} modalProps={{ destroyOnClose: true, onCancel }} onFinish={onFinish}>
      <ProFormText name="name" label="字典名称" rules={[{ required: true }]} />
      <ProFormText name="code" label="字典编码" rules={[{ required: true }]} />
      <ProFormText name="description" label="描述" />
      <ProFormSelect name="status" label="状态" options={statusOptions} rules={[{ required: true }]} />
    </ModalForm>
  );
}

function DictDataForm({ title, open, initialValues, onCancel, onFinish }: {
  title: string;
  open: boolean;
  initialValues?: Partial<DictDataPayload>;
  onCancel: () => void;
  onFinish: (values: DictDataPayload) => Promise<boolean>;
}) {
  return (
    <ModalForm<DictDataPayload> title={title} open={open} initialValues={initialValues ?? { sortOrder: 0, status: 1 }} modalProps={{ destroyOnClose: true, onCancel }} onFinish={onFinish}>
      <ProFormText name="label" label="字典标签" rules={[{ required: true }]} />
      <ProFormText name="value" label="字典值" rules={[{ required: true }]} />
      <ProFormDigit name="sortOrder" label="排序" min={0} fieldProps={{ precision: 0 }} />
      <ProFormText name="cssClass" label="样式标识" />
      <ProFormSelect name="status" label="状态" options={statusOptions} rules={[{ required: true }]} />
    </ModalForm>
  );
}
