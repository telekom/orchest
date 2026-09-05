import {
  DEFAULT_DECISION_LIST_URL_STATE,
  decisionListUrlSchema,
  type DecisionListUrlState,
} from '@/shared/url-state/configs/decisionListUrlState';
import { useUrlFilters } from '@/shared/url-state/hooks/useUrlFilters';
import type { DecisionFilters } from '../types';
import { useMemo } from 'react';

export function useDecisionListUrlState() {
  const urlState = useUrlFilters<DecisionListUrlState>({
    schema: decisionListUrlSchema,
    defaults: DEFAULT_DECISION_LIST_URL_STATE,
    pushHistoryKeys: ['status', 'decisionId', 'version', 'from', 'to'],
    onInvalidParams: (keys) => {
      if (import.meta.env.DEV) {
        console.warn('[DecisionList] Ignored invalid URL params:', keys);
      }
    },
  });

  const decisionFilters: DecisionFilters = useMemo(
    () => ({
      decisionId: urlState.filters.decisionId,
      version: urlState.filters.version,
      searchText: urlState.filters.searchText || null,
      status: urlState.filters.status,
      from: urlState.filters.from,
      to: urlState.filters.to,
      timezone: urlState.filters.timezone,
    }),
    [
      urlState.filters.decisionId,
      urlState.filters.version,
      urlState.filters.searchText,
      urlState.filters.status,
      urlState.filters.from,
      urlState.filters.to,
      urlState.filters.timezone,
    ],
  );

  return {
    ...urlState,
    decisionFilters,
  };
}

export type { DecisionListUrlState };
