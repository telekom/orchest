import { useMemo } from 'react';

export interface UseSearchFilterOptions<T> {
  data: T[];
  searchTerm: string;
  searchFields: (keyof T)[];
  caseSensitive?: boolean;
  customFilter?: (item: T, searchTerm: string) => boolean;
}

/**
 * Hook for case-insensitive search filtering across multiple fields
 */
export function useSearchFilter<T>(options: UseSearchFilterOptions<T>): T[] {
  const {
    data,
    searchTerm,
    searchFields,
    caseSensitive = false,
    customFilter,
  } = options;

  return useMemo(() => {
    if (!searchTerm || searchTerm.trim() === '') {
      return data;
    }

    const term = caseSensitive ? searchTerm : searchTerm.toLowerCase();

    if (customFilter) {
      return data.filter((item) => customFilter(item, searchTerm));
    }

    return data.filter((item) => {
      return searchFields.some((field) => {
        const value = item[field];

        if (value === null || value === undefined) {
          return false;
        }

        const stringValue = String(value);
        const searchValue = caseSensitive ? stringValue : stringValue.toLowerCase();

        return searchValue.includes(term);
      });
    });
  }, [data, searchTerm, searchFields, caseSensitive, customFilter]);
}
