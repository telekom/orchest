import { useDebounce } from '@/shared/hooks/useDebounce';
import { useCallback, useEffect, useRef, useState } from 'react';
import type { UrlHistoryMode } from '../types';

interface UseDebouncedUrlFilterOptions<T extends Record<string, unknown>> {
  filters: T;
  setFilters: (patch: Partial<T>, mode?: UrlHistoryMode) => void;
  key: keyof T;
  delayMs?: number;
}

export function useDebouncedUrlFilter<T extends Record<string, unknown>>({
  filters,
  setFilters,
  key,
  delayMs = 400,
}: UseDebouncedUrlFilterOptions<T>) {
  const urlValue = String(filters[key] ?? '');
  const [localValue, setLocalValue] = useState(urlValue);
  const skipDebounceRef = useRef(false);

  useEffect(() => {
    setLocalValue(urlValue);
  }, [urlValue]);

  const debouncedValue = useDebounce(localValue, delayMs);

  useEffect(() => {
    if (skipDebounceRef.current) {
      skipDebounceRef.current = false;
      return;
    }
    if (debouncedValue === urlValue) return;
    setFilters({ [key]: debouncedValue } as Partial<T>, 'replace');
  }, [debouncedValue, urlValue, key, setFilters]);

  const onChange = useCallback((value: string) => {
    setLocalValue(value);
  }, []);

  const clear = useCallback(() => {
    skipDebounceRef.current = true;
    setLocalValue('');
    setFilters({ [key]: '' } as Partial<T>, 'replace');
  }, [key, setFilters]);

  return { localValue, onChange, clear };
}
