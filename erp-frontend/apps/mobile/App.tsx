import React, { useState } from "react";
import {
  ActivityIndicator,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  SafeAreaView,
  ScrollView,
  Text,
  TextInput,
  View
} from "react-native";
import { StatusBar } from "expo-status-bar";
import { mobileShellTitle } from "@erp/ui-mobile";
import { fetchUserInfo, login, logout } from "./src/api/auth";
import { saveAccessToken, saveUser } from "./src/store/auth";
import type { UserInfo } from "./src/types/auth";
import { OperationDetail } from "./src/components/operations/OperationDetail";
import { operationCards, type OperationKey } from "./src/components/operations/operationConfig";
import { styles } from "./src/styles";


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
              <Text style={styles.subtitle}>欢迎回来，{user.realName || user.username}。移动端聚焦现场扫码、盘点、生产执行和打印预览。</Text>
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
              <OperationDetail user={user} operation={operationCards.find((operation) => operation.key === activeOperation) ?? operationCards[0]} />
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
