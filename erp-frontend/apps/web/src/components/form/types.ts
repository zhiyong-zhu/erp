/** 表单行编辑用的共享类型与辅助函数 */

export function createRowId() {
  if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
    return crypto.randomUUID();
  }
  return `row-${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

/** 规格定义行 */
export interface EditableSpecificationRow {
  rowId: string;
  key: string;
  valuesText: string;
}

/** SKU 属性行 */
export interface EditableAttributeRow {
  rowId: string;
  key: string;
  value: string;
}

/** SKU 行 */
export interface EditableSkuRow {
  rowId: string;
  id?: string;
  skuCode: string;
  attributes: string;
  barcode?: string | null;
  price?: number | null;
  costPrice?: number | null;
  weight?: number | null;
  status?: number;
}

export function createEmptySpecificationRow(): EditableSpecificationRow {
  return { rowId: createRowId(), key: "", valuesText: "" };
}

export function createEmptyAttributeRow(): EditableAttributeRow {
  return { rowId: createRowId(), key: "", value: "" };
}

export function createEmptySkuRow(): EditableSkuRow {
  return {
    rowId: createRowId(),
    skuCode: "",
    attributes: "{}",
    barcode: "",
    price: undefined,
    costPrice: undefined,
    weight: undefined,
    status: 1
  };
}

/* ---------- 规格定义 JSON 序列化 ---------- */

export function parseSpecifications(raw?: string | null): EditableSpecificationRow[] {
  if (!raw) {
    return [];
  }
  try {
    const parsed = JSON.parse(raw) as Record<string, unknown>;
    return Object.entries(parsed).map(([key, value]) => ({
      rowId: createRowId(),
      key,
      valuesText: Array.isArray(value) ? value.join(", ") : ""
    }));
  } catch {
    return [];
  }
}

export function serializeSpecifications(rows: EditableSpecificationRow[]): string | undefined {
  const result: Record<string, string[]> = {};
  for (const row of rows) {
    const key = row.key.trim();
    if (!key) {
      continue;
    }
    const values = row.valuesText
      .split(/[,，]/)
      .map((value) => value.trim())
      .filter(Boolean);
    if (values.length > 0) {
      result[key] = values;
    }
  }
  return Object.keys(result).length > 0 ? JSON.stringify(result) : undefined;
}

/* ---------- SKU 属性 JSON 序列化 ---------- */

export function parseAttributes(raw?: string | null): EditableAttributeRow[] {
  if (!raw) {
    return [createEmptyAttributeRow()];
  }
  try {
    const parsed = JSON.parse(raw) as Record<string, unknown>;
    const rows = Object.entries(parsed).map(([key, value]) => ({
      rowId: createRowId(),
      key,
      value: value == null ? "" : String(value)
    }));
    return rows.length > 0 ? rows : [createEmptyAttributeRow()];
  } catch {
    return [createEmptyAttributeRow()];
  }
}

export function serializeAttributes(rows: EditableAttributeRow[]): string | null {
  const result: Record<string, string> = {};
  for (const row of rows) {
    const key = row.key.trim();
    const value = row.value.trim();
    if (!key || !value) {
      continue;
    }
    result[key] = value;
  }
  return Object.keys(result).length > 0 ? JSON.stringify(result) : null;
}

/* ---------- 规格 → SKU 候选 生成 ---------- */

export function buildSkuRowsFromSpecifications(rows: EditableSpecificationRow[]): EditableSkuRow[] {
  const normalizedSpecs = rows
    .map((row) => ({
      key: row.key.trim(),
      values: row.valuesText
        .split(/[,，]/)
        .map((value) => value.trim())
        .filter(Boolean)
    }))
    .filter((row) => row.key && row.values.length > 0);

  if (normalizedSpecs.length === 0) {
    return [];
  }

  const combinations = buildAttributeCombinations(normalizedSpecs);
  return combinations.map((attributes) => {
    const serialized = JSON.stringify(attributes);
    return {
      rowId: createRowId(),
      skuCode: buildGeneratedSkuCode(attributes),
      attributes: serialized,
      barcode: "",
      price: undefined,
      costPrice: undefined,
      weight: undefined,
      status: 1
    };
  });
}

function buildAttributeCombinations(specs: Array<{ key: string; values: string[] }>) {
  let results: Array<Record<string, string>> = [{}];
  for (const spec of specs) {
    const nextResults: Array<Record<string, string>> = [];
    for (const result of results) {
      for (const value of spec.values) {
        nextResults.push({ ...result, [spec.key]: value });
      }
    }
    results = nextResults;
  }
  return results;
}

function buildGeneratedSkuCode(attributes: Record<string, string>) {
  const suffix = Object.values(attributes)
    .map((value) => value.replace(/\s+/g, "").slice(0, 4).toUpperCase())
    .join("-");
  return suffix ? `AUTO-${suffix}` : `AUTO-${Date.now()}`;
}
