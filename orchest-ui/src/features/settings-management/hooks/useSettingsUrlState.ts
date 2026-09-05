import { FilterValue } from '@/shared/constants/status';
import {
  DEFAULT_SETTINGS_URL_STATE,
  settingsUrlSchema,
  type SettingsUrlState,
} from '@/shared/url-state/configs/settingsUrlState';
import { useUrlFilters } from '@/shared/url-state/hooks/useUrlFilters';
import type { UrlHistoryMode } from '@/shared/url-state/types';
import { useCallback } from 'react';

export function useSettingsUrlState() {
  const urlState = useUrlFilters<SettingsUrlState>({
    schema: settingsUrlSchema,
    defaults: DEFAULT_SETTINGS_URL_STATE,
    pushHistoryKeys: ['statusFilter', 'page', 'pageSize'],
    onInvalidParams: (keys) => {
      if (import.meta.env.DEV) {
        console.warn('[Settings] Ignored invalid URL params:', keys);
      }
    },
  });

  const updateFilter = useCallback(
    <K extends keyof SettingsUrlState>(key: K, value: SettingsUrlState[K]) => {
      const resetsPage = key === 'searchTerm' || key === 'statusFilter';
      urlState.setFilters(
        { [key]: value, ...(resetsPage ? { page: 0 } : {}) } as Partial<SettingsUrlState>,
        key === 'searchTerm' ? 'replace' : 'push',
      );
    },
    [urlState.setFilters],
  );

  const handlePageChangeWithTotal = useCallback(
    (direction: 'next' | 'prev' | number, totalPages: number) => {
      const { page } = urlState.filters;

      let nextPage = page;
      if (typeof direction === 'number') {
        nextPage = direction;
      } else if (direction === 'next') {
        nextPage = page + 1;
      } else if (direction === 'prev') {
        nextPage = page - 1;
      }

      if (nextPage < 0 || nextPage >= totalPages) return;
      urlState.setFilters({ page: nextPage }, 'push');
    },
    [urlState.filters, urlState.setFilters],
  );

  const handlePageSizeChange = useCallback(
    (pageSize: number) => {
      urlState.setFilters({ pageSize, page: 0 }, 'push');
    },
    [urlState.setFilters],
  );

  const clearFilters = useCallback(() => {
    urlState.clearFilters('push');
  }, [urlState.clearFilters]);

  const hasActiveFilters =
    !!urlState.filters.searchTerm ||
    urlState.filters.statusFilter !== FilterValue.ALL;

  return {
    filters: urlState.filters,
    updateFilter,
    clearFilters,
    hasActiveFilters,
    handlePageChangeWithTotal,
    handlePageSizeChange,
    setFilters: urlState.setFilters,
  };
}

export type { SettingsUrlState };
