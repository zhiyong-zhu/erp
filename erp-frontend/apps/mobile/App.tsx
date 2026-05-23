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
import { APP_NAME } from "@erp/shared";
import { mobileShellTitle } from "@erp/ui-mobile";
import { fetchUserInfo, login, logout } from "./src/api/auth";
import { createDepartment, createRole, fetchDepartments, fetchRoles } from "./src/api/system";
import { saveAccessToken, saveUser } from "./src/store/auth";
import type { UserInfo } from "./src/types/auth";
import type { DepartmentRecord, RoleRecord } from "./src/types/system";

type MobileTab = "departments" | "roles";

export default function App() {
  const [username, setUsername] = useState("admin");
  const [password, setPassword] = useState("password");
  const [user, setUser] = useState<UserInfo | null>(null);
  const [departments, setDepartments] = useState<DepartmentRecord[]>([]);
  const [roles, setRoles] = useState<RoleRecord[]>([]);
  const [activeTab, setActiveTab] = useState<MobileTab>("departments");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function loadSystemData() {
    const [departmentData, roleData] = await Promise.all([fetchDepartments(), fetchRoles()]);
    setDepartments(departmentData);
    setRoles(roleData);
  }

  async function handleLogin() {
    setLoading(true);
    setError(null);
    try {
      const result = await login({ username, password });
      saveAccessToken(result.accessToken);
      const info = await fetchUserInfo();
      saveUser(info);
      setUser(info);
      await loadSystemData();
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
      setDepartments([]);
      setRoles([]);
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
              <Text style={styles.title}>系统管理工作台</Text>
              <Text style={styles.subtitle}>欢迎回来，{user.realName || user.username}。移动端可维护部门与角色基础数据。</Text>
              <View style={styles.tabs}>
                <Pressable onPress={() => setActiveTab("departments")} style={[styles.tab, activeTab === "departments" && styles.activeTab]}><Text style={[styles.tabText, activeTab === "departments" && styles.activeTabText]}>部门</Text></Pressable>
                <Pressable onPress={() => setActiveTab("roles")} style={[styles.tab, activeTab === "roles" && styles.activeTab]}><Text style={[styles.tabText, activeTab === "roles" && styles.activeTabText]}>角色</Text></Pressable>
              </View>
              {activeTab === "departments" ? <DepartmentSection departments={departments} onReload={loadSystemData} /> : <RoleSection roles={roles} onReload={loadSystemData} />}
              <Pressable disabled={loading} onPress={() => void handleLogout()} style={[styles.secondaryButton, loading && styles.disabledButton]}><Text style={styles.secondaryButtonText}>{loading ? "退出中..." : "退出登录"}</Text></Pressable>
            </View>
          ) : (
            <View style={styles.panel}>
              <Text style={styles.badge}>{mobileShellTitle()}</Text>
              <Text style={styles.title}>全渠道 ERP 移动端登录</Text>
              <Text style={styles.subtitle}>连接后端服务，使用管理员账号进入移动端工作台。</Text>
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

function DepartmentSection({ departments, onReload }: { departments: DepartmentRecord[]; onReload: () => Promise<void> }) {
  const [name, setName] = useState("");
  const [code, setCode] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleCreate() {
    setSaving(true);
    setError(null);
    try {
      await createDepartment({ name, code, sortOrder: flattenDepartments(departments).length + 1 });
      setName("");
      setCode("");
      await onReload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "部门创建失败");
    } finally {
      setSaving(false);
    }
  }

  return <ManagementSection title="新建部门" name={name} code={code} error={error} saving={saving} onNameChange={setName} onCodeChange={setCode} onSubmit={handleCreate} items={flattenDepartments(departments).map((department) => ({ id: department.id, title: department.name, meta: department.code }))} />;
}

function RoleSection({ roles, onReload }: { roles: RoleRecord[]; onReload: () => Promise<void> }) {
  const [name, setName] = useState("");
  const [code, setCode] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleCreate() {
    setSaving(true);
    setError(null);
    try {
      await createRole({ name, code, dataScope: 1 });
      setName("");
      setCode("");
      await onReload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "角色创建失败");
    } finally {
      setSaving(false);
    }
  }

  return <ManagementSection title="新建角色" name={name} code={code} error={error} saving={saving} onNameChange={setName} onCodeChange={setCode} onSubmit={handleCreate} items={roles.map((role) => ({ id: role.id, title: role.name, meta: role.code }))} />;
}

function ManagementSection({ title, name, code, error, saving, items, onNameChange, onCodeChange, onSubmit }: {
  title: string;
  name: string;
  code: string;
  error: string | null;
  saving: boolean;
  items: Array<{ id: string; title: string; meta: string }>;
  onNameChange: (value: string) => void;
  onCodeChange: (value: string) => void;
  onSubmit: () => Promise<void>;
}) {
  return (
    <View style={styles.managementBlock}>
      <Text style={styles.sectionTitle}>{title}</Text>
      {error ? <Text style={styles.error}>{error}</Text> : null}
      <TextInput onChangeText={onNameChange} placeholder="名称" style={styles.input} value={name} />
      <TextInput autoCapitalize="characters" onChangeText={onCodeChange} placeholder="编码" style={styles.input} value={code} />
      <Pressable disabled={saving || !name || !code} onPress={() => void onSubmit()} style={[styles.primaryButton, (saving || !name || !code) && styles.disabledButton]}><Text style={styles.primaryButtonText}>{saving ? "保存中..." : "保存"}</Text></Pressable>
      <View style={styles.infoCard}>{items.map((item) => <InfoRow key={item.id} label={item.title} value={item.meta} />)}</View>
    </View>
  );
}

function InfoRow({ label, value }: { label: string; value: string }) {
  return <View style={styles.infoRow}><Text style={styles.infoLabel}>{label}</Text><Text style={styles.infoValue}>{value}</Text></View>;
}

function flattenDepartments(departments: DepartmentRecord[]): DepartmentRecord[] {
  return departments.flatMap((department) => [department, ...flattenDepartments(department.children ?? [])]);
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
  tabs: { flexDirection: "row", gap: 10 },
  tab: { flex: 1, alignItems: "center", paddingVertical: 10, borderRadius: 999, backgroundColor: "#e2e8f0" },
  activeTab: { backgroundColor: "#0f766e" },
  tabText: { color: "#334155", fontWeight: "700" },
  activeTabText: { color: "#ffffff" },
  managementBlock: { gap: 12 },
  sectionTitle: { fontSize: 18, fontWeight: "700", color: "#10263d" },
  infoCard: { gap: 12, padding: 16, borderRadius: 18, backgroundColor: "#f8fafc" },
  infoRow: { gap: 4 },
  infoLabel: { color: "#64748b", fontSize: 13, fontWeight: "700" },
  infoValue: { color: "#0f172a", fontSize: 15 }
});
