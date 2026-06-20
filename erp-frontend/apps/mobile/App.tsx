import React, { useState } from "react";
import {
  ActivityIndicator,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View
} from "react-native";
import { StatusBar } from "expo-status-bar";
import { mobileShellTitle } from "@erp/ui-mobile";
import { fetchUserInfo, login, logout } from "./src/api/auth";
import { saveAccessToken, saveUser } from "./src/store/auth";
import type { UserInfo } from "./src/types/auth";

type OperationKey = "production" | "receipt" | "issue" | "check" | "boxPrint" | "documentPrint";

const operationCards: Array<{ key: OperationKey; title: string; description: string; tag: string }> = [
  { key: "production", title: "生产执行", description: "扫描工单并提交生产报工。", tag: "车间" },
  { key: "receipt", title: "扫码入库", description: "扫描物料码、箱码或入库单。", tag: "入库" },
  { key: "issue", title: "扫码出库", description: "扫描领料单、出库单或箱码。", tag: "出库" },
  { key: "check", title: "仓库盘点", description: "按库位扫码盘点并提交差异。", tag: "盘点" },
  { key: "boxPrint", title: "箱码打印", description: "按工单生成箱码并打印标签。", tag: "打印" },
  { key: "documentPrint", title: "单据打印", description: "打印入库单、出库单、盘点单。", tag: "单据" }
];

export default function App() {
  const [username, setUsername] = useState("admin");
  const [password, setPassword] = useState("password");
  const [user, setUser] = useState<UserInfo | null>(null);
  const [activeOperation, setActiveOperation] = useState<OperationKey>("production");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleLogin() {
    setLoading(true);
    setError(null);
    try {
      const result = await login({ username, password });
      saveAccessToken(result.accessToken);
      const info = await fetchUserInfo();
      saveUser(info);
      setUser(info);
    } catch (err) {
      setError(err instanceof Error ? err.message : "登录失败，请检查用户名和密码");
    } finally {
      setLoading(false);
    }
  }

  async function handleLogout() {
    setLoading(true);
    try {
      await logout();
    } finally {
      saveAccessToken(null);
      saveUser(null);
      setUser(null);
      setLoading(false);
    }
  }

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar style="dark" />
      <KeyboardAvoidingView behavior={Platform.OS === "ios" ? "padding" : undefined} style={styles.keyboardView}>
        <ScrollView contentContainerStyle={styles.scrollContent} keyboardShouldPersistTaps="handled">
          {user ? (
            <View style={[styles.panel, styles.dashboardPanel]}>
              <Text style={styles.badge}>ERP Mobile</Text>
              <Text style={styles.title}>移动作业台</Text>
              <Text style={styles.subtitle}>欢迎回来，{user.realName || user.username}。移动端只保留扫码、盘点、打印等现场操作入口。</Text>
              <View style={styles.operationGrid}>
                {operationCards.map((operation) => (
                  <Pressable
                    key={operation.key}
                    onPress={() => setActiveOperation(operation.key)}
                    style={[styles.operationCard, activeOperation === operation.key && styles.activeOperationCard]}
                  >
                    <Text style={[styles.operationTag, activeOperation === operation.key && styles.activeOperationText]}>{operation.tag}</Text>
                    <Text style={[styles.operationTitle, activeOperation === operation.key && styles.activeOperationText]}>{operation.title}</Text>
                    <Text style={[styles.operationDescription, activeOperation === operation.key && styles.activeOperationText]}>{operation.description}</Text>
                  </Pressable>
                ))}
              </View>
              <OperationDetail operation={operationCards.find((operation) => operation.key === activeOperation) ?? operationCards[0]} />
              <Pressable disabled={loading} onPress={() => void handleLogout()} style={[styles.secondaryButton, loading && styles.disabledButton]}><Text style={styles.secondaryButtonText}>{loading ? "退出中..." : "退出登录"}</Text></Pressable>
            </View>
          ) : (
            <View style={styles.panel}>
              <Text style={styles.badge}>{mobileShellTitle()}</Text>
              <Text style={styles.title}>全渠道 ERP 移动端登录</Text>
              <Text style={styles.subtitle}>连接后端服务，使用管理员账号进入移动作业台。</Text>
              {error ? <Text style={styles.error}>{error}</Text> : null}
              <View style={styles.form}>
                <View style={styles.field}><Text style={styles.label}>用户名</Text><TextInput autoCapitalize="none" autoCorrect={false} editable={!loading} onChangeText={setUsername} placeholder="请输入用户名" style={styles.input} value={username} /></View>
                <View style={styles.field}><Text style={styles.label}>密码</Text><TextInput editable={!loading} onChangeText={setPassword} placeholder="请输入密码" secureTextEntry style={styles.input} value={password} /></View>
                <Pressable disabled={loading} onPress={() => void handleLogin()} style={[styles.primaryButton, loading && styles.disabledButton]}>{loading ? <ActivityIndicator color="#ffffff" /> : <Text style={styles.primaryButtonText}>登录</Text>}</Pressable>
              </View>
            </View>
          )}
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

function OperationDetail({ operation }: { operation: (typeof operationCards)[number] }) {
  return (
    <View style={styles.detailCard}>
      <Text style={styles.detailTag}>{operation.tag}</Text>
      <Text style={styles.sectionTitle}>{operation.title}</Text>
      <Text style={styles.detailDescription}>{operation.description}</Text>
      {renderOperationSteps(operation.key).map((step) => <Text key={step} style={styles.stepItem}>{step}</Text>)}
      <Pressable style={styles.primaryButton}><Text style={styles.primaryButtonText}>进入{operation.title}</Text></Pressable>
    </View>
  );
}

function renderOperationSteps(operationKey: OperationKey) {
  const stepMap: Record<OperationKey, string[]> = {
    production: ["扫描/选择工单", "录入良品与不良数量", "提交生产执行记录"],
    receipt: ["扫描入库单或箱码", "校验物料与数量", "确认扫码入库"],
    issue: ["扫描出库单或领料单", "校验批次与库存", "确认扫码出库"],
    check: ["选择仓库/库位", "扫码录入实盘数量", "提交盘点差异"],
    boxPrint: ["选择工单与箱数", "生成箱码", "连接打印机输出标签"],
    documentPrint: ["选择单据类型", "预览打印内容", "连接打印机输出单据"]
  };
  return stepMap[operationKey];
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: "#eef6fb" },
  keyboardView: { flex: 1 },
  scrollContent: { flexGrow: 1, justifyContent: "center", padding: 20 },
  panel: { padding: 24, borderRadius: 28, backgroundColor: "#ffffff", shadowColor: "#0f172a", shadowOffset: { width: 0, height: 18 }, shadowOpacity: 0.12, shadowRadius: 32, elevation: 8 },
  dashboardPanel: { gap: 18 },
  badge: { alignSelf: "flex-start", paddingHorizontal: 12, paddingVertical: 6, borderRadius: 999, backgroundColor: "#0f766e", color: "#ffffff", overflow: "hidden", fontSize: 12, fontWeight: "700", letterSpacing: 1 },
  title: { marginTop: 18, fontSize: 28, fontWeight: "700", color: "#10263d" },
  subtitle: { marginTop: 8, fontSize: 16, lineHeight: 24, color: "#5b7089" },
  form: { marginTop: 24, gap: 16 },
  field: { gap: 8 },
  label: { fontSize: 14, fontWeight: "700", color: "#334155" },
  input: { height: 48, borderWidth: 1, borderColor: "#cbd5e1", borderRadius: 14, paddingHorizontal: 14, backgroundColor: "#ffffff", color: "#0f172a" },
  primaryButton: { height: 48, alignItems: "center", justifyContent: "center", borderRadius: 14, backgroundColor: "#0f766e" },
  primaryButtonText: { color: "#ffffff", fontSize: 16, fontWeight: "700" },
  secondaryButton: { height: 46, alignItems: "center", justifyContent: "center", borderRadius: 14, backgroundColor: "#e2e8f0" },
  secondaryButtonText: { color: "#0f172a", fontSize: 15, fontWeight: "700" },
  disabledButton: { opacity: 0.7 },
  error: { marginTop: 10, padding: 12, borderRadius: 14, overflow: "hidden", backgroundColor: "#fef2f2", color: "#b91c1c" },
  operationGrid: { flexDirection: "row", flexWrap: "wrap", gap: 10 },
  operationCard: { width: "48%", minHeight: 136, gap: 8, padding: 14, borderRadius: 18, backgroundColor: "#f8fafc", borderWidth: 1, borderColor: "#e2e8f0" },
  activeOperationCard: { backgroundColor: "#0f766e", borderColor: "#0f766e" },
  operationTag: { color: "#0f766e", fontSize: 12, fontWeight: "800" },
  operationTitle: { color: "#10263d", fontSize: 17, fontWeight: "800" },
  operationDescription: { color: "#64748b", fontSize: 13, lineHeight: 19 },
  activeOperationText: { color: "#ffffff" },
  detailCard: { gap: 12, padding: 18, borderRadius: 20, backgroundColor: "#ecfeff" },
  detailTag: { color: "#0f766e", fontSize: 12, fontWeight: "800" },
  sectionTitle: { fontSize: 20, fontWeight: "800", color: "#10263d" },
  detailDescription: { color: "#475569", fontSize: 15, lineHeight: 22 },
  stepItem: { padding: 12, borderRadius: 14, overflow: "hidden", backgroundColor: "#ffffff", color: "#334155", fontWeight: "700" }
});
