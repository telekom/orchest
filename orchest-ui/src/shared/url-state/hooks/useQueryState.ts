import { useCallback, useMemo } from 'react';
import { useSearchParams } from 'react-router-dom';
import type { UrlFieldDef, UrlHistoryMode } from '../types';

interface UseQueryStateOptions<T> {
  key: string;
  def: UrlFieldDef<T>;
  defaultValue: T;
  history?: UrlHistoryMode;
}

export function useQueryState<T>({
  key,
  def,
  defaultValue,
  history = 'replace',
}: UseQueryStateOptions<T>): [T, (value: T, mode?: UrlHistoryMode) => void] {
  const [searchParams, setSearchParams] = useSearchParams();

  const value = useMemo(() => {
    const raw = searchParams.get(key);
    if (raw == null) return defaultValue;

    try {
      const parsed = def.parse(raw);
      return def.isEmpty?.(parsed) ? defaultValue : parsed;
    } catch {
      return defaultValue;
    }
  }, [searchParams, key, def, defaultValue]);

  const setValue = useCallback(
    (next: T, mode: UrlHistoryMode = history) => {
      setSearchParams(
        (prev) => {
          const nextParams = new URLSearchParams(prev);
          const serialized = def.serialize(next);
          const isDefault = def.isEmpty?.(next) ?? next === defaultValue;

          if (isDefault || serialized == null) {
            nextParams.delete(key);
          } else {
            nextParams.set(key, serialized);
          }

          return nextParams;
        },
        { replace: mode === 'replace' },
      );
    },
    [key, def, defaultValue, history, setSearchParams],
  );

  return [value, setValue];
}
