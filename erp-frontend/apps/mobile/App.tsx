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
import {
  createDepartment,
  createRole,
  fetchDepartments,
  fetchRoles,
  updateDepartment,
  updateDepartmentStatus,
  updateRole,
  updateRoleStatus
} from "./src/api/system";
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
  const [editingDepartment, setEditingDepartment] = useState<DepartmentRecord | null>(null);
  const [name, setName] = useState("");
  const [code, setCode] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  function startEdit(department: DepartmentRecord) {
    setEditingDepartment(department);
    setName(department.name);
    setCode(department.code);
    setError(null);
  }

  function resetForm() {
    setEditingDepartment(null);
    setName("");
    setCode("");
  }

  async function handleSubmit() {
    setSaving(true);
    setError(null);
    try {
      if (editingDepartment) {
        await updateDepartment(editingDepartment.id, {
          parentId: editingDepartment.parentId ?? null,
          name,
          code,
          leader: editingDepartment.leader ?? undefined,
          phone: editingDepartment.phone ?? undefined,
          sortOrder: editingDepartment.sortOrder
        });
      } else {
        await createDepartment({ name, code, sortOrder: flattenDepartments(departments).length + 1 });
      }
      resetForm();
      await onReload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "部门保存失败");
    } finally {
      setSaving(false);
    }
  }

  async function handleToggleStatus(department: DepartmentRecord) {
    setSaving(true);
    setError(null);
    try {
      await updateDepartmentStatus(department.id, department.status === 1 ? 0 : 1);
      await onReload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "部门状态更新失败");
    } finally {
      setSaving(false);
    }
  }

  return (
    <ManagementSection
      title={editingDepartment ? "编辑部门" : "新建部门"}
      name={name}
      code={code}
      error={error}
      saving={saving}
      submitText={editingDepartment ? "更新" : "保存"}
      onCancelEdit={editingDepartment ? resetForm : undefined}
      onNameChange={setName}
      onCodeChange={setCode}
      onSubmit={handleSubmit}
      items={flattenDepartments(departments).map((department) => ({
        id: department.id,
        title: department.name,
        meta: `${department.code} · ${department.status === 1 ? "启用" : "禁用"}`,
        status: department.status,
        onEdit: () => startEdit(department),
        onToggleStatus: () => void handleToggleStatus(department)
      }))}
    />
  );
}

function RoleSection({ roles, onReload }: { roles: RoleRecord[]; onReload: () => Promise<void> }) {
  const [editingRole, setEditingRole] = useState<RoleRecord | null>(null);
  const [name, setName] = useState("");
  const [code, setCode] = useState("");
  const [description, setDescription] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  function startEdit(role: RoleRecord) {
    setEditingRole(role);
    setName(role.name);
    setCode(role.code);
    setDescription(role.description ?? "");
    setError(null);
  }

  function resetForm() {
    setEditingRole(null);
    setName("");
    setCode("");
    setDescription("");
  }

  async function handleSubmit() {
    setSaving(true);
    setError(null);
    try {
      if (editingRole) {
        await updateRole(editingRole.id, {
          name,
          code,
          description,
          dataScope: editingRole.dataScope,
          permissionIds: editingRole.permissionIds ?? []
        });
      } else {
        await createRole({ name, code, description, dataScope: 1 });
      }
      resetForm();
      await onReload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "角色保存失败");
    } finally {
      setSaving(false);
    }
  }

  async function handleToggleStatus(role: RoleRecord) {
    setSaving(true);
    setError(null);
    try {
      await updateRoleStatus(role.id, role.status === 1 ? 0 : 1);
      await onReload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "角色状态更新失败");
    } finally {
      setSaving(false);
    }
  }

  return (
    <ManagementSection
      title={editingRole ? "编辑角色" : "新建角色"}
      name={name}
      code={code}
      description={description}
      descriptionPlaceholder="描述"
      error={error}
      saving={saving}
      submitText={editingRole ? "更新" : "保存"}
      onCancelEdit={editingRole ? resetForm : undefined}
      onNameChange={setName}
      onCodeChange={setCode}
      onDescriptionChange={setDescription}
      onSubmit={handleSubmit}
      items={roles.map((role) => ({
        id: role.id,
        title: role.name,
        meta: `${role.code} · ${renderDataScope(role.dataScope)} · ${role.status === 1 ? "启用" : "禁用"}`,
        status: role.status,
        onEdit: () => startEdit(role),
        onToggleStatus: () => void handleToggleStatus(role)
      }))}
    />
  );
}

function ManagementSection({ title, name, code, description, descriptionPlaceholder, error, saving, submitText, items, onCancelEdit, onNameChange, onCodeChange, onDescriptionChange, onSubmit }: {
  title: string;
  name: string;
  code: string;
  description?: string;
  descriptionPlaceholder?: string;
  error: string | null;
  saving: boolean;
  submitText: string;
  items: Array<{ id: string; title: string; meta: string; status: number; onEdit: () => void; onToggleStatus: () => void }>;
  onCancelEdit?: () => void;
  onNameChange: (value: string) => void;
  onCodeChange: (value: string) => void;
  onDescriptionChange?: (value: string) => void;
  onSubmit: () => Promise<void>;
}) {
  return (
    <View style={styles.managementBlock}>
      <View style={styles.sectionTitleRow}>
        <Text style={styles.sectionTitle}>{title}</Text>
        {onCancelEdit ? <Pressable onPress={onCancelEdit}><Text style={styles.linkText}>取消</Text></Pressable> : null}
      </View>
      {error ? <Text style={styles.error}>{error}</Text> : null}
      <TextInput onChangeText={onNameChange} placeholder="名称" style={styles.input} value={name} />
      <TextInput autoCapitalize="characters" onChangeText={onCodeChange} placeholder="编码" style={styles.input} value={code} />
      {onDescriptionChange ? <TextInput onChangeText={onDescriptionChange} placeholder={descriptionPlaceholder} style={styles.input} value={description} /> : null}
      <Pressable disabled={saving || !name || !code} onPress={() => void onSubmit()} style={[styles.primaryButton, (saving || !name || !code) && styles.disabledButton]}><Text style={styles.primaryButtonText}>{saving ? "保存中..." : submitText}</Text></Pressable>
      <View style={styles.infoCard}>{items.map((item) => <InfoRow key={item.id} item={item} saving={saving} />)}</View>
    </View>
  );
}

function InfoRow({ item, saving }: { item: { title: string; meta: string; status: number; onEdit: () => void; onToggleStatus: () => void }; saving: boolean }) {
  return (
    <View style={styles.infoRow}>
      <View style={styles.infoTextBlock}>
        <Text style={styles.infoLabel}>{item.title}</Text>
        <Text style={styles.infoValue}>{item.meta}</Text>
      </View>
      <View style={styles.rowActions}>
        <Pressable disabled={saving} onPress={item.onEdit}><Text style={[styles.linkText, saving && styles.disabledText]}>编辑</Text></Pressable>
        <Pressable disabled={saving} onPress={item.onToggleStatus}><Text style={[styles.linkText, saving && styles.disabledText]}>{item.status === 1 ? "禁用" : "启用"}</Text></Pressable>
      </View>
    </View>
  );
}

function flattenDepartments(departments: DepartmentRecord[]): DepartmentRecord[] {
  return departments.flatMap((department) => [department, ...flattenDepartments(department.children ?? [])]);
}

function renderDataScope(dataScope: number) {
  if (dataScope === 1) return "全部数据";
  if (dataScope === 2) return "部门数据";
  if (dataScope === 3) return "本人数据";
  return `数据范围 ${dataScope}`;
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
  sectionTitleRow: { flexDirection: "row", alignItems: "center", justifyContent: "space-between", gap: 12 },
  sectionTitle: { fontSize: 18, fontWeight: "700", color: "#10263d" },
  infoCard: { gap: 12, padding: 16, borderRadius: 18, backgroundColor: "#f8fafc" },
  infoRow: { flexDirection: "row", justifyContent: "space-between", gap: 12 },
  infoTextBlock: { flex: 1, gap: 4 },
  infoLabel: { color: "#64748b", fontSize: 13, fontWeight: "700" },
  infoValue: { color: "#0f172a", fontSize: 15 },
  rowActions: { flexDirection: "row", alignItems: "center", gap: 12 },
  linkText: { color: "#0f766e", fontWeight: "700" },
  disabledText: { opacity: 0.55 }
});
