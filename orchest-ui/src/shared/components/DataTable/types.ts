import { ReactNode } from "react";

export interface Column<T> {
  key: string;
  header: string | ReactNode;
  accessor?: (row: T) => ReactNode;
  render?: (row: T) => ReactNode;
  className?: string;
  headerClassName?: string;
  width?: string;
  minWidth?: string;
  align?: "left" | "center" | "right";
  hidden?: boolean | string;
  /** Enable server-side sorting for this column. The `sortKey` (or `key`) is sent to the sort callback. */
  sortable?: boolean;
  /** Override the key sent to the sort callback. Defaults to `key`. Useful when API field names differ from column keys. */
  sortKey?: string;
}

export type SortDirection = 'asc' | 'desc';

export interface SortConfig {
  field: string;
  direction: SortDirection;
}

/** Convert a SortConfig to the API sort parameter format: "+field" (asc) or "-field" (desc) */
export function toSortParam(sort: SortConfig): string {
  return `${sort.direction === 'desc' ? '-' : '+'}${sort.field}`;
}

/** Convert multiple SortConfigs to a comma-separated API sort parameter */
export function toMultiSortParam(sorts: SortConfig[]): string {
  return sorts.map(toSortParam).join(',');
}

export interface PaginationConfig {
  currentPage: number;
  totalPages: number;
  totalElements: number;
  pageSize: number;
  pageSizeOptions?: readonly number[] | number[];
  onPageChange?: (direction: 'next' | 'prev' | number) => void;
  onPageSizeChange?: (pageSize: number) => void;
}

export interface InfiniteScrollConfig {
  /** Whether more rows can be loaded from the server. */
  hasNextPage: boolean;
  /** True while the next page is being fetched. */
  isFetchingNextPage: boolean;
  /** Called when the load-more sentinel scrolls into view, or when the user clicks the manual load button. */
  onLoadMore: () => void;
  /** Total number of rows that match the current filters on the server. */
  totalElements: number;
  /** Number of rows currently loaded into the client. */
  loadedElements: number;
  /** IntersectionObserver rootMargin used to trigger the next page early. Defaults to "200px". */
  rootMargin?: string;
  /** Localized label shown next to the loaded/total counter. Defaults to "loaded". */
  loadedLabel?: string;
  /** Optional override for the "load more" button label. Defaults to "Load more". */
  loadMoreLabel?: string;
  /** Optional override for the "all loaded" message. Defaults to "All records loaded." */
  endOfListLabel?: string;
}

export interface RowSelectionConfig<T = unknown> {
  /** Currently selected row keys */
  selectedRowKeys: string[];
  /** Callback when selection changes */
  onSelectionChange: (selectedKeys: string[]) => void;
  /** Optional: Check if a row is selectable (default: all rows are selectable) */
  isRowSelectable?: (row: T) => boolean;
  /** Optional: Custom checkbox column width */
  checkboxColumnWidth?: string;
  /** Optional: Custom actions to render in header when rows are selected */
  headerActions?: React.ReactNode;
}
