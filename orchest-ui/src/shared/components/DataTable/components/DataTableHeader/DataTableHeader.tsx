import clsx from "clsx";
import { ArrowDown, ArrowUp, ArrowUpDown } from "lucide-react";
import { ReactNode } from "react";
import type { SortConfig } from "../../types";
import { SelectionCheckbox } from "../SelectionCheckbox/SelectionCheckbox";
import styles from "./DataTableHeader.module.css";

interface DataTableHeaderProps {
  columns: Array<{
    key: string;
    header: string | ReactNode;
    align?: "left" | "center" | "right";
    hidden?: boolean | string;
    headerClassName?: string;
    sortable?: boolean;
    sortKey?: string;
  }>;
  renderActions: boolean;
  actionsHeader: string;
  stickyHeader: boolean;
  bordered: boolean;
  headerClassName: string;
  sortConfig?: SortConfig | SortConfig[];
  onSortChange?: (sort: SortConfig[]) => void;
  rowSelection?: {
    isAllSelected: boolean;
    isSomeSelected: boolean;
    onSelectAll: (checked: boolean) => void;
    checkboxColumnWidth?: string;
    selectedCount?: number;
    headerActions?: React.ReactNode;
  };
}

const getAlignClass = (align?: "left" | "center" | "right") => {
  if (align === "center") return styles.alignCenter;
  if (align === "right") return styles.alignRight;
  return styles.alignLeft;
};

/** Normalize single or array config into a stable array */
function normalizeSortConfig(sortConfig?: SortConfig | SortConfig[]): SortConfig[] {
  if (!sortConfig) return [];
  return Array.isArray(sortConfig) ? sortConfig : [sortConfig];
}

function SortIndicator({ field, sorts }: { field: string; sorts: SortConfig[] }) {
  const index = sorts.findIndex(s => s.field === field);
  if (index === -1) {
    return <ArrowUpDown size={14} className={styles.sortIconIdle} />;
  }
  const isMulti = sorts.length > 1;
  const Icon = sorts[index].direction === "asc" ? ArrowUp : ArrowDown;
  return (
    <span className={styles.sortIndicatorWrap}>
      <Icon size={14} className={styles.sortIconActive} />
      {isMulti && <span className={styles.sortOrder}>{index + 1}</span>}
    </span>
  );
}

export function DataTableHeader({
  columns,
  renderActions,
  actionsHeader,
  stickyHeader,
  bordered,
  headerClassName,
  sortConfig,
  onSortChange,
  rowSelection,
}: DataTableHeaderProps) {
  const sorts = normalizeSortConfig(sortConfig);

  const handleSortClick = (column: typeof columns[number], shiftKey: boolean) => {
    if (!column.sortable || !onSortChange) return;

    const sortField = column.sortKey ?? column.key;
    const existingIndex = sorts.findIndex(s => s.field === sortField);
    const currentSort = existingIndex !== -1 ? sorts[existingIndex] : null;

    if (shiftKey) {
      // Multi-sort: Shift+Click adds/toggles a secondary sort
      const newSorts = [...sorts];
      if (existingIndex !== -1) {
        // Toggle direction of existing sort
        newSorts[existingIndex] = {
          ...newSorts[existingIndex],
          direction: newSorts[existingIndex].direction === 'asc' ? 'desc' : 'asc',
        };
      } else {
        // Add new sort column
        newSorts.push({ field: sortField, direction: 'asc' });
      }
      onSortChange(newSorts);
    } else {
      // Single click: replace all sorts with this one
      // Cycle: no sort → asc → desc → asc → desc → ...
      const nextDirection = currentSort?.direction === 'asc' ? 'desc' : 'asc';
      onSortChange([{ field: sortField, direction: nextDirection }]);
    }
  };

  return (
    <thead
      className={clsx(
        styles.thead,
        stickyHeader && styles.theadSticky,
        headerClassName
      )}
    >
      <tr className={clsx(bordered && styles.row)}>
        {rowSelection && (
          <th
            className={clsx(styles.headerCell, styles.checkboxCell)}
            style={{ width: rowSelection.checkboxColumnWidth || '36px' }}
          >
            <SelectionCheckbox
              checked={rowSelection.isAllSelected}
              indeterminate={rowSelection.isSomeSelected}
              onChange={rowSelection.onSelectAll}
              aria-label="Select all rows"
            />
          </th>
        )}
        {columns.map((column) => {
          const hiddenClass = typeof column.hidden === "string" ? column.hidden : "";
          const isSortable = column.sortable && !!onSortChange;
          const sortField = column.sortKey ?? column.key;
          const activeSortEntry = sorts.find(s => s.field === sortField);

          return (
            <th
              key={column.key}
              className={clsx(
                styles.headerCell,
                getAlignClass(column.align),
                hiddenClass,
                column.headerClassName,
                isSortable && styles.sortableCell
              )}
              onClick={isSortable ? (e) => handleSortClick(column, e.shiftKey) : undefined}
              role={isSortable ? "columnheader" : undefined}
              aria-sort={
                isSortable && activeSortEntry
                  ? activeSortEntry.direction === "asc" ? "ascending" : "descending"
                  : isSortable ? "none" : undefined
              }
              tabIndex={isSortable ? 0 : undefined}
              onKeyDown={isSortable ? (e) => {
                if (e.key === "Enter" || e.key === " ") {
                  e.preventDefault();
                  handleSortClick(column, e.shiftKey);
                }
              } : undefined}
            >
              {isSortable ? (
                <span className={styles.sortableContent}>
                  <span>{column.header}</span>
                  <SortIndicator field={sortField} sorts={sorts} />
                </span>
              ) : (
                column.header
              )}
            </th>
          );
        })}
        {renderActions && (
          <th
            className={clsx(
              styles.headerCell,
              styles.alignRight,
              styles.actionsCell,
              actionsHeader ? styles.actionsCellWithPadding : styles.actionsCellNoPadding
            )}
          >
            {rowSelection?.selectedCount && rowSelection.selectedCount > 0 ? (
              rowSelection.headerActions || (
                <span className={styles.selectionCount}>
                  {rowSelection.selectedCount} selected
                </span>
              )
            ) : (
              actionsHeader
            )}
          </th>
        )}
      </tr>
    </thead>
  );
}
