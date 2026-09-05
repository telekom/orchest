import { ScrollArea } from '@/design-system/components/ui/scroll-area';
import { useDebouncedCallback } from '@/shared/hooks';
import clsx from 'clsx';
import React, { ReactNode, useCallback, useEffect, useState } from 'react';
import styles from './FilterSidebar.module.css';
import { FilterSidebarActions } from './components/FilterSidebarActions/FilterSidebarActions';
import { FilterSidebarCollapsed } from './components/FilterSidebarCollapsed/FilterSidebarCollapsed';
import { FilterSidebarHeader } from './components/FilterSidebarHeader/FilterSidebarHeader';
import { FilterSidebarSearch } from './components/FilterSidebarSearch/FilterSidebarSearch';

export interface FilterSidebarProps {
  title: string;
  isCollapsed: boolean;
  onToggleCollapse: () => void;
  searchText?: string;
  searchPlaceholder?: string;
  onSearchChange?: (value: string) => void;
  onClearFilters?: () => void;
  hasActiveFilters?: boolean;
  searchDebounce?: number;
  children?: ReactNode;
  onFilterChange?: () => void;
  actionSlot?: ReactNode;
  collapsedSlot?: ReactNode;
  headerIcon?: string;
  onExport?: () => void;
  isExporting?: boolean;
  canExport?: boolean;
}

export const FilterSidebar: React.FC<FilterSidebarProps> = ({
  title,
  isCollapsed,
  onToggleCollapse,
  searchText = '',
  searchPlaceholder = 'Search...',
  onSearchChange,
  onClearFilters,
  hasActiveFilters = false,
  searchDebounce = 300,
  children,
  onFilterChange,
  actionSlot,
  collapsedSlot,
  headerIcon,
  onExport,
  isExporting = false,
  canExport = true,
}) => {
  const [localSearchText, setLocalSearchText] = useState(searchText);

  useEffect(() => {
    setLocalSearchText(searchText);
  }, [searchText]);

  const debouncedSearchChange = useDebouncedCallback(
    (value: string) => {
      onSearchChange?.(value.trim());
      onFilterChange?.();
    },
    searchDebounce,
    [onSearchChange, onFilterChange]
  );

  const handleSearchInputChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const newValue = e.target.value;
    setLocalSearchText(newValue);
    debouncedSearchChange(newValue);
  }, [debouncedSearchChange]);

  return (
    <div
      className={clsx(
        styles.sidebar,
        isCollapsed ? styles.sidebarCollapsed : styles.sidebarExpanded
      )}
    >
      {!isCollapsed && (
        <>
          <FilterSidebarHeader title={title} onToggle={onToggleCollapse} headerIcon={headerIcon} />

          <ScrollArea className={styles.content}>
            <div className={styles.contentInner}>
              {onSearchChange && (
                <FilterSidebarSearch
                  value={localSearchText}
                  placeholder={searchPlaceholder}
                  onChange={handleSearchInputChange}
                />
              )}
              {children}
            </div>
          </ScrollArea>

          {onClearFilters && (
            <FilterSidebarActions
              hasActiveFilters={hasActiveFilters}
              onClear={onClearFilters}
              onExport={onExport}
              isExporting={isExporting}
              canExport={canExport}
              leftSlot={actionSlot}
            />
          )}
        </>
      )}

      {isCollapsed && (
        <>
          {collapsedSlot ? (
            collapsedSlot
          ) : (
            <FilterSidebarCollapsed
              hasActiveFilters={hasActiveFilters}
              onToggle={onToggleCollapse}
              onClear={onClearFilters}
              onExport={onExport}
              isExporting={isExporting}
              canExport={canExport}
            />
          )}
        </>
      )}
    </div>
  );
};