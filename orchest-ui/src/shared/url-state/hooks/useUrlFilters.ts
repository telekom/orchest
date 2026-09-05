import { deepEqual } from '@/shared/utils/deepEqual';
import { useCallback, useEffect, useMemo, useRef } from 'react';
import { useSearchParams } from 'react-router-dom';
import { buildSearchParams, searchParamsToString } from '../buildSearchParams';
import { parseSearchParams } from '../parseSearchParams';
import type { UrlHistoryMode, UrlStateSchema } from '../types';

export interface UseUrlFiltersOptions<T extends Record<string, unknown>> {
  schema: UrlStateSchema<T>;
  defaults: T;
  pushHistoryKeys?: (keyof T)[];
  onInvalidParams?: (keys: string[]) => void;
}

export interface UseUrlFiltersReturn<T extends Record<string, unknown>> {
  filters: T;
  setFilters: (patch: Partial<T>, mode?: UrlHistoryMode) => void;
  replaceFilters: (next: T, mode?: UrlHistoryMode) => void;
  clearFilters: (mode?: UrlHistoryMode) => void;
  hasActiveFilters: boolean;
}

export function useUrlFilters<T extends Record<string, unknown>>({
  schema,
  defaults,
  pushHistoryKeys = [],
  onInvalidParams,
}: UseUrlFiltersOptions<T>): UseUrlFiltersReturn<T> {
  const [searchParams, setSearchParams] = useSearchParams();
  const lastSerialized = useRef<string>(searchParamsToString(searchParams));

  useEffect(() => {
    lastSerialized.current = searchParamsToString(searchParams);
  }, [searchParams]);

  const { state: filters, invalidKeys } = useMemo(
    () => parseSearchParams(searchParams, schema, defaults),
    [searchParams, schema, defaults],
  );

  useEffect(() => {
    if (invalidKeys.length > 0) {
      onInvalidParams?.(invalidKeys);
    }
  }, [invalidKeys, onInvalidParams]);

  const writeToUrl = useCallback(
    (nextState: T, mode: UrlHistoryMode) => {
      const params = buildSearchParams(nextState, schema, defaults);

      // Keep non-schema query keys (e.g. Dashboard `view=alerts`) intact.
      for (const [key, value] of searchParams.entries()) {
        if (!(key in schema) && !params.has(key)) {
          params.set(key, value);
        }
      }

      const serialized = searchParamsToString(params);

      if (serialized === lastSerialized.current) return;

      lastSerialized.current = serialized;
      setSearchParams(params, { replace: mode === 'replace' });
    },
    [schema, defaults, setSearchParams, searchParams],
  );

  const setFilters = useCallback(
    (patch: Partial<T>, mode?: UrlHistoryMode) => {
      const next = { ...filters, ...patch };
      if (deepEqual(next, filters)) return;

      const resolvedMode =
        mode ??
        (Object.keys(patch).some((key) => pushHistoryKeys.includes(key as keyof T))
          ? 'push'
          : 'replace');

      writeToUrl(next, resolvedMode);
    },
    [filters, pushHistoryKeys, writeToUrl],
  );

  const replaceFilters = useCallback(
    (next: T, mode: UrlHistoryMode = 'replace') => {
      if (deepEqual(next, filters)) return;
      writeToUrl(next, mode);
    },
    [filters, writeToUrl],
  );

  const clearFilters = useCallback(
    (mode: UrlHistoryMode = 'push') => {
      writeToUrl(defaults, mode);
    },
    [defaults, writeToUrl],
  );

  const hasActiveFilters = useMemo(
    () => !deepEqual(filters, defaults),
    [filters, defaults],
  );

  return {
    filters,
    setFilters,
    replaceFilters,
    clearFilters,
    hasActiveFilters,
  };
}
