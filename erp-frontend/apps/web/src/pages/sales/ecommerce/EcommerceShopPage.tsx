import { PlusOutlined, SyncOutlined } from "@ant-design/icons";
import { ModalForm, ProFormSelect, ProFormText } from "@ant-design/pro-components";
import { SALES_PERMISSIONS } from "@erp/shared";
import { App, Button, Space, Table, Tag, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { createEcommerceShop, fetchEcommerceShops, syncEcommerceOrders } from "../../../api/sales";
import { hasPermission } from "../../../store/auth";
import type { EcommerceShopRecord } from "../../../types/sales";

const { Title, Text } = Typography;

const PLATFORM_MAP: Record<string, string> = {
  ALIBABA_1688: "1688",
  TAOBAO: "淘宝/天猫",
  JD: "京东",
  PDD: "拼多多",
  DOUYIN: "抖音"
};

export function EcommerceShopPage() {
  const [loading, setLoading] = useState(false);
  const [records, setRecords] = useState<EcommerceShopRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [createOpen, setCreateOpen] = useState(false);
  const [syncingId, setSyncingId] = useState<string | null>(null);
  const { message } = App.useApp();
  const canCreate = hasPermission(SALES_PERMISSIONS.ORDER_CREATE);

  useEffect(() => {
    void loadData();
  }, []);

  async function loadData(nextPageNum = pageNum, nextPageSize = pageSize) {
    setLoading(true);
    try {
      const data = await fetchEcommerceShops({ pageNum: nextPageNum, pageSize: nextPageSize });
      setRecords(data.records);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
      setTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载店铺失败");
    } finally {
      setLoading(false);
    }
  }

  async function handleSync(shopId: string) {
    setSyncingId(shopId);
    try {
      const result = await syncEcommerceOrders(shopId);
      message.success(`同步成功，新增 ${result.syncedCount} 笔订单`);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "同步失败");
    } finally {
      setSyncingId(null);
    }
  }

  async function handleCreate(values: any) {
    try {
      await createEcommerceShop(values);
      message.success("店铺添加成功");
      setCreateOpen(false);
      await loadData(1, pageSize);
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "添加失败");
      return false;
    }
  }

  const columns: ColumnsType<EcommerceShopRecord> = [
    {
      title: "平台", dataIndex: "platform", key: "platform", width: 100,
      render: (v: string) => <Tag color="blue">{PLATFORM_MAP[v] ?? v}</Tag>
    },
    { title: "店铺名称", dataIndex: "shopName", key: "shopName", width: 200 },
    { title: "平台店铺ID", dataIndex: "shopIdOnPlatform", key: "shopIdOnPlatform", width: 180 },
    {
      title: "状态", dataIndex: "status", key: "status", width: 80,
      render: (v: number) => v === 1 ? <Tag color="green">启用</Tag> : <Tag color="red">禁用</Tag>
    },
    {
      title: "操作", key: "actions", width: 160,
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            icon={<SyncOutlined spin={syncingId === record.id} />}
            disabled={record.status !== 1 || syncingId !== null}
            onClick={() => void handleSync(record.id)}
          >
            {syncingId === record.id ? "同步中..." : "同步订单"}
          </Button>
        </Space>
      )
    }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>销售管理 / 电商平台</Title>
          <Text type="secondary">管理电商平台店铺，同步平台订单到销售订单。</Text>
        </div>
        <Button type="primary" icon={<PlusOutlined />} disabled={!canCreate} onClick={() => setCreateOpen(true)}>
          添加店铺
        </Button>
      </div>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={records}
        loading={loading}
        pagination={{
          current: pageNum, pageSize, total,
          showSizeChanger: true,
          showTotal: (count) => `共 ${count} 条`,
          onChange: (nextPageNum, nextPageSize) => void loadData(nextPageNum, nextPageSize)
        }}
      />

      <ModalForm
        title="添加电商平台店铺"
        open={createOpen}
        width={600}
        modalProps={{ destroyOnClose: true, onCancel: () => setCreateOpen(false) }}
        onFinish={handleCreate}
      >
        <ProFormSelect
          name="platform" label="平台" rules={[{ required: true }]}
          options={[
            { label: "1688 (阿里巴巴)", value: "ALIBABA_1688" },
            { label: "淘宝/天猫", value: "TAOBAO" },
            { label: "京东", value: "JD" },
            { label: "拼多多", value: "PDD" },
            { label: "抖音", value: "DOUYIN" }
          ]}
        />
        <ProFormText name="shopName" label="店铺名称" rules={[{ required: true, message: "请输入店铺名称" }]} />
        <ProFormText name="shopIdOnPlatform" label="平台店铺ID" rules={[{ required: true, message: "请输入平台店铺ID" }]} />
        <ProFormText name="accessToken" label="Access Token" />
      </ModalForm>
    </section>
  );
}
