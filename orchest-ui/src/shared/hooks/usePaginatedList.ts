import { useCallback, useEffect, useMemo, useRef } from "react";
import { usePagination } from "./usePagination";

interface UsePaginatedListConfig<T = unknown> {
  initialPageSize?: number;
  filterDependencies: unknown[];
  data?: T[];
}

/**
 * Hook for paginated lists with automatic page reset on filter changes.
 * Supports both server-side and client-side pagination.
 */
export function usePaginatedList<T = unknown>({
  initialPageSize = 25,
  filterDependencies,
  data,
}: UsePaginatedListConfig<T>) {
  const {
    pageNumber,
    pageSize,
    totalPages,
    handlePageChange,
    handlePageSizeChange,
    setTotalPages,
    setPageNumber,
  } = usePagination({
    initialPageSize,
    initialPageNumber: 0,
    loading: false,
  });

  const filterKey = filterDependencies.length === 1 && typeof filterDependencies[0] === 'string'
    ? filterDependencies[0]
    : JSON.stringify(filterDependencies);
  const isInitialMount = useRef(true);

  useEffect(() => {
    if (isInitialMount.current) {
      isInitialMount.current = false;
      return;
    }
    setPageNumber(0);
  }, [filterKey, setPageNumber]);

  const paginatedData = useMemo(() => {
    if (!data) {
      return undefined;
    }

    const start = pageNumber * pageSize;
    const end = start + pageSize;
    return data.slice(start, end) as T[];
  }, [data, pageNumber, pageSize]);

  useEffect(() => {
    if (data) {
      const calculatedTotalPages = Math.ceil(data.length / pageSize);
      setTotalPages(calculatedTotalPages);
    }
  }, [data, pageSize, setTotalPages]);

  const resetToFirstPage = useCallback(() => {
    setPageNumber(0);
  }, [setPageNumber]);

  const totalElements = data ? data.length : undefined;

  return {
    pageNumber,
    pageSize,
    totalPages,
    handlePageChange,
    handlePageSizeChange,
    setTotalPages,
    setPageNumber,
    resetToFirstPage,
    paginatedData,
    totalElements,
  };
}
