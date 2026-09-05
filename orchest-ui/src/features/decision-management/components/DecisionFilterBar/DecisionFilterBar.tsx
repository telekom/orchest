import { DateRangePicker } from '@/design-system/components/ui/date-range-picker';
import { Select } from '@/design-system/components/ui/select';
import { SearchInput } from '@/design-system/components/ui/search-input';
import { createGroupedOptions, formatLocalDate, parseLocalDate, useDropdownOptions, useVersionCalculation } from '@/shared/hooks';
import type { UrlHistoryMode } from '@/shared/url-state';
import { useDebouncedUrlFilter } from '@/shared/url-state';
import { convertFilterValue } from '@/shared/utils/filterUtils';
import { X } from 'lucide-react';
import React, { useCallback } from 'react';
import type { DecisionFilters } from '../../types';
import { useDecisionDefinitions } from '../../hooks/useDecisionDefinitions';
import styles from './DecisionFilterBar.module.css';

export interface DecisionFilterBarBindings {
  decisionFilters: DecisionFilters;
  setDecisionFilters: (filters: Partial<DecisionFilters>, mode?: UrlHistoryMode) => void;
  clearDecisionFilters: () => void;
  hasActiveDecisionFilters: boolean;
}

interface DecisionFilterBarProps extends DecisionFilterBarBindings {
  onFilterChange?: () => void;
}

interface DecisionDefinition {
  definitionId: string;
  resourceXML: string;
  version: number;
}

const DecisionFilterBar: React.FC<DecisionFilterBarProps> = ({
  decisionFilters,
  setDecisionFilters,
  clearDecisionFilters,
  hasActiveDecisionFilters,
  onFilterChange,
}) => {
  const { localValue: searchValue, onChange: onSearchChange, clear: clearSearch } = useDebouncedUrlFilter({
    filters: decisionFilters,
    setFilters: (patch) => {
      setDecisionFilters({ searchText: (patch.searchText as string) || null }, 'replace');
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
    setDecisionFilters({ decisionId: decisionIdValue, version: decisionIdValue ? latestVersion : null });
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

  const handleClearAll = useCallback(() => {
    clearSearch();
    clearDecisionFilters();
  }, [clearSearch, clearDecisionFilters]);

  return (
    <div className={styles.wrapper} role="search" aria-label="Decision filters">
      <div className={styles.filterItemSearch}>
        <SearchInput
          labelText="Search…"
          value={searchValue}
          onChange={handleSearchChange}
          onClear={handleSearchClear}
          size="sm"
          showResultList={false}
        />
      </div>

      <div className={styles.filterItem}>
        <Select
          value={decisionFilters?.decisionId || 'all'}
          items={dmnOptions}
          onValueChange={handleDecisionIDChange}
          disabled={loading}
          placeholder="All DMNs"
          size="sm"
        />
      </div>

      <div className={styles.filterItem}>
        <Select
          key={`version-${decisionFilters?.decisionId || 'none'}`}
          value={decisionFilters?.version || 'all'}
          items={versionOptions}
          onValueChange={handleVersionChange}
          disabled={loading || !decisionFilters?.decisionId}
          placeholder="Version"
          size="sm"
        />
      </div>

      <div className={styles.filterItemDate}>
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

      <span className={styles.spacer} />

      {hasActiveDecisionFilters && (
        <button type="button" className={styles.clearAllButton} onClick={handleClearAll}>
          <X size={12} />
          Clear all
        </button>
      )}
    </div>
  );
};

export default React.memo(DecisionFilterBar);