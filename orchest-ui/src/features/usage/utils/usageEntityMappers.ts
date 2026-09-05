import {
  heatColorForCount,
  heatTextColorForCount,
} from './usageMappers';

export type UsageKind = 'bpmn' | 'dmn';

export interface UsageEntityRow {
  id: string;
  total: number;
}

export function buildEntityRows(
  items: { id: string; total: number }[] | undefined,
): UsageEntityRow[] {
  if (!items?.length) return [];
  return [...items].sort((a, b) => b.total - a.total);
}

export function maxEntityTotal(rows: UsageEntityRow[]): number {
  return rows.reduce((m, r) => Math.max(m, r.total), 0);
}

export interface ChartEntityBar {
  id: string;
  label: string;
  total: number;
}

export function buildTopEntityBars(
  rows: UsageEntityRow[],
  limit = 10,
): ChartEntityBar[] {
  return rows.slice(0, limit).map((r) => ({
    id: r.id,
    label: r.id.length > 28 ? `${r.id.slice(0, 26)}…` : r.id,
    total: r.total,
  }));
}

export { heatColorForCount, heatTextColorForCount };
