import { Pressable, Text, View } from "react-native";
import { styles } from "../../styles";

export function ChoiceList({ items, selectedId, onSelect }: { items: Array<{ id: string; title: string; subtitle?: string | null }>; selectedId: string; onSelect: (id: string) => void }) {
  return (
    <View style={styles.choiceList}>
      {items.length ? items.slice(0, 6).map((item) => (
        <Pressable key={item.id} onPress={() => onSelect(item.id)} style={[styles.choiceItem, selectedId === item.id && styles.activeChoiceItem]}>
          <Text style={[styles.choiceTitle, selectedId === item.id && styles.activeOperationText]}>{item.title}</Text>
          {item.subtitle ? <Text style={[styles.choiceSubtitle, selectedId === item.id && styles.activeOperationText]}>{item.subtitle}</Text> : null}
        </Pressable>
      )) : <Text style={styles.summary}>暂无可选数据</Text>}
    </View>
  );
}

export function RecordPreview({ title, records }: { title: string; records: string[] }) {
  return (
    <View style={styles.previewCard}>
      <Text style={styles.label}>{title}</Text>
      {records.length ? records.slice(0, 4).map((record) => <Text key={record} style={styles.previewText}>{record}</Text>) : <Text style={styles.previewText}>暂无记录</Text>}
    </View>
  );
}

export function defaultBatchNo() {
  const now = new Date();
  return `WO-${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, "0")}${String(now.getDate()).padStart(2, "0")}-${String(now.getHours()).padStart(2, "0")}${String(now.getMinutes()).padStart(2, "0")}`;
}

export function toNumber(value: string) {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
}

export function formatStatus(status: string) {
  const statusMap: Record<string, string> = {
    DRAFT: "草稿",
    RELEASED: "已下达",
    IN_PROGRESS: "生产中",
    COMPLETED: "已完工",
    CLOSED: "已关闭"
  };
  return statusMap[status] ?? status;
}
