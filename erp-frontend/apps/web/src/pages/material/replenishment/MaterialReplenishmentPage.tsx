import { BulbOutlined } from "@ant-design/icons";
import { App, Button, Input, Modal, Space, Table, Tag, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { fetchMaterialReplenishmentSuggestions } from "../../../api/material";
import { generatePurchaseDraftsFromReplenishment } from "../../../api/purchase";
import { PURCHASE_PERMISSIONS } from "@erp/shared";
import { hasPermission } from "../../../store/auth";
import type { MaterialReplenishmentRecord } from "../../../types/material";

const { Title, Text } = Typography;

export function MaterialReplenishmentPage() {
  const [loading, setLoading] = useState(false);
  const [records, setRecords] = useState<MaterialReplenishmentRecord[]>([]);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [keyword, setKeyword] = useState("");
  const { message } = App.useApp();
  const canCreatePurchase = hasPermission(PURCHASE_PERMISSIONS.ORDER_CREATE);

  useEffect(() => {
    void loadData();
  }, []);

  async function loadData(nextPageNum = pageNum, nextPageSize = pageSize, name = keyword) {
    setLoading(true);
    try {
      const data = await fetchMaterialReplenishmentSuggestions({ pageNum: nextPageNum, pageSize: nextPageSize, name });
      setRecords(data.records);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
      setTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载补货建议失败");
    } finally {
      setLoading(false);
    }
  }

  async function handleGeneratePurchaseDrafts() {
    const materialIds = selectedRowKeys.map(String);
    if (materialIds.length === 0) {
      message.warning("请先选择补货建议");
      return;
    }

    Modal.confirm({
      title: "生成采购草稿",
      content: `将基于已选 ${materialIds.length} 条补货建议生成采购单草稿，是否继续？`,
      onOk: async () => {
        try {
          const result = await generatePurchaseDraftsFromReplenishment({ materialIds });
          message.success(`已生成 ${result.records.length} 张采购草稿`);
          setSelectedRowKeys([]);
        } catch (err: any) {
          message.error(err?.response?.data?.message ?? err?.message ?? "生成采购草稿失败");
        }
      }
    });
  }

  const columns: ColumnsType<MaterialReplenishmentRecord> = [
    { title: "原料编码", dataIndex: "materialCode", key: "materialCode", width: 140 },
    { title: "原料名称", dataIndex: "materialName", key: "materialName", width: 180 },
    { title: "当前库存", dataIndex: "currentStock", key: "currentStock", width: 110 },
    { title: "安全库存", dataIndex: "safetyStock", key: "safetyStock", width: 110 },
    {
      title: "缺口量",
      dataIndex: "shortageAmount",
      key: "shortageAmount",
      width: 110,
      render: (value?: number | null) => <Tag color="red">{value ?? 0}</Tag>
    },
    {
      title: "建议采购量",
      dataIndex: "suggestedQuantity",
      key: "suggestedQuantity",
      width: 130,
      render: (value: number | null | undefined, record: MaterialReplenishmentRecord) => `${value ?? 0} ${record.unit}`
    },
    { title: "建议供应商", dataIndex: "supplierName", key: "supplierName", width: 180 },
    { title: "参考报价", dataIndex: "quotePrice", key: "quotePrice", width: 120 },
    { title: "币种", dataIndex: "currency", key: "currency", width: 90 },
    { title: "预估金额", dataIndex: "estimatedAmount", key: "estimatedAmount", width: 120 },
    { title: "交期(天)", dataIndex: "leadTimeDays", key: "leadTimeDays", width: 100 },
    { title: "建议说明", dataIndex: "recommendationReason", key: "recommendationReason", width: 260 }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>原料管理 / 补货建议</Title>
          <Text type="secondary">结合当前库存、安全库存、默认供应商和有效报价，生成建议采购量和参考金额。</Text>
        </div>
        <Space>
          <Input.Search
            allowClear
            placeholder="搜索原料名称"
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
            onSearch={(value) => {
              setKeyword(value);
              void loadData(1, pageSize, value);
            }}
            prefix={<BulbOutlined />}
            style={{ width: 260 }}
          />
          <Button type="primary" disabled={!canCreatePurchase} onClick={() => void handleGeneratePurchaseDrafts()}>
            生成采购草稿
          </Button>
        </Space>
      </div>

      <Table
        rowKey="materialId"
        columns={columns}
        dataSource={records}
        loading={loading}
        rowSelection={{
          selectedRowKeys,
          onChange: (keys) => setSelectedRowKeys(keys)
        }}
        scroll={{ x: 1500 }}
        pagination={{
          current: pageNum,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: (count) => `共 ${count} 条`,
          onChange: (nextPageNum, nextPageSize) => void loadData(nextPageNum, nextPageSize)
        }}
      />
    </section>
  );
}
