import { useMemo } from 'react';

export interface DropdownOption {
  value: string;
  label: string;
  [key: string]: unknown;
}

export interface UseDropdownOptionsConfig<T> {
  data: T[] | undefined;
  transform: (items: T[]) => DropdownOption[];
  includeAllOption?: boolean;
  allOption?: DropdownOption;
  dependencies?: unknown[];
}

/**
 * Hook for transforming array data into dropdown options with memoization
 */
export function useDropdownOptions<T>(
  config: UseDropdownOptionsConfig<T>
): DropdownOption[] {
  const {
    data,
    transform,
    includeAllOption = false,
    allOption = { value: 'all', label: 'All' },
    dependencies = [],
  } = config;

  return useMemo(() => {
    if (!data || !Array.isArray(data) || data.length === 0) {
      return includeAllOption ? [allOption] : [];
    }

    const options = transform(data);

    if (includeAllOption) {
      return [allOption, ...options];
    }

    return options;
    // eslint-disable-next-line react-hooks/exhaustive-deps -- allOption tracked by value/label; transform/dependencies controlled by consumer
  }, [data, includeAllOption, allOption?.value, allOption?.label, ...dependencies]);
}

export function createUniqueOptions<T>(
  items: T[],
  key: keyof T,
  labelKey?: keyof T
): DropdownOption[] {
  const uniqueValues = Array.from(new Set(items.map((item) => item[key])));

  return uniqueValues.map((value, index) => ({
    value: String(value || `_generated_${index}`),
    label: labelKey
      ? String(items.find((item) => item[key] === value)?.[labelKey] || value)
      : String(value || `Item ${index + 1}`),
  }));
}

export function createGroupedOptions<T, M extends Record<string, unknown>>(
  items: T[],
  groupKey: keyof T,
  metadataFn: (grouped: T[]) => M
): (DropdownOption & M)[] {
  const grouped = items.reduce(
    (acc, item) => {
      const key = String(item[groupKey]);
      if (!acc[key]) {
        acc[key] = [];
      }
      acc[key].push(item);
      return acc;
    },
    {} as Record<string, T[]>
  );

  return Object.entries(grouped).map(([value, groupedItems]) => ({
    value,
    label: value,
    ...metadataFn(groupedItems),
  }));
}
