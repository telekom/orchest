import { deepEqual } from "@/shared/utils/deepEqual";
import { clearTimeoutRef } from "@/shared/utils/timerUtils";
import { useStableCallback } from "@/shared/hooks/useStableCallback";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";

export interface UseLocalFiltersOptions<T extends Record<string, unknown>> {
  initialFilters: T;
  onFilterChange?: (filters: T) => void;
  debounceMs?: number;
  isActiveCheck?: (filters: T) => boolean;
}

export interface UseLocalFiltersReturn<T extends Record<string, unknown>> {
  filters: T;
  updateFilter: <K extends keyof T>(key: K, value: T[K]) => void;
  clearFilters: () => void;
  hasActiveFilters: boolean;
  setFilters: (filters: T | ((prev: T) => T)) => void;
  reset: () => void;
}

/**
 * Generic hook for local filter state management.
 * For URL-synced list pages, use useUrlFilters from '@/shared/url-state' instead.
 */
export function useLocalFilters<T extends Record<string, unknown>>({
  initialFilters,
  onFilterChange,
  debounceMs = 300,
  isActiveCheck,
}: UseLocalFiltersOptions<T>): UseLocalFiltersReturn<T> {
  const [filters, setFilters] = useState<T>(initialFilters);
  const prevFiltersRef = useRef<T>(initialFilters);
  const debounceTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const stableOnFilterChange = useStableCallback(onFilterChange);

  const updateFilter = useCallback(<K extends keyof T>(key: K, value: T[K]) => {
    setFilters((prev) => ({ ...prev, [key]: value }));
  }, []);

  const clearFilters = useCallback(() => {
    setFilters(initialFilters);
  }, [initialFilters]);

  const reset = clearFilters;

  const hasActiveFilters = useMemo(() => {
    if (isActiveCheck) {
      return isActiveCheck(filters);
    }

    return !deepEqual(filters, initialFilters);
  }, [filters, initialFilters, isActiveCheck]);

  useEffect(() => {
    const hasChanged = !deepEqual(prevFiltersRef.current, filters);

    if (hasChanged && stableOnFilterChange) {
      clearTimeoutRef(debounceTimerRef);

      debounceTimerRef.current = setTimeout(() => {
        prevFiltersRef.current = filters;
        stableOnFilterChange(filters);
      }, debounceMs);
    }

    return () => clearTimeoutRef(debounceTimerRef);
  }, [filters, debounceMs, stableOnFilterChange]);

  return {
    filters,
    updateFilter,
    clearFilters,
    hasActiveFilters,
    setFilters,
    reset,
  };
}
