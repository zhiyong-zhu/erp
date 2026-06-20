import { ModalForm, ProFormTextArea } from "@ant-design/pro-components";
import { App, Button, Table, Tag, Typography } from "antd";
import { SALES_PERMISSIONS } from "@erp/shared";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { fetchSaleExceptions, handleSaleException } from "../../../api/sales";
import { hasPermission } from "../../../store/auth";
import type { SaleExceptionHandlePayload, SaleExceptionRecord } from "../../../types/sales";

const { Title, Text } = Typography;

export function SaleExceptionPage() {
  const [loading, setLoading] = useState(false);
  const [records, setRecords] = useState<SaleExceptionRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [handlingRecord, setHandlingRecord] = useState<SaleExceptionRecord | null>(null);
  const { message } = App.useApp();
  const canUpdate = hasPermission(SALES_PERMISSIONS.EXCEPTION_UPDATE);

  useEffect(() => {
    void loadData();
  }, []);

  async function loadData(nextPageNum = pageNum, nextPageSize = pageSize) {
    setLoading(true);
    try {
      const data = await fetchSaleExceptions({ pageNum: nextPageNum, pageSize: nextPageSize });
      setRecords(data.records);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
      setTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载销售异常失败");
    } finally {
      setLoading(false);
    }
  }

  async function handleSubmit(values: SaleExceptionHandlePayload) {
    if (!handlingRecord) {
      return false;
    }
    try {
      await handleSaleException(handlingRecord.id, values);
      message.success("销售异常处理成功");
      setHandlingRecord(null);
      await loadData();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "销售异常处理失败");
      return false;
    }
  }

  const columns: ColumnsType<SaleExceptionRecord> = [
    { title: "异常编号", dataIndex: "exceptionNo", key: "exceptionNo", width: 210 },
    { title: "客户", dataIndex: "customerName", key: "customerName", width: 180 },
    { title: "SKU", dataIndex: "skuCode", key: "skuCode", width: 140 },
    { title: "产品名称", dataIndex: "productName", key: "productName", width: 180 },
    { title: "异常类型", dataIndex: "exceptionType", key: "exceptionType", width: 130 },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 120,
      render: (value: string) => <Tag color={value === "OPEN" ? "orange" : "default"}>{value}</Tag>
    },
    { title: "异常描述", dataIndex: "description", key: "description", width: 280 },
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
          <Title level={3} style={{ margin: 0 }}>销售管理 / 销售异常</Title>
          <Text type="secondary">集中跟踪发货、退货检验和退货驳回等销售异常。</Text>
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

      <ModalForm<SaleExceptionHandlePayload>
        title="处理销售异常"
        open={!!handlingRecord}
        width={720}
        initialValues={{ action: "resolve", resolution: handlingRecord?.resolution ?? "" }}
        modalProps={{ destroyOnClose: true, onCancel: () => setHandlingRecord(null) }}
        onFinish={handleSubmit}
      >
        <ProFormTextArea
          name="resolution"
          label="处理说明"
          fieldProps={{ autoSize: { minRows: 3, maxRows: 5 } }}
          rules={[{ required: true, message: "请输入处理说明" }]}
        />
      </ModalForm>
    </section>
  );
}
