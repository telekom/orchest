import { httpClient } from "@/api/core";
import type { OrchestStatsDTO } from "@/api/types/orchest-api";
import { coerceOrchestStatsResponse } from "./coerceOrchestStatsResponse";

const ORCHEST_BASE = "/orchest";

/** Optional query for `GET /orchest/stats` (when backend supports date-bounded aggregates). */
export type OrchestStatsQuerySlice = {
  from?: string;
  to?: string;
  /** IANA zone — mirrors how `from` / `to` instants were derived */
  timeZone?: string;
};

/**
 * Consolidated orchestration statistics endpoint (Swagger path `/stats` under OrchesT).
 */
export class StatsService {
  async getOrchestStats(
    querySlice: OrchestStatsQuerySlice | null = null,
  ): Promise<OrchestStatsDTO> {
    let url = `${ORCHEST_BASE}/stats`;
    if (querySlice != null) {
      const params: Record<string, string> = {};
      if (querySlice.from) params.from = querySlice.from;
      if (querySlice.to) params.to = querySlice.to;
      if (querySlice.timeZone) params.tz = querySlice.timeZone;
      const qs = new URLSearchParams(params).toString();
      if (qs) url = `${url}?${qs}`;
    }
    const raw = await httpClient.get<unknown>(url);
    return coerceOrchestStatsResponse(raw);
  }
}

export const statsService = new StatsService();
