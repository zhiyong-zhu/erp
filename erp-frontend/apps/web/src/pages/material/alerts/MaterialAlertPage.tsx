import { AlertOutlined } from "@ant-design/icons";
import { App, Input, Table, Tag, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { fetchMaterialAlerts } from "../../../api/material";
import type { MaterialAlertRecord } from "../../../types/material";

const { Title, Text } = Typography;

export function MaterialAlertPage() {
  const [loading, setLoading] = useState(false);
  const [alerts, setAlerts] = useState<MaterialAlertRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [keyword, setKeyword] = useState("");
  const { message } = App.useApp();

  useEffect(() => {
    void loadAlerts();
  }, []);

  async function loadAlerts(nextPageNum = pageNum, nextPageSize = pageSize, name = keyword) {
    setLoading(true);
    try {
      const data = await fetchMaterialAlerts({ pageNum: nextPageNum, pageSize: nextPageSize, name });
      setAlerts(data.records);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
      setTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载安全库存预警失败");
    } finally {
      setLoading(false);
    }
  }

  const columns: ColumnsType<MaterialAlertRecord> = [
    { title: "原料编码", dataIndex: "code", key: "code", width: 140 },
    { title: "原料名称", dataIndex: "name", key: "name" },
    { title: "分类", dataIndex: "categoryName", key: "categoryName", width: 140 },
    { title: "单位", dataIndex: "unit", key: "unit", width: 90 },
    { title: "当前库存", dataIndex: "currentStock", key: "currentStock", width: 110 },
    { title: "安全库存", dataIndex: "safetyStock", key: "safetyStock", width: 110 },
    {
      title: "缺口",
      dataIndex: "shortageAmount",
      key: "shortageAmount",
      width: 110,
      render: (value?: number | null) => <Tag color="red">{value ?? 0}</Tag>
    },
    { title: "默认供应商", dataIndex: "defaultSupplierName", key: "defaultSupplierName", width: 160 },
    { title: "采购周期(天)", dataIndex: "leadTimeDays", key: "leadTimeDays", width: 120 }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>原料管理 / 安全库存预警</Title>
          <Text type="secondary">按当前库存低于或等于安全库存的规则生成原料预警列表，便于后续采购补货。</Text>
        </div>
        <Input.Search
          allowClear
          placeholder="搜索原料名称"
          value={keyword}
          onChange={(event) => setKeyword(event.target.value)}
          onSearch={(value) => {
            setKeyword(value);
            void loadAlerts(1, pageSize, value);
          }}
          prefix={<AlertOutlined />}
          style={{ width: 260 }}
        />
      </div>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={alerts}
        loading={loading}
        pagination={{
          current: pageNum,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: (count) => `共 ${count} 条`,
          onChange: (nextPageNum, nextPageSize) => void loadAlerts(nextPageNum, nextPageSize)
        }}
      />
    </section>
  );
}
