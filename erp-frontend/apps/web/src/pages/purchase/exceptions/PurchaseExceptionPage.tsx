import { App, Button, Table, Tag, Typography } from "antd";
import { PURCHASE_PERMISSIONS } from "@erp/shared";
import { CreateForm } from "../../../components/CreateForm";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { fetchPurchaseExceptions, handlePurchaseException } from "../../../api/purchase";
import { hasPermission } from "../../../store/auth";
import type { PurchaseExceptionHandlePayload, PurchaseExceptionRecord } from "../../../types/purchase";

const { Title, Text } = Typography;

export function PurchaseExceptionPage() {
  const [loading, setLoading] = useState(false);
  const [records, setRecords] = useState<PurchaseExceptionRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [handlingRecord, setHandlingRecord] = useState<PurchaseExceptionRecord | null>(null);
  const { message } = App.useApp();
  const canUpdate = hasPermission(PURCHASE_PERMISSIONS.EXCEPTION_UPDATE);

  useEffect(() => {
    void loadData();
  }, []);

  async function loadData(nextPageNum = pageNum, nextPageSize = pageSize) {
    setLoading(true);
    try {
      const data = await fetchPurchaseExceptions({ pageNum: nextPageNum, pageSize: nextPageSize });
      setRecords(data.records);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
      setTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载采购异常失败");
    } finally {
      setLoading(false);
    }
  }

  async function handleSubmit(values: PurchaseExceptionHandlePayload) {
    if (!handlingRecord) {
      return false;
    }
    try {
      await handlePurchaseException(handlingRecord.id, values);
      message.success("采购异常处理成功");
      setHandlingRecord(null);
      await loadData();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "采购异常处理失败");
      return false;
    }
  }

  const columns: ColumnsType<PurchaseExceptionRecord> = [
    { title: "异常编号", dataIndex: "exceptionNo", key: "exceptionNo", width: 180 },
    { title: "供应商", dataIndex: "supplierName", key: "supplierName", width: 180 },
    { title: "原料编码", dataIndex: "materialCode", key: "materialCode", width: 140 },
    { title: "原料名称", dataIndex: "materialName", key: "materialName", width: 180 },
    { title: "异常类型", dataIndex: "exceptionType", key: "exceptionType", width: 120 },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 120,
      render: (value: string) => <Tag>{value}</Tag>
    },
    { title: "异常描述", dataIndex: "description", key: "description", width: 260 },
    { title: "处理结果", dataIndex: "resolution", key: "resolution", width: 260 },
    {
      title: "操作",
      key: "actions",
      width: 120,
      render: (_, record) => (
        <Button type="link" disabled={!canUpdate || record.status !== "OPEN"} onClick={() => setHandlingRecord(record)}>
          处理
        </Button>
      )
    }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>采购管理 / 采购异常</Title>
          <Text type="secondary">集中处理到货验收中的不合格、差异等采购异常。</Text>
        </div>
      </div>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={records}
        loading={loading}
        pagination={{
          current: pageNum,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: (count) => `共 ${count} 条`,
          onChange: (nextPageNum, nextPageSize) => void loadData(nextPageNum, nextPageSize)
        }}
      />

      <CreateForm
        title="处理采购异常"
        open={!!handlingRecord}
        width={720}
        initialValues={{ action: "resolve", resolution: handlingRecord?.resolution ?? "" }}
        onCancel={() => setHandlingRecord(null)}
        onFinish={handleSubmit}
        sections={[
          {
            title: "处理说明",
            fields: [
              { type: "textarea", name: "resolution", label: "处理说明", autoSize: { minRows: 3, maxRows: 5 }, rules: [{ required: true, message: "请输入处理说明" }], colSpan: 24 }
            ]
          }
        ]}
      />
    </section>
  );
}
