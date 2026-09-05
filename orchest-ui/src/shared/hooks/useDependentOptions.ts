import { useCallback, useEffect, useState } from 'react';

export interface UseDependentOptionsConfig<T> {
  parentValue: string | null;
  fetchOptions: (parentValue: string) => T[] | Promise<T[]>;
  autoSelectFirst?: boolean;
  initialSelected?: T | null;
}

/**
 * Hook for managing dependent dropdown options (cascading dropdowns)
 */
export function useDependentOptions<T>(config: UseDependentOptionsConfig<T>) {
  const {
    parentValue,
    fetchOptions,
    autoSelectFirst = false,
    initialSelected = null,
  } = config;

  const [options, setOptions] = useState<T[]>([]);
  const [selected, setSelected] = useState<T | null>(initialSelected);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const clear = useCallback(() => {
    setOptions([]);
    setSelected(null);
    setError(null);
  }, []);

  const refresh = useCallback(async () => {
    if (!parentValue) {
      clear();
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      const result = await Promise.resolve(fetchOptions(parentValue));
      setOptions(result);

      if (autoSelectFirst && result.length > 0) {
        setSelected(result[0]);
      } else if (result.length === 0) {
        setSelected(null);
      }
    } catch (err) {
      const error = err instanceof Error ? err : new Error(String(err));
      setError(error);
      setOptions([]);
      setSelected(null);
    } finally {
      setIsLoading(false);
    }
  }, [parentValue, fetchOptions, autoSelectFirst, clear]);

  useEffect(() => {
    if (!parentValue) {
      clear();
      return;
    }

    refresh();
  }, [parentValue, refresh, clear]);

  return {
    options,
    selected,
    setSelected,
    isLoading,
    error,
    clear,
    refresh,
    hasOptions: options.length > 0,
  };
}
