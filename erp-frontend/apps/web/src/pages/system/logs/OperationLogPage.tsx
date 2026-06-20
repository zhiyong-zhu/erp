import { ReloadOutlined, SearchOutlined } from "@ant-design/icons";
import { ProFormSelect, ProFormText, QueryFilter } from "@ant-design/pro-components";
import { App, Button, Card, Descriptions, Drawer, Space, Table, Tag, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { fetchOperationLogs } from "../../../api/system";
import type { OperationLogRecord } from "../../../types/system";

const { Title, Text, Paragraph } = Typography;

interface LogQuery {
  module?: string;
  action?: string;
  username?: string;
}

function formatDateTime(value?: string) {
  if (!value) {
    return "-";
  }
  return new Date(value).toLocaleString();
}

function optionalText(value?: string | number | null) {
  return value === undefined || value === null || value === "" ? "-" : value;
}

export function OperationLogPage() {
  const { message } = App.useApp();
  const [loading, setLoading] = useState(false);
  const [logs, setLogs] = useState<OperationLogRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [query, setQuery] = useState<LogQuery>({});
  const [selectedLog, setSelectedLog] = useState<OperationLogRecord | null>(null);

  useEffect(() => {
    void loadLogs();
  }, []);

  async function loadLogs(nextPageNum = pageNum, nextPageSize = pageSize, nextQuery = query) {
    setLoading(true);
    try {
      const data = await fetchOperationLogs({ pageNum: nextPageNum, pageSize: nextPageSize, ...nextQuery });
      setLogs(data.records);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
      setTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载操作日志失败");
    } finally {
      setLoading(false);
    }
  }

  const columns: ColumnsType<OperationLogRecord> = [
    {
      title: "时间",
      dataIndex: "createdAt",
      key: "createdAt",
      width: 180,
      render: (value?: string) => formatDateTime(value)
    },
    {
      title: "用户",
      dataIndex: "username",
      key: "username",
      width: 140,
      render: (value?: string | null) => optionalText(value)
    },
    { title: "模块", dataIndex: "module", key: "module", width: 140 },
    { title: "动作", dataIndex: "action", key: "action", width: 140 },
    {
      title: "描述",
      dataIndex: "description",
      key: "description",
      ellipsis: true,
      render: (value?: string | null) => optionalText(value)
    },
    {
      title: "结果",
      dataIndex: "success",
      key: "success",
      width: 100,
      render: (value?: boolean | null) => (
        <Tag color={value === false ? "error" : "success"}>{value === false ? "失败" : "成功"}</Tag>
      )
    },
    {
      title: "耗时",
      dataIndex: "duration",
      key: "duration",
      width: 100,
      render: (value?: number | null) => (value === undefined || value === null ? "-" : `${value}ms`)
    },
    {
      title: "IP",
      dataIndex: "ip",
      key: "ip",
      width: 140,
      render: (value?: string | null) => optionalText(value)
    },
    {
      title: "操作",
      key: "actions",
      width: 90,
      render: (_, record) => (
        <Button type="link" onClick={() => setSelectedLog(record)}>
          详情
        </Button>
      )
    }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>系统管理 / 操作日志</Title>
          <Text type="secondary">查看后台关键操作、请求信息与异常追踪。</Text>
        </div>
        <Button icon={<ReloadOutlined />} onClick={() => void loadLogs()} loading={loading}>
          刷新
        </Button>
      </div>

      <Card>
        <QueryFilter
          defaultCollapsed={false}
          onFinish={async (values) => {
            const nextQuery = values as LogQuery;
            setQuery(nextQuery);
            await loadLogs(1, pageSize, nextQuery);
          }}
          onReset={() => {
            setQuery({});
            void loadLogs(1, pageSize, {});
          }}
          submitter={{
            searchConfig: {
              submitText: "查询",
              resetText: "重置"
            },
            submitButtonProps: {
              icon: <SearchOutlined />
            }
          }}
        >
          <ProFormText name="username" label="用户" placeholder="用户名" />
          <ProFormText name="module" label="模块" placeholder="如 system/product" />
          <ProFormSelect
            name="action"
            label="动作"
            placeholder="请选择动作"
            allowClear
            options={[
              { label: "创建", value: "CREATE" },
              { label: "更新", value: "UPDATE" },
              { label: "删除", value: "DELETE" },
              { label: "查询", value: "QUERY" },
              { label: "登录", value: "LOGIN" },
              { label: "退出", value: "LOGOUT" }
            ]}
          />
        </QueryFilter>

        <Table
          rowKey="id"
          columns={columns}
          dataSource={logs}
          loading={loading}
          pagination={{
            current: pageNum,
            pageSize,
            total,
            showSizeChanger: true,
            showTotal: (count) => `共 ${count} 条`,
            onChange: (nextPageNum, nextPageSize) => void loadLogs(nextPageNum, nextPageSize)
          }}
        />
      </Card>

      <Drawer
        title="操作日志详情"
        width={720}
        open={Boolean(selectedLog)}
        onClose={() => setSelectedLog(null)}
      >
        {selectedLog ? (
          <Space direction="vertical" size={16} style={{ width: "100%" }}>
            <Descriptions bordered column={2} size="small">
              <Descriptions.Item label="日志 ID">{selectedLog.id}</Descriptions.Item>
              <Descriptions.Item label="时间">{formatDateTime(selectedLog.createdAt)}</Descriptions.Item>
              <Descriptions.Item label="用户">{optionalText(selectedLog.username)}</Descriptions.Item>
              <Descriptions.Item label="用户 ID">{optionalText(selectedLog.userId)}</Descriptions.Item>
              <Descriptions.Item label="模块">{selectedLog.module}</Descriptions.Item>
              <Descriptions.Item label="动作">{selectedLog.action}</Descriptions.Item>
              <Descriptions.Item label="方法">{optionalText(selectedLog.method)}</Descriptions.Item>
              <Descriptions.Item label="响应码">{optionalText(selectedLog.responseCode)}</Descriptions.Item>
              <Descriptions.Item label="IP">{optionalText(selectedLog.ip)}</Descriptions.Item>
              <Descriptions.Item label="耗时">{selectedLog.duration === undefined || selectedLog.duration === null ? "-" : `${selectedLog.duration}ms`}</Descriptions.Item>
              <Descriptions.Item label="Trace ID" span={2}>{optionalText(selectedLog.traceId)}</Descriptions.Item>
              <Descriptions.Item label="请求地址" span={2}>{optionalText(selectedLog.requestUrl)}</Descriptions.Item>
              <Descriptions.Item label="数据范围" span={2}>{optionalText(selectedLog.dataScopeLevel)}</Descriptions.Item>
              <Descriptions.Item label="审计标签" span={2}>{optionalText(selectedLog.auditTags)}</Descriptions.Item>
            </Descriptions>

            <Card size="small" title="描述">
              <Paragraph style={{ marginBottom: 0 }}>{optionalText(selectedLog.description)}</Paragraph>
            </Card>

            <Card size="small" title="请求参数">
              <Paragraph code copyable style={{ whiteSpace: "pre-wrap", marginBottom: 0 }}>
                {optionalText(selectedLog.requestParams)}
              </Paragraph>
            </Card>

            {selectedLog.errorMessage ? (
              <Card size="small" title="错误信息">
                <Paragraph type="danger" copyable style={{ whiteSpace: "pre-wrap", marginBottom: 0 }}>
                  {selectedLog.errorMessage}
                </Paragraph>
              </Card>
            ) : null}
          </Space>
        ) : null}
      </Drawer>
    </section>
  );
}
