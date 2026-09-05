import { Combobox } from '@/design-system/components/ui/combobox';
import { DateRangePicker } from '@/design-system/components/ui/date-range-picker';
import { Select } from '@/design-system/components/ui/select';
import { useProcessDefinitions } from '@/features/process-management/hooks/useProcessDefinitions';
import { createUniqueOptions, formatLocalDate, parseLocalDate } from '@/shared/hooks';
import type { UrlHistoryMode } from '@/shared/url-state';
import type { AlertListStateFilter } from '@/shared/url-state/configs/alertListUrlState';
import { X } from 'lucide-react';
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import type { AlertListUrlState } from '../../hooks/useAlertListUrlState';
import styles from './AlertFilterBar.module.css';

const ALL_VALUE = 'all';

const STATE_OPTIONS = [
  { label: 'Firing', value: 'FIRING' },
  { label: 'Silenced', value: 'SILENCED' },
  { label: 'Disabled', value: 'DISABLED' },
  { label: 'Acknowledged', value: 'ACKNOWLEDGED' },
  { label: 'Resolved', value: 'RESOLVED' },
];

interface AlertFilterBarProps {
  filters: AlertListUrlState;
  setFilters: (filters: Partial<AlertListUrlState>, mode?: UrlHistoryMode) => void;
  clearFilters: (mode?: UrlHistoryMode) => void;
  hasActiveAlertFilters: boolean;
}

const AlertFilterBar: React.FC<AlertFilterBarProps> = ({
  filters,
  setFilters,
  clearFilters,
  hasActiveAlertFilters,
}) => {
  const { processDefinitions, isLoading } = useProcessDefinitions();

  const processItems = useMemo(
    () => [
      { value: ALL_VALUE, label: 'All Processes' },
      ...createUniqueOptions(processDefinitions, 'definitionId'),
    ],
    [processDefinitions],
  );

  const selectedProcess = filters.processDefinitionId || ALL_VALUE;
  const processComboboxKey = `alert-process-${selectedProcess}`;

  const [draftFrom, setDraftFrom] = useState<Date | null>(
    filters.from ? parseLocalDate(filters.from) : null,
  );
  const [draftTo, setDraftTo] = useState<Date | null>(
    filters.to ? parseLocalDate(filters.to) : null,
  );
  const [draftTimezone, setDraftTimezone] = useState(filters.timezone || 'local');

  useEffect(() => {
    setDraftFrom(filters.from ? parseLocalDate(filters.from) : null);
    setDraftTo(filters.to ? parseLocalDate(filters.to) : null);
    setDraftTimezone(filters.timezone || 'local');
  }, [filters.from, filters.to, filters.timezone]);

  const handleTimezoneChange = useCallback(
    (timezone: string) => {
      const nextTimezone = timezone === 'local' ? 'local' : timezone;
      setDraftTimezone(nextTimezone);
      setFilters({ timezone: nextTimezone, page: 0 });
    },
    [setFilters],
  );

  const handleStateChange = useCallback(
    (value: string) => {
      if (!value) return;
      setFilters({
        state: value as AlertListStateFilter,
        page: 0,
      });
    },
    [setFilters],
  );

  const handleProcessChange = useCallback(
    (selectedLabel?: string, selectedValue?: string) => {
      const value = selectedValue ?? selectedLabel ?? '';
      setFilters({
        processDefinitionId: !value || value === ALL_VALUE ? null : value,
        page: 0,
      });
    },
    [setFilters],
  );

  const handleApplyDates = useCallback(
    (appliedFrom?: Date | null, appliedTo?: Date | null) => {
      let nextFrom = draftFrom;
      let nextTo = draftTo;

      if (appliedFrom !== undefined) {
        nextFrom = appliedFrom;
        setDraftFrom(appliedFrom);
      }
      if (appliedTo !== undefined) {
        nextTo = appliedTo;
        setDraftTo(appliedTo);
      }

      setFilters({
        from: nextFrom ? formatLocalDate(nextFrom) : null,
        to: nextTo ? formatLocalDate(nextTo) : null,
        timezone: draftTimezone,
        page: 0,
      });
    },
    [draftFrom, draftTo, draftTimezone, setFilters],
  );

  const handleClearDates = useCallback(() => {
    setDraftFrom(null);
    setDraftTo(null);
    setDraftTimezone('local');
    setFilters({ from: null, to: null, timezone: null, page: 0 });
  }, [setFilters]);

  const handleClearAll = useCallback(() => {
    setDraftFrom(null);
    setDraftTo(null);
    setDraftTimezone('local');
    clearFilters('push');
  }, [clearFilters]);

  return (
    <div className={styles.wrapper} role="search" aria-label="Alert filters">
      <div className={styles.filterItem}>
        <Select
          value={filters.state}
          items={STATE_OPTIONS}
          onValueChange={handleStateChange}
          placeholder="State"
          size="sm"
        />
      </div>

      <div className={styles.filterItem}>
        <Combobox
          key={processComboboxKey}
          defaultValue={selectedProcess}
          items={processItems}
          onSelectValue={handleProcessChange}
          disabled={isLoading}
          loading={isLoading}
          noItemsMessage="No processes found"
          placeholderText="All Processes"
          size="sm"
          editable={false}
          filterOptions
          showExpandIcon
        />
      </div>

      <div className={styles.filterItemDate}>
        <DateRangePicker
          from={draftFrom}
          to={draftTo}
          onChange={(from, to) => {
            setDraftFrom(from);
            setDraftTo(to);
          }}
          onTimezoneChange={handleTimezoneChange}
          onApply={handleApplyDates}
          onClear={handleClearDates}
          showClear={!!(filters.from || filters.to)}
          timezone={draftTimezone}
        />
      </div>

      <span className={styles.spacer} />

      {hasActiveAlertFilters && (
        <button type="button" className={styles.clearAllButton} onClick={handleClearAll}>
          <X size={12} />
          Clear all
        </button>
      )}
    </div>
  );
};

export default React.memo(AlertFilterBar);
