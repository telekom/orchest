import { useEffect, useState, useCallback, useRef } from 'react';

/**
 * Debounces a value by delaying updates until after a specified delay.
 *
 * @param value - The value to debounce
 * @param delay - Delay in milliseconds
 * @returns Debounced value
 *
 * @example
 * ```tsx
 * const debouncedSearchTerm = useDebounce(searchTerm, 500);
 *
 * useEffect(() => {
 *   // API call with debounced value
 *   searchAPI(debouncedSearchTerm);
 * }, [debouncedSearchTerm]);
 * ```
 */
export function useDebounce<T>(value: T, delay: number): T {
  const [debouncedValue, setDebouncedValue] = useState<T>(value);

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);

  return debouncedValue;
}

/**
 * Creates a debounced callback that delays execution.
 *
 * @param callback - Function to debounce
 * @param delay - Delay in milliseconds
 * @param deps - Dependency array for useCallback
 * @returns Debounced callback function
 *
 * @example
 * ```tsx
 * const debouncedSearch = useDebouncedCallback(
 *   (term: string) => {
 *     searchAPI(term);
 *   },
 *   500,
 *   []
 * );
 *
 * // In input handler
 * <input onChange={(e) => debouncedSearch(e.target.value)} />
 * ```
 */
export function useDebouncedCallback<T extends (...args: unknown[]) => unknown>(
  callback: T,
  delay: number,
  deps: React.DependencyList = []
): (...args: Parameters<T>) => void {
  const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  return useCallback((...args: Parameters<T>) => {
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
    }

    timeoutRef.current = setTimeout(() => {
      callback(...args);
    }, delay);
    // eslint-disable-next-line react-hooks/exhaustive-deps -- callback intentionally excluded; deps spread controlled by consumer
  }, [delay, ...deps]);
}
