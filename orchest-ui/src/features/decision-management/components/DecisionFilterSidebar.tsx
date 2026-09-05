import { Separator } from '@/design-system/components/ui/separator';
import { FilterSidebar } from '@/shared/components';
import styles from '@/shared/components/FilterSidebar/FilterSidebarODS.module.css';
import { createGroupedOptions, formatLocalDate, parseLocalDate, useDropdownOptions, useVersionCalculation } from '@/shared/hooks';
import type { UrlHistoryMode } from '@/shared/url-state';
import { useDebouncedUrlFilter } from '@/shared/url-state';
import { convertFilterValue } from '@/shared/utils/filterUtils';
import { DateRangePicker } from '@/design-system/components/ui/date-range-picker';
import { Select } from '@/design-system/components/ui/select';
import { SearchInput } from '@/design-system/components/ui/search-input';
import React, { useCallback } from 'react';
import type { DecisionFilters } from '../types';
import { useDecisionDefinitions } from '../hooks/useDecisionDefinitions';

export interface DecisionFilterSidebarBindings {
  decisionFilters: DecisionFilters;
  setDecisionFilters: (filters: Partial<DecisionFilters>, mode?: UrlHistoryMode) => void;
  clearDecisionFilters: () => void;
  hasActiveDecisionFilters: boolean;
}

interface DecisionFilterSidebarProps extends DecisionFilterSidebarBindings {
  isCollapsed: boolean;
  onToggleCollapse: () => void;
  onFilterChange?: () => void;
}

interface DecisionDefinition {
  definitionId: string;
  resourceXML: string;
  version: number;
}

const DecisionFilterSidebar: React.FC<DecisionFilterSidebarProps> = ({
  isCollapsed,
  onToggleCollapse,
  decisionFilters,
  setDecisionFilters,
  clearDecisionFilters,
  hasActiveDecisionFilters,
  onFilterChange,
}) => {
  const searchFilters = {
    searchText: decisionFilters.searchText ?? '',
  };

  const { localValue: searchValue, onChange: onSearchChange, clear: clearSearch } = useDebouncedUrlFilter({
    filters: searchFilters,
    setFilters: (patch) => {
      setDecisionFilters({
        searchText: (patch.searchText as string) || null,
      }, 'replace');
    },
    key: 'searchText',
    delayMs: 400,
  });

  const { decisionDefinitions, isLoading: loading } = useDecisionDefinitions();

  const dmnOptions = useDropdownOptions({
    data: decisionDefinitions as DecisionDefinition[] | undefined,
    transform: (items) =>
      createGroupedOptions(items, 'definitionId', (grouped) => ({
        versions: grouped.map((g) => g.version),
      })),
    includeAllOption: true,
    allOption: { value: 'all', label: 'All DMNs' },
  });

  const { versionOptions, calculateLatestVersion } = useVersionCalculation({
    definitions: decisionDefinitions,
    selectedId: decisionFilters?.decisionId,
  });

  const odsDmnOptions = dmnOptions;
  const odsVersionOptions = versionOptions;

  const handleSearchChange = useCallback((value: string) => {
    onSearchChange(value);
  }, [onSearchChange]);

  const handleSearchClear = useCallback(() => {
    clearSearch();
    setDecisionFilters({ searchText: null }, 'replace');
  }, [clearSearch, setDecisionFilters]);

  const handleDecisionIDChange = useCallback((value: string) => {
    
    if (!value) return;

    const decisionIdValue = convertFilterValue(value);
    const latestVersion = calculateLatestVersion(decisionIdValue);

    setDecisionFilters({
      decisionId: decisionIdValue,
      version: decisionIdValue ? latestVersion : null,
    });
    onFilterChange?.();
  }, [calculateLatestVersion, setDecisionFilters, onFilterChange]);

  const handleVersionChange = useCallback((value: string) => {
    
    if (!value) return;

    setDecisionFilters({ version: convertFilterValue(value) });
    onFilterChange?.();
  }, [setDecisionFilters, onFilterChange]);

  const handleDateRangeChange = useCallback((from: Date | null, to: Date | null) => {
    setDecisionFilters({
      from: from ? formatLocalDate(from) : null,
      to: to ? formatLocalDate(to) : null,
    });
  }, [setDecisionFilters]);

  const handleDateRangeApply = useCallback(
    (from: Date | null, to: Date | null) => {
      setDecisionFilters({
        from: from ? formatLocalDate(from) : null,
        to: to ? formatLocalDate(to) : null,
      });
      onFilterChange?.();
    },
    [setDecisionFilters, onFilterChange],
  );

  const handleDateRangeClear = useCallback(() => {
    setDecisionFilters({ from: null, to: null, timezone: null });
    onFilterChange?.();
  }, [setDecisionFilters, onFilterChange]);

  const handleTimezoneChange = useCallback((tz: string) => {
    setDecisionFilters({ timezone: tz });
    onFilterChange?.();
  }, [setDecisionFilters, onFilterChange]);

  const handleClearFilters = useCallback(() => {
    clearSearch();
    clearDecisionFilters();
  }, [clearDecisionFilters, clearSearch]);

  return (
    <FilterSidebar
      title="Decision Filters"
      isCollapsed={isCollapsed}
      onToggleCollapse={onToggleCollapse}
      onClearFilters={handleClearFilters}
      hasActiveFilters={hasActiveDecisionFilters}
      onFilterChange={onFilterChange}
      headerIcon="filter"
    >
      <div className={styles.odsContainer}>
        <div className={styles.filterInputWrapper}>
          <SearchInput
            labelText="Search decisions..."
            value={searchValue}
            onChange={handleSearchChange}
            onClear={handleSearchClear}
            size="sm"
            showResultList={false}
          />
        </div>

        <Separator />

        <div className={styles.filterGroup}>
          <div className={styles.filterInputWrapper}>
            <div className={styles.dropdownWrapper}>
              <Select
                label="DMN IDs"
                value={decisionFilters?.decisionId || 'all'}
                items={odsDmnOptions}
                onValueChange={handleDecisionIDChange}
                disabled={loading}
                size="sm"
                className={styles.dropdown}
              />
            </div>
          </div>

          <div className={styles.filterInputWrapper}>
            <div className={styles.dropdownWrapper}>
              <Select
                key={`version-${decisionFilters?.decisionId || 'none'}`}
                label="Version"
                value={decisionFilters?.version || 'all'}
                items={odsVersionOptions}
                onValueChange={handleVersionChange}
                disabled={loading || !decisionFilters?.decisionId}
                size="sm"
                className={styles.dropdown}
              />
            </div>
          </div>
        </div>

        <Separator />

        <div className={styles.filterGroup}>
          <div className={styles.filterInputWrapper}>
            <DateRangePicker
              from={decisionFilters?.from ? parseLocalDate(decisionFilters.from) : null}
              to={decisionFilters?.to ? parseLocalDate(decisionFilters.to) : null}
              onChange={handleDateRangeChange}
              onTimezoneChange={handleTimezoneChange}
              onApply={handleDateRangeApply}
              onClear={handleDateRangeClear}
              showClear={!!(decisionFilters?.from || decisionFilters?.to)}
            />
          </div>
        </div>
      </div>
    </FilterSidebar>
  );
};

export default React.memo(DecisionFilterSidebar);
