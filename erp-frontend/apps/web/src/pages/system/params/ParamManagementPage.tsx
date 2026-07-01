import { SYSTEM_PERMISSIONS } from "@erp/shared";
import { App, Button, Modal, Space, Switch, Table, Tag, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { fetchSystemParams, updateSystemParam } from "../../../api/system";
import { hasPermission } from "../../../store/auth";
import type { ParamValueType, SysParamRecord } from "../../../types/system";

const { Title, Text, Paragraph } = Typography;

const VALUE_TYPE_LABEL: Record<ParamValueType, { label: string; color: string }> = {
  BOOL: { label: "开关", color: "blue" },
  STRING: { label: "文本", color: "default" },
  INT: { label: "整数", color: "green" }
};

export function ParamManagementPage() {
  const [loading, setLoading] = useState(false);
  const [params, setParams] = useState<SysParamRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [total, setTotal] = useState(0);
  const [editing, setEditing] = useState<SysParamRecord | null>(null);
  const [draftValue, setDraftValue] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const { message } = App.useApp();
  const canUpdate = hasPermission(SYSTEM_PERMISSIONS.PARAM_UPDATE);

  useEffect(() => {
    void loadParams();
  }, []);

  async function loadParams(nextPageNum = pageNum, nextPageSize = pageSize) {
    setLoading(true);
    try {
      const data = await fetchSystemParams({ pageNum: nextPageNum, pageSize: nextPageSize });
      setParams(data.records);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
      setTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载系统参数失败");
    } finally {
      setLoading(false);
    }
  }

  async function toggleBool(record: SysParamRecord, checked: boolean) {
    try {
      const updated = await updateSystemParam(record.id, { value: String(checked) });
      setParams((prev) => prev.map((p) => (p.id === updated.id ? updated : p)));
      message.success("参数已更新");
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "更新失败");
    }
  }

  function openEdit(record: SysParamRecord) {
    setEditing(record);
    setDraftValue(record.value);
  }

  async function submitEdit() {
    if (!editing) return;
    setSubmitting(true);
    try {
      const updated = await updateSystemParam(editing.id, { value: draftValue });
      setParams((prev) => prev.map((p) => (p.id === updated.id ? updated : p)));
      message.success("参数已更新");
      setEditing(null);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "更新失败");
    } finally {
      setSubmitting(false);
    }
  }

  const columns: ColumnsType<SysParamRecord> = [
    { title: "参数编码", dataIndex: "code", key: "code", width: 280 },
    { title: "参数名称", dataIndex: "name", key: "name", width: 200 },
    {
      title: "类型", dataIndex: "valueType", key: "valueType", width: 90,
      render: (v: ParamValueType) => {
        const t = VALUE_TYPE_LABEL[v];
        return t ? <Tag color={t.color}>{t.label}</Tag> : v;
      }
    },
    {
      title: "参数值", dataIndex: "value", key: "value", width: 160,
      render: (_, record) => {
        if (record.valueType === "BOOL") {
          return (
            <Switch
              checked={record.value === "true"}
              disabled={!canUpdate || record.status === 0}
              onChange={(checked) => toggleBool(record, checked)}
            />
          );
        }
        return <Text code>{record.value}</Text>;
      }
    },
    {
      title: "说明", dataIndex: "description", key: "description",
      render: (v: string) => v ?? "-"
    },
    {
      title: "操作", key: "actions", width: 90,
      render: (_, record) =>
        record.valueType !== "BOOL" ? (
          <Button type="link" disabled={!canUpdate} onClick={() => openEdit(record)}>编辑</Button>
        ) : (
          <Text type="secondary">-</Text>
        )
    }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>系统管理 / 系统参数</Title>
          <Text type="secondary">维护影响业务流程的系统级开关与参数，修改即时生效（带缓存）。</Text>
        </div>
      </div>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={params}
        loading={loading}
        pagination={{
          current: pageNum, pageSize, total,
          showSizeChanger: true,
          showTotal: (count) => `共 ${count} 条`,
          onChange: (nextPageNum, nextPageSize) => void loadParams(nextPageNum, nextPageSize)
        }}
      />

      <Modal
        title={editing ? `编辑参数：${editing.name}` : "编辑参数"}
        open={!!editing}
        onOk={() => void submitEdit()}
        confirmLoading={submitting}
        onCancel={() => setEditing(null)}
        destroyOnClose
      >
        {editing && (
          <Space direction="vertical" size={12} style={{ width: "100%" }}>
            <Paragraph type="secondary" style={{ marginBottom: 0 }}>
              {editing.description ?? "无说明"}
            </Paragraph>
            <div>
              <Text strong>参数值{editing.valueType === "INT" ? "（整数）" : ""}</Text>
              <input
                className="ant-input"
                style={{ width: "100%", marginTop: 6 }}
                value={draftValue}
                placeholder="请输入参数值"
                onChange={(e) => setDraftValue(e.target.value)}
              />
            </div>
          </Space>
        )}
      </Modal>
    </section>
  );
}
