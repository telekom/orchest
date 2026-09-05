import type { SortConfig } from '@/shared/components/DataTable/types';
import {
  DEFAULT_PROCESS_LIST_URL_STATE,
  processListUrlSchema,
  type ProcessListUrlState,
} from '@/shared/url-state/configs/processListUrlState';
import { useUrlFilters } from '@/shared/url-state/hooks/useUrlFilters';
import { useMemo } from 'react';

export function useProcessListUrlState() {
  const urlState = useUrlFilters<ProcessListUrlState>({
    schema: processListUrlSchema,
    defaults: DEFAULT_PROCESS_LIST_URL_STATE,
    pushHistoryKeys: [
      'status',
      'process',
      'version',
      'from',
      'to',
      'sortBy',
      'sortOrder',
    ],
    onInvalidParams: (keys) => {
      if (import.meta.env.DEV) {
        console.warn('[ProcessList] Ignored invalid URL params:', keys);
      }
    },
  });

  const sortConfig: SortConfig[] = useMemo(
    () => [{ field: urlState.filters.sortBy, direction: urlState.filters.sortOrder }],
    [urlState.filters.sortBy, urlState.filters.sortOrder],
  );

  const processFilters = useMemo(
    () => ({
      process: urlState.filters.process,
      version: urlState.filters.version,
      searchText: urlState.filters.searchText,
      status: urlState.filters.status,
      from: urlState.filters.from,
      to: urlState.filters.to,
      timezone: urlState.filters.timezone,
    }),
    [
      urlState.filters.process,
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
    processFilters,
    sortConfig,
  };
}

export type { ProcessListUrlState };
