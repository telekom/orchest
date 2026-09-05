import { useIntersectionLoader } from "@/shared/hooks/useIntersectionLoader";
import { useVirtualizer } from "@tanstack/react-virtual";
import clsx from "clsx";
import React, { ReactNode, useCallback, useMemo, useRef } from "react";
import { Link } from "react-router-dom";
import { DataTableEmptyState } from "./components/DataTableEmptyState/DataTableEmptyState";
import { DataTableHeader } from "./components/DataTableHeader/DataTableHeader";
import { DataTablePagination } from "./components/DataTablePagination/DataTablePagination";
import { InfiniteScrollFooter } from "./components/InfiniteScrollFooter/InfiniteScrollFooter";
import { PaginationLoadingOverlay } from "./components/PaginationLoadingOverlay/PaginationLoadingOverlay";
import { SelectionCheckbox } from "./components/SelectionCheckbox/SelectionCheckbox";
import styles from "./DataTable.module.css";
import type { Column, InfiniteScrollConfig, PaginationConfig, RowSelectionConfig, SortConfig, SortDirection } from "./types";

// Re-export types for convenience
export type { Column, InfiniteScrollConfig, PaginationConfig, RowSelectionConfig, SortConfig, SortDirection };

export interface DataTableProps<T> {
  data: T[];
  columns: Column<T>[];
  keyExtractor: (row: T, index: number) => string;
  loading?: boolean;
  /** Shows full-page skeleton overlay when paginating (keeps existing data visible) */
  isPaginationLoading?: boolean;
  loadingText?: string;
  emptyText?: string;
  onRowClick?: (row: T, event: React.MouseEvent) => void;
  onRowMouseDown?: (row: T, event: React.MouseEvent) => void;
  onRowMouseEnter?: (row: T, event: React.MouseEvent) => void;
  onRowContextMenu?: (row: T, event: React.MouseEvent) => void;
  onRowAuxClick?: (row: T, event: React.MouseEvent) => void;
  /**
   * When provided, each row gets a real `<Link>` overlay so Ctrl/Cmd-click,
   * middle-click, and right-click "Open in new tab" work natively.
   * Prefer this over `onRowClick` for route navigation.
   */
  getRowHref?: (row: T) => string | undefined;
  rowClassName?: (row: T) => string;
  selectedRowKey?: string;
  className?: string;
  containerClassName?: string;
  headerClassName?: string;
  bodyClassName?: string;
  rowHoverEffect?: boolean;
  stickyHeader?: boolean;
  bordered?: boolean;
  maxHeight?: string;
  showCard?: boolean;
  renderActions?: (row: T) => ReactNode;
  actionsHeader?: string;
  actionsAlign?: "left" | "center" | "right";
  pagination?: PaginationConfig;
  /**
   * Enables infinite-scroll mode. Mutually exclusive with `pagination`; when both
   * are provided, `infiniteScroll` wins. Place the table inside a height-constrained
   * container (e.g. `maxHeight="100%"`) so the internal scroll container drives
   * the IntersectionObserver-based load-more.
   */
  infiniteScroll?: InfiniteScrollConfig;
  /** Current sort configuration(s). Enables sort UI on columns with `sortable: true`. Supports multi-sort via array. */
  sortConfig?: SortConfig | SortConfig[];
  /** Called when user clicks a sortable column header. Shift+Click adds multi-sort. */
  onSortChange?: (sort: SortConfig[]) => void;
  enableVirtualization?: boolean;
  estimatedRowHeight?: number;
  overscan?: number;
  /** Enable row selection with checkboxes */
  rowSelection?: RowSelectionConfig<T>;
}

const getAlignClass = (align?: "left" | "center" | "right") => {
  if (align === "center") return styles.textCenter;
  if (align === "right") return styles.textRight;
  return styles.textLeft;
};

const getColumnValue = <T,>(row: T, column: Column<T>): ReactNode => {
  if (column.render) return column.render(row);
  if (column.accessor) return column.accessor(row);

  const value = row[column.key as keyof T];
  if (value == null) return null;
  if (typeof value === 'object') return JSON.stringify(value);
  return String(value);
};

const isColumnVisible = <T,>(column: Column<T>) => column.hidden !== true;

function DataTableComponent<T extends Record<string, unknown>>({
  data,
  columns,
  keyExtractor,
  loading = false,
  isPaginationLoading = false,
  loadingText = "Loading...",
  emptyText = "No data found",
  onRowClick,
  onRowMouseDown,
  onRowMouseEnter,
  onRowContextMenu,
  onRowAuxClick,
  getRowHref,
  rowClassName,
  selectedRowKey,
  className = "",
  containerClassName = "",
  headerClassName = "",
  bodyClassName = "",
  rowHoverEffect = true,
  stickyHeader = false,
  bordered = true,
  maxHeight,
  showCard = false,
  renderActions,
  actionsHeader = "Actions",
  pagination,
  infiniteScroll,
  sortConfig,
  onSortChange,
  enableVirtualization = false,
  estimatedRowHeight = 48,
  overscan = 5,
  rowSelection,
}: DataTableProps<T>) {
  const tableContainerRef = useRef<HTMLDivElement>(null);
  const sentinelRef = useRef<HTMLDivElement>(null);
  const visibleColumns = columns.filter(isColumnVisible);
  // Infinite scroll takes precedence when both are provided; this keeps the
  // pagination footer from rendering alongside the load-more footer.
  const effectivePagination = infiniteScroll ? undefined : pagination;

  // Row selection logic
  const selectableData = useMemo(() => {
    if (!rowSelection?.isRowSelectable) return data;
    return data.filter(rowSelection.isRowSelectable);
  }, [data, rowSelection]);

  const allSelectableKeys = useMemo(() => {
    return selectableData.map((row, index) => keyExtractor(row, index));
  }, [selectableData, keyExtractor]);

  const isRowSelectable = (row: T): boolean => {
    return !rowSelection || !rowSelection.isRowSelectable || rowSelection.isRowSelectable(row);
  };

  const handleSelectAll = useCallback((checked: boolean) => {
    if (!rowSelection) return;
    if (checked) {
      rowSelection.onSelectionChange(allSelectableKeys);
    } else {
      rowSelection.onSelectionChange([]);
    }
  }, [rowSelection, allSelectableKeys]);

  const handleSelectRow = useCallback((rowKey: string, checked: boolean) => {
    if (!rowSelection) return;
    const newSelection = checked
      ? [...rowSelection.selectedRowKeys, rowKey]
      : rowSelection.selectedRowKeys.filter(key => key !== rowKey);
    rowSelection.onSelectionChange(newSelection);
  }, [rowSelection]);

  const isAllSelected = useMemo(() => {
    if (!rowSelection || allSelectableKeys.length === 0) return false;
    return allSelectableKeys.every(key => rowSelection.selectedRowKeys.includes(key));
  }, [rowSelection, allSelectableKeys]);

  const isSomeSelected = useMemo(() => {
    if (!rowSelection || allSelectableKeys.length === 0) return false;
    return rowSelection.selectedRowKeys.length > 0 && !isAllSelected;
  }, [rowSelection, isAllSelected, allSelectableKeys]);

  const totalColumns = visibleColumns.length + (renderActions ? 1 : 0) + (rowSelection ? 1 : 0);

  const rowVirtualizer = useVirtualizer({
    count: data.length,
    getScrollElement: () => tableContainerRef.current,
    estimateSize: () => estimatedRowHeight,
    overscan,
    enabled: enableVirtualization,
  });

  // Infinite-scroll sentinel: only active once we have data and more pages exist.
  // Disabling during the initial load avoids stacked fetches on first render.
  const infiniteScrollEnabled =
    !!infiniteScroll &&
    infiniteScroll.hasNextPage &&
    !infiniteScroll.isFetchingNextPage &&
    !loading &&
    data.length > 0;

  useIntersectionLoader({
    targetRef: sentinelRef,
    rootRef: tableContainerRef,
    enabled: infiniteScrollEnabled,
    onIntersect: infiniteScroll?.onLoadMore ?? (() => {}),
    rootMargin: infiniteScroll?.rootMargin ?? '200px',
  });

  const renderRow = (row: T, index: number) => {
    const rowKey = keyExtractor(row, index);
    const isSelected = selectedRowKey === rowKey;
    const isRowSelected = rowSelection?.selectedRowKeys.includes(rowKey) ?? false;
    const selectable = isRowSelectable(row);
    const rowHref = getRowHref?.(row);
    const isNavigable = Boolean(rowHref) || Boolean(onRowClick);

    return (
      <tr
        key={rowKey}
        className={clsx(
          rowHoverEffect && styles.rowHover,
          isSelected && styles.rowSelected,
          isNavigable && styles.rowClickable,
          rowHref && styles.rowWithHref,
          rowClassName?.(row)
        )}
        onClick={(e) => {
          if (rowHref) return;
          onRowClick?.(row, e);
        }}
        onMouseDown={(e) => onRowMouseDown?.(row, e)}
        onMouseEnter={(e) => onRowMouseEnter?.(row, e)}
        onContextMenu={(e) => onRowContextMenu?.(row, e)}
        onAuxClick={(e) => onRowAuxClick?.(row, e)}
      >
        {rowSelection && (
          <td
            className={clsx(styles.cell, styles.checkboxCell, styles.rowInteractive)}
            style={{ width: rowSelection.checkboxColumnWidth || '36px' }}
            onClick={(e) => e.stopPropagation()}
          >
            <SelectionCheckbox
              checked={isRowSelected}
              onChange={(checked) => handleSelectRow(rowKey, checked)}
              disabled={!selectable}
              aria-label={`Select row ${rowKey}`}
            />
          </td>
        )}
        {visibleColumns.map((column, colIndex) => {
          const hiddenClass = typeof column.hidden === "string" ? column.hidden : "";
          return (
            <td
              key={column.key}
              className={clsx(
                styles.cell,
                getAlignClass(column.align),
                hiddenClass,
                column.className,
                rowHref && styles.cellWithHref
              )}
            >
              {rowHref && (
                <Link
                  to={rowHref}
                  className={styles.rowHref}
                  aria-label={colIndex === 0 ? `Open ${rowKey}` : undefined}
                  tabIndex={-1}
                />
              )}
              <div className={clsx(styles.cellContent, rowHref && styles.cellContentPassthrough)}>
                {getColumnValue(row, column)}
              </div>
            </td>
          );
        })}
        {renderActions && (
          <td
            className={clsx(
              styles.cell,
              styles.textRight,
              styles.nowrap,
              styles.actionsCell,
              styles.rowInteractive
            )}
            onClick={(e) => e.stopPropagation()}
          >
            {renderActions(row)}
          </td>
        )}
      </tr>
    );
  };

  const renderTableBody = () => {
    if (!enableVirtualization) {
      return data.map((row, index) => renderRow(row, index));
    }

    return (
      <>
        {/* INLINE STYLE: TanStack Virtual requires dynamic height calculation */}
        <tr style={{ height: `${rowVirtualizer.getTotalSize()}px` }} />
        {rowVirtualizer.getVirtualItems().map((virtualRow) => {
          const row = data[virtualRow.index];
          return (
            <tr
              key={virtualRow.key}
              className={styles.virtualRow}
              style={{
                transform: `translateY(${virtualRow.start}px)`,
              }}
            >
              <td colSpan={totalColumns} className={styles.virtualCell}>
                <table className={styles.table}>
                  <tbody>
                    {renderRow(row, virtualRow.index)}
                  </tbody>
                </table>
              </td>
            </tr>
          );
        })}
      </>
    );
  };

  const tableContent = (
    <>
      <div
        ref={tableContainerRef}
        className={clsx(
          styles.tableContainer,
          maxHeight && styles.tableContainerFlex,
          data.length === 0 && styles.tableContainerMinHeight,
          isPaginationLoading && styles.tableContainerRelative,
          className
        )}
        {...(maxHeight && { style: { maxHeight } })}
      >
        <DataTableEmptyState
          loading={loading}
          dataLength={data.length}
          loadingText={loadingText}
          emptyText={emptyText}
        />

        {/* Full-page skeleton overlay during pagination */}
        {isPaginationLoading && data.length > 0 && (
          <PaginationLoadingOverlay
            columns={totalColumns}
            rows={Math.min(data.length, pagination?.pageSize || 25)}
          />
        )}

        <table className={styles.table}>
          <DataTableHeader
            columns={visibleColumns}
            renderActions={!!renderActions}
            actionsHeader={actionsHeader}
            stickyHeader={stickyHeader}
            bordered={bordered}
            headerClassName={headerClassName}
            sortConfig={sortConfig}
            onSortChange={onSortChange}
            rowSelection={rowSelection ? {
              isAllSelected,
              isSomeSelected,
              onSelectAll: handleSelectAll,
              checkboxColumnWidth: rowSelection.checkboxColumnWidth,
              selectedCount: rowSelection.selectedRowKeys.length,
              headerActions: rowSelection.headerActions,
            } : undefined}
          />
          <tbody className={clsx(
            styles.tableBody,
            bodyClassName
          )}>
            {renderTableBody()}
          </tbody>
        </table>

        {infiniteScroll && data.length > 0 && (
          <div
            ref={sentinelRef}
            className={styles.infiniteScrollSentinel}
            aria-hidden="true"
          />
        )}
      </div>

      {effectivePagination && (
        <DataTablePagination
          pagination={effectivePagination}
          loading={loading || isPaginationLoading}
        />
      )}

      {infiniteScroll && data.length > 0 && (
        <InfiniteScrollFooter
          config={infiniteScroll}
          disabled={loading}
        />
      )}
    </>
  );

  if (showCard) {
    return (
      <div className={clsx(
        styles.card,
        (pagination || infiniteScroll || maxHeight) && styles.cardWithPagination,
        containerClassName
      )}>
        {tableContent}
      </div>
    );
  }

  return <div className={containerClassName}>{tableContent}</div>;
}

export const DataTable = DataTableComponent as typeof DataTableComponent & {
  displayName: string;
};

DataTable.displayName = "DataTable";
