import { statsService, type OrchestStatsQuerySlice } from "@/api/domains";
import type { OrchestStatsDTO } from "@/api/types/orchest-api";
import { queryKeys } from "@/shared/constants/queryKeys";
import { useApiQuery } from "@/shared/hooks";
import { useMemo } from "react";
import {
  mapOrchestStatsToDashboard,
  type MappedOrchestDashboard,
} from "../utils/orchestStatsDashboardMapper";

export interface UseOrchestDashboardDataOptions {
  refetchInterval?: number | false;
  refetchIntervalInBackground?: boolean;
  refetchOnWindowFocus?: boolean;
  refetchOnReconnect?: boolean;
  refetchOnMount?: boolean | "always";
  staleTime?: number;
  /** Null = unbounded / all-time request (no `from` / `to` query params). */
  statsQuery?: OrchestStatsQuerySlice | null;
}

export interface OrchestDashboardPayload extends MappedOrchestDashboard {
  /** Best-effort unified “last updated” time for UI (API field + client fetch time). */
  lastUpdatedMillis: number;
}

/**
 * Loads dashboard aggregates from consolidated `GET /orchest/stats`.
 *
 * Caches the raw {@link OrchestStatsDTO} under {@link queryKeys.orchest.stats}
 * (same key as Process/Decision list stats) and maps to dashboard shape in the
 * client — so navigating Process → Dashboard does not collide on incompatible
 * cached payloads.
 */
export function useOrchestDashboardData(
  options: UseOrchestDashboardDataOptions = {}
) {
  const {
    refetchInterval,
    refetchIntervalInBackground,
    refetchOnWindowFocus,
    refetchOnReconnect,
    refetchOnMount,
    staleTime = 0,
    statsQuery = null,
  } = options;

  const query = useApiQuery<OrchestStatsDTO>(
    queryKeys.orchest.stats(statsQuery),
    async () => statsService.getOrchestStats(statsQuery ?? null),
    {
      showErrorToast: true,
      enabled: true,
      staleTime,
      refetchInterval,
      refetchIntervalInBackground,
      refetchOnWindowFocus,
      refetchOnReconnect,
      refetchOnMount,
    }
  );

  const data = useMemo((): OrchestDashboardPayload | undefined => {
    if (query.data === undefined) return undefined;
    const mapped = mapOrchestStatsToDashboard(query.data);
    let serverTs = query.dataUpdatedAt || Date.now();
    if (query.data.lastUpdatedAt) {
      const parsed = Date.parse(query.data.lastUpdatedAt);
      if (!Number.isNaN(parsed)) serverTs = parsed;
    }
    return {
      ...mapped,
      lastUpdatedMillis: serverTs,
    };
  }, [query.data, query.dataUpdatedAt]);

  return {
    ...query,
    data,
  };
}
