import { LockOutlined, UserOutlined } from "@ant-design/icons";
import { Alert, App, Button, Card, Form, Input, Typography } from "antd";
import { login, fetchUserInfo } from "../../api/auth";
import { saveAccessToken, saveUser } from "../../store/auth";
import type { LoginRequest } from "../../types/auth";
import { useState } from "react";
import { useNavigate } from "react-router-dom";

const { Title, Text } = Typography;

export function LoginPage() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { message } = App.useApp();
  const navigate = useNavigate();

  async function onFinish(values: LoginRequest) {
    setLoading(true);
    setError(null);
    try {
      const result = await login(values);
      saveAccessToken(result.accessToken);
      const user = await fetchUserInfo();
      saveUser(user);
      message.success("登录成功");
      navigate("/system/users", { replace: true });
    } catch (err: any) {
      setError(err?.response?.data?.message ?? "登录失败，请检查用户名和密码");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-shell">
      <Card className="login-card" bordered={false}>
        <Text className="login-badge">ERP Console</Text>
        <Title level={2} style={{ marginTop: 16 }}>全渠道 ERP 登录</Title>
        <Text type="secondary">使用系统管理员账户登录，进入系统管理与用户管理页面。</Text>
        {error ? <Alert style={{ marginTop: 20 }} type="error" message={error} showIcon /> : null}
        <Form layout="vertical" onFinish={onFinish} style={{ marginTop: 24 }} initialValues={{ username: "admin", password: "password" }}>
          <Form.Item label="用户名" name="username" rules={[{ required: true, message: "请输入用户名" }]}>
            <Input prefix={<UserOutlined />} placeholder="请输入用户名" />
          </Form.Item>
          <Form.Item label="密码" name="password" rules={[{ required: true, message: "请输入密码" }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="请输入密码" />
          </Form.Item>
          <Button type="primary" htmlType="submit" block size="large" loading={loading}>
            登录
          </Button>
        </Form>
      </Card>
    </div>
  );
}
