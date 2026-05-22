import React from "react";
import { SafeAreaView, StyleSheet, Text, View } from "react-native";
import { StatusBar } from "expo-status-bar";
import { APP_NAME } from "@erp/shared";
import { mobileShellTitle } from "@erp/ui-mobile";

export default function App() {
  return (
    <SafeAreaView style={styles.container}>
      <StatusBar style="dark" />
      <View style={styles.panel}>
        <Text style={styles.badge}>Expo Skeleton</Text>
        <Text style={styles.title}>{mobileShellTitle()}</Text>
        <Text style={styles.subtitle}>{APP_NAME} 移动端骨架已创建。</Text>
        <Text style={styles.text}>后续可继续接入扫码、推送、离线队列和审批待办。</Text>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#eef6fb"
  },
  panel: {
    margin: 20,
    marginTop: 32,
    padding: 24,
    borderRadius: 24,
    backgroundColor: "#ffffff"
  },
  badge: {
    alignSelf: "flex-start",
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 999,
    backgroundColor: "#0f766e",
    color: "#ffffff",
    overflow: "hidden"
  },
  title: {
    marginTop: 18,
    fontSize: 30,
    fontWeight: "700",
    color: "#10263d"
  },
  subtitle: {
    marginTop: 8,
    fontSize: 18,
    color: "#2f4f6a"
  },
  text: {
    marginTop: 14,
    fontSize: 15,
    lineHeight: 24,
    color: "#5b7089"
  }
});
