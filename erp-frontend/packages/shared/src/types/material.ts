export interface MaterialRecord {
  id: string;
  code: string;
  name: string;
  unit: string;
  categoryId?: string;
  categoryName?: string;
  currentStock?: number | null;
  safetyStock?: number | null;
  status?: number;
}
