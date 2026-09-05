import type { OrchestStatItem, OrchestStatsDTO } from "@/api/types/orchest-api";

function readObject(value: unknown): Record<string, unknown> | null {
  if (value === null || value === undefined) return null;
  if (typeof value !== "object") return null;
  if (Array.isArray(value)) return null;
  return value as Record<string, unknown>;
}

/**
 * Normalize a single `{ state, count }` entry tolerating casing / numeric strings.
 */
function normalizeStatEntry(entry: unknown): OrchestStatItem | null {
  const o = readObject(entry);
  if (!o) return null;

  const stateRaw =
    o.state ??
    o.State ??
    o.STATE ??
    o.name ??
    o.key;
  if (stateRaw === undefined || stateRaw === null) return null;

  const countRaw = o.count ?? o.Count ?? o.total ?? o.value;
  let count = 0;
  if (typeof countRaw === "number" && Number.isFinite(countRaw)) {
    count = Math.trunc(countRaw);
  } else if (countRaw != null && countRaw !== "") {
    const parsed = Number.parseInt(String(countRaw), 10);
    count = Number.isFinite(parsed) ? parsed : 0;
  }

  return { state: String(stateRaw), count };
}

function normalizeStatList(raw: unknown): OrchestStatItem[] {
  if (!Array.isArray(raw)) return [];
  const out: OrchestStatItem[] = [];
  for (const item of raw) {
    const n = normalizeStatEntry(item);
    if (n) out.push(n);
  }
  return out;
}

/**
 * Outer key = process/decision id, inner key = version, value = stat list.
 */
function normalizeGroupedMap(
  raw: unknown
): Record<string, Record<string, OrchestStatItem[]>> {
  const outer = readObject(raw);
  if (!outer) return {};

  const result: Record<string, Record<string, OrchestStatItem[]>> = {};
  for (const [outerKey, innerRaw] of Object.entries(outer)) {
    const innerObj = readObject(innerRaw);
    if (!innerObj) continue;

    const innerOut: Record<string, OrchestStatItem[]> = {};
    for (const [versionKey, listRaw] of Object.entries(innerObj)) {
      innerOut[versionKey] = normalizeStatList(listRaw);
    }
    result[outerKey] = innerOut;
  }
  return result;
}

function hasStatsShape(obj: Record<string, unknown>): boolean {
  return [
    "totalProcessStats",
    "total_process_stats",
    "totalDecisionStats",
    "total_decision_stats",
    "processStats",
    "process_stats",
    "decisionStats",
    "decision_stats",
    "lastUpdatedAt",
    "last_updated_at",
  ].some((key) => key in obj && obj[key] != null);
}

/**
 * Many Orchest endpoints return `{ meta, data }`. Some gateways use snake_case.
 * This picks the object that actually holds StatsDTO fields.
 */
function unwrapStatsPayload(rawBody: unknown): Record<string, unknown> {
  const root = readObject(rawBody);
  if (!root) return {};

  // Standard `{ meta, data }` envelope used across Orchest APIs
  if ("meta" in root && root.data !== undefined) {
    const inner = readObject(root.data);
    if (inner) return inner;
  }

  if ("success" in root && root.data !== undefined) {
    const inner = readObject(root.data);
    if (inner) return inner;
  }

  if (hasStatsShape(root)) return root;

  const genericData = readObject(root.data);
  if (genericData && hasStatsShape(genericData)) {
    return genericData;
  }

  return root;
}

/**
 * Coerces raw JSON into {@link OrchestStatsDTO}, regardless of wrapper or casing.
 */
export function coerceOrchestStatsResponse(rawBody: unknown): OrchestStatsDTO {
  const r = unwrapStatsPayload(rawBody);

  const totalProcessStats = normalizeStatList(
    r.totalProcessStats ?? r.total_process_stats
  );
  const totalDecisionStats = normalizeStatList(
    r.totalDecisionStats ?? r.total_decision_stats
  );
  const processStats = normalizeGroupedMap(
    r.processStats ?? r.process_stats
  );
  const decisionStats = normalizeGroupedMap(
    r.decisionStats ?? r.decision_stats
  );

  const lastRaw = r.lastUpdatedAt ?? r.last_updated_at;
  const lastUpdatedAt =
    lastRaw === undefined || lastRaw === null ? null : String(lastRaw);

  return {
    totalProcessStats,
    totalDecisionStats,
    processStats,
    decisionStats,
    lastUpdatedAt,
  };
}
