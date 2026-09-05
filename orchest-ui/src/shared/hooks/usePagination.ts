import { useCallback, useState, useEffect, useRef } from "react";

export interface UsePaginationOptions {
  initialPageSize?: number;
  initialPageNumber?: number;
  onPageChange?: (pageNumber: number, pageSize: number) => void;
  loading?: boolean;
}

export interface UsePaginationReturn {
  pageNumber: number;
  pageSize: number;
  totalPages: number;
  handlePageChange: (direction: "next" | "prev" | number) => void;
  handlePageSizeChange: (newPageSize: number) => void;
  setTotalPages: (total: number) => void;
  reset: () => void;
  setPageNumber: (page: number) => void;
  setPageSize: (size: number) => void;
}

export function usePagination({
  initialPageSize = 25,
  initialPageNumber = 0,
  onPageChange,
  loading = false,
}: UsePaginationOptions = {}): UsePaginationReturn {
  const [pageNumber, setPageNumber] = useState(initialPageNumber);
  const [pageSize, setPageSize] = useState(initialPageSize);
  const [totalPages, setTotalPages] = useState(0);

  // Use ref to stabilize callback and prevent unnecessary re-renders
  const onPageChangeRef = useRef(onPageChange);

  useEffect(() => {
    onPageChangeRef.current = onPageChange;
  }, [onPageChange]);

  const handlePageChange = useCallback(
    (direction: "next" | "prev" | number) => {
      if (loading) return;

      let nextPage: number;

      if (typeof direction === "number") {
        nextPage = direction;
      } else if (direction === "next") {
        nextPage = pageNumber + 1;
      } else if (direction === "prev") {
        nextPage = pageNumber - 1;
      } else {
        return;
      }

      if (nextPage >= 0 && nextPage < totalPages) {
        setPageNumber(nextPage);
        onPageChangeRef.current?.(nextPage, pageSize);
      }
    },
    [pageNumber, totalPages, pageSize, loading]
  );

  const handlePageSizeChange = useCallback(
    (newPageSize: number) => {
      setPageSize(newPageSize);
      setPageNumber(0);
      onPageChangeRef.current?.(0, newPageSize);
    },
    []
  );

  const reset = useCallback(() => {
    setPageNumber(initialPageNumber);
    setPageSize(initialPageSize);
    setTotalPages(0);
  }, [initialPageNumber, initialPageSize]);

  return {
    pageNumber,
    pageSize,
    totalPages,
    handlePageChange,
    handlePageSizeChange,
    setTotalPages,
    reset,
    setPageNumber,
    setPageSize,
  };
}
