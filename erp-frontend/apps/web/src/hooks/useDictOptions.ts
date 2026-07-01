import { useEffect, useState } from "react";
import { fetchDictData } from "../api/system";
import type { FieldOption } from "../components/CreateForm";

/**
 * 按字典编码加载字典项并转为下拉选项。
 * 复用于产品/原料/物料等需要从字典加载选项的表单字段。
 */
export function useDictOptions(dictCode: string): { options: FieldOption[]; loading: boolean } {
  const [options, setOptions] = useState<FieldOption[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let active = true;
    setLoading(true);
    fetchDictData(dictCode)
      .then((items) => {
        if (!active) return;
        setOptions(
          items
            .filter((d) => d.status !== 0)
            .sort((a, b) => a.sortOrder - b.sortOrder)
            .map((d) => ({ label: d.label, value: d.value }))
        );
      })
      .catch(() => {
        if (active) setOptions([]);
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [dictCode]);

  return { options, loading };
}
