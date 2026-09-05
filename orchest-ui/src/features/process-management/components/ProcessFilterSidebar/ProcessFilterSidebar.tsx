import { Separator } from '@/design-system/components/ui/separator';
import { useAuth, UserRoles } from '@/shared/auth';
import { FilterSidebar } from '@/shared/components';
import styles from '@/shared/components/FilterSidebar/FilterSidebarODS.module.css';
import { FilterValue, ProcessStatus } from '@/shared/constants/status';
import type { ProcessFilters } from '@/shared/types/listFilters';
import { createUniqueOptions, formatLocalDate, parseLocalDate, useDropdownOptions, useVersionCalculation } from '@/shared/hooks';
import type { UrlHistoryMode } from '@/shared/url-state';
import { useDebouncedUrlFilter } from '@/shared/url-state';
import { convertFilterValue } from '@/shared/utils/filterUtils';
import { Combobox } from '@/design-system/components/ui/combobox';
import { DateRangePicker } from '@/design-system/components/ui/date-range-picker';
import { Select } from '@/design-system/components/ui/select';
import { SearchInput } from '@/design-system/components/ui/search-input';

import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { ALL_PROCESSES_OPTION } from '../../constants/filterOptions';
import { useProcessDefinitions } from '../../hooks/useProcessDefinitions';

export interface ProcessFilterSidebarBindings {
  processFilters: ProcessFilters;
  setProcessFilters: (filters: Partial<ProcessFilters>, mode?: UrlHistoryMode) => void;
  clearProcessFilters: () => void;
  hasActiveProcessFilters: boolean;
}

interface ProcessFilterSidebarProps extends ProcessFilterSidebarBindings {
  isCollapsed: boolean;
  onToggleCollapse: () => void;
  onClearFilters?: () => void;
  onFilterChange?: () => void;
  onExport?: () => void;
  isExporting?: boolean;
  canExport?: boolean;
  stats?: {
    total: number;
    completed: number;
    running: number;
    hold: number;
    cancelled: number;
    failed: number;
    incident: number;
  };
}

const STATUS_OPTIONS = [
  { label: 'All Status', value: FilterValue.ALL },
  { label: 'Running', value: ProcessStatus.RUNNING },
  { label: 'Completed', value: ProcessStatus.COMPLETED },
  { label: 'Failed', value: ProcessStatus.FAILED },
  { label: 'Hold', value: ProcessStatus.HOLD },
  { label: 'Incident', value: ProcessStatus.INCIDENT },
  { label: 'Cancelled', value: ProcessStatus.CANCELLED }
];

const ProcessFilterSidebar: React.FC<ProcessFilterSidebarProps> = ({
  isCollapsed,
  onToggleCollapse,
  processFilters,
  setProcessFilters,
  clearProcessFilters,
  hasActiveProcessFilters,
  onClearFilters,
  onFilterChange,
  onExport,
  isExporting = false,
  canExport = true,
}) => {
  const { hasRole } = useAuth();
  const showExportButton = hasRole(UserRoles.ADMIN);

  const { localValue: searchValue, onChange: onSearchChange, clear: clearSearch } = useDebouncedUrlFilter({
    filters: processFilters,
    setFilters: setProcessFilters,
    key: 'searchText',
    delayMs: 400,
  });

  const { processDefinitions, isLoading: loading } = useProcessDefinitions();

  const processOptions = useDropdownOptions({
    data: processDefinitions,
    transform: (items) => createUniqueOptions(items, 'definitionId'),
    includeAllOption: true,
    allOption: ALL_PROCESSES_OPTION,
  });

  const { versionOptions, calculateLatestVersion } = useVersionCalculation({
    definitions: processDefinitions,
    selectedId: processFilters.process,
  });

  const odsProcessOptions = processOptions;
  const odsVersionOptions = versionOptions;

  const processComboboxItems = useMemo(
    () =>
      odsProcessOptions.map((opt) => ({
        id: opt.id,
        value: opt.value,
        label: opt.label,
      })),
    [odsProcessOptions],
  );

  const processSelectDefaultValue = processFilters.process || ALL_PROCESSES_OPTION.value;
  const processComboboxMountKey = `pf-process-${processSelectDefaultValue}`;

  const handleSearchChange = useCallback((value: string) => {
    onSearchChange(value);
  }, [onSearchChange]);

  const handleSearchClear = useCallback(() => {
    clearSearch();
  }, [clearSearch]);

  const handleProcessComboboxSelect = useCallback(
    (selectedLabel?: string, selectedValue?: string) => {
      const raw = selectedValue ?? selectedLabel ?? '';
      if (raw === '') {
        setProcessFilters({ process: null, version: null });
        onFilterChange?.();
        return;
      }

      const processValue = convertFilterValue(raw);
      const latestVersion = calculateLatestVersion(processValue);

      setProcessFilters({
        process: processValue,
        version: processValue ? latestVersion : null,
      });
      onFilterChange?.();
    },
    [calculateLatestVersion, setProcessFilters, onFilterChange],
  );

  const handleVersionChange = useCallback((value: string) => {
    
    if (!value) return;

    setProcessFilters({ version: convertFilterValue(value) });
    onFilterChange?.();
  }, [setProcessFilters, onFilterChange]);

  const handleStatusFilter = useCallback((value: string) => {
    
    if (!value) return;

    setProcessFilters({ status: convertFilterValue(value) });
    onFilterChange?.();
  }, [setProcessFilters, onFilterChange]);

  const [draftFrom, setDraftFrom] = useState<Date | null>(
    processFilters.from ? parseLocalDate(processFilters.from) : null
  );
  const [draftTo, setDraftTo] = useState<Date | null>(
    processFilters.to ? parseLocalDate(processFilters.to) : null
  );
  const [draftTimezone, setDraftTimezone] = useState<string>(processFilters.timezone || 'local');

  // Keep calendar drafts aligned when URL filters clear/change outside this sidebar.
  useEffect(() => {
    setDraftFrom(processFilters.from ? parseLocalDate(processFilters.from) : null);
    setDraftTo(processFilters.to ? parseLocalDate(processFilters.to) : null);
  }, [processFilters.from, processFilters.to]);

  useEffect(() => {
    setDraftTimezone(processFilters.timezone || 'local');
  }, [processFilters.timezone]);

  const handleApplyDateFilter = useCallback(
    (appliedFrom?: Date | null, appliedTo?: Date | null) => {
      const nextFrom = appliedFrom !== undefined ? appliedFrom : draftFrom;
      const nextTo = appliedTo !== undefined ? appliedTo : draftTo;
      if (appliedFrom !== undefined) setDraftFrom(appliedFrom);
      if (appliedTo !== undefined) setDraftTo(appliedTo);
      const from = nextFrom ? formatLocalDate(nextFrom) : null;
      const to = nextTo ? formatLocalDate(nextTo) : null;
      setProcessFilters({ from, to, timezone: draftTimezone });
      onFilterChange?.();
    },
    [draftFrom, draftTo, draftTimezone, setProcessFilters, onFilterChange],
  );

  const handleClearDateFilter = useCallback(() => {
    setDraftFrom(null);
    setDraftTo(null);
    setProcessFilters({ from: null, to: null, timezone: null });
    onFilterChange?.();
  }, [setProcessFilters, onFilterChange]);

  const handleClearFilters = useCallback(() => {
    setDraftFrom(null);
    setDraftTo(null);
    setDraftTimezone('local');
    clearSearch();
    onClearFilters?.();
    clearProcessFilters();
  }, [onClearFilters, clearProcessFilters, clearSearch]);

  return (
    <FilterSidebar
      title="Process Filters"
      isCollapsed={isCollapsed}
      onToggleCollapse={onToggleCollapse}
      onClearFilters={handleClearFilters}
      hasActiveFilters={hasActiveProcessFilters}
      onFilterChange={onFilterChange}
      headerIcon="filter"
      onExport={showExportButton && onExport ? onExport : undefined}
      isExporting={isExporting}
      canExport={canExport}
    >
      <div className={styles.odsContainer}>
        <div className={styles.filterInputWrapper}>
          <SearchInput
            labelText="Search processes..."
            value={searchValue}
            onChange={handleSearchChange}
            onClear={handleSearchClear}
            size="sm"
            showResultList={false}
          />
        </div>

        <Separator />

        <div className={styles.filterInputWrapper}>
          <div className={styles.dropdownWrapper}>
            <Select
              label="Status"
              value={processFilters.status || FilterValue.ALL}
              items={STATUS_OPTIONS}
              onValueChange={handleStatusFilter}
              size="sm"
              className={styles.dropdown}
            />
          </div>
        </div>

        <Separator />

        <div className={styles.filterGroup}>
          <div className={styles.filterInputWrapper}>
            <div className={styles.dropdownWrapper}>
              <Combobox
                key={processComboboxMountKey}
                label="Process"
                defaultValue={processSelectDefaultValue}
                items={processComboboxItems}
                onSelectValue={handleProcessComboboxSelect}
                disabled={loading}
                loading={loading}
                noItemsMessage="No processes found"
                placeholderText="Search or select process"
                size="sm"
                editable={false}
                filterOptions
                showExpandIcon
                className={`${styles.dropdown} ${styles.processCombobox}`}
              />
            </div>
          </div>

          <div className={styles.filterInputWrapper}>
            <div className={styles.dropdownWrapper}>
              <Select
                key={`version-${processFilters.process || 'none'}`}
                label="Version"
                value={processFilters.version || 'all'}
                items={odsVersionOptions}
                onValueChange={handleVersionChange}
                disabled={!processFilters.process || loading}
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
              from={draftFrom}
              to={draftTo}
              onChange={(f, t) => { setDraftFrom(f); setDraftTo(t); }}
              onTimezoneChange={(tz) => setDraftTimezone(tz === 'local' ? 'local' : tz)}
              onApply={handleApplyDateFilter}
              onClear={handleClearDateFilter}
              showClear={!!(processFilters.from || processFilters.to)}
              timezone={draftTimezone}
            />
          </div>
        </div>
      </div>
    </FilterSidebar>
  );
};

export default React.memo(ProcessFilterSidebar);
