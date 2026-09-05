import { DateRangePicker } from '@/design-system/components/ui/date-range-picker';
import { Button } from '@/design-system/components/ui/button';
import { RefreshCw } from 'lucide-react';
import clsx from 'clsx';
import React from 'react';
import styles from './UsageToolbar.module.css';

export interface UsageToolbarProps {
  draftFrom: Date | null;
  draftTo: Date | null;
  timezone?: string;
  isRangeFiltered: boolean;
  isRefreshing: boolean;
  lastUpdatedLabel?: string | null;
  onDraftChange: (from: Date | null, to: Date | null) => void;
  onTimezoneChange: (tz: string) => void;
  onApply: (from: Date | null, to: Date | null) => void;
  onClear: () => void;
  onRefresh: () => void;
}

export const UsageToolbar: React.FC<UsageToolbarProps> = ({
  draftFrom,
  draftTo,
  timezone,
  isRangeFiltered,
  isRefreshing,
  lastUpdatedLabel,
  onDraftChange,
  onTimezoneChange,
  onApply,
  onClear,
  onRefresh,
}) => {
  return (
    <div className={styles.toolbar}>
      <div className={styles.filters}>
        <DateRangePicker
          from={draftFrom}
          to={draftTo}
          timezone={timezone}
          onChange={onDraftChange}
          onTimezoneChange={(tz) =>
            onTimezoneChange(tz === 'local' ? 'UTC' : tz)
          }
          onApply={onApply}
          onClear={onClear}
          showClear={isRangeFiltered}
          disabled={isRefreshing}
        />
        {isRangeFiltered && (
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={onClear}
            disabled={isRefreshing}
          >
            Clear
          </Button>
        )}
      </div>
      <div className={styles.meta}>
        {lastUpdatedLabel && (
          <span className={styles.updated}>{lastUpdatedLabel}</span>
        )}
        <Button
          type="button"
          variant="ghost"
          size="icon"
          onClick={onRefresh}
          disabled={isRefreshing}
          aria-label="Refresh usage data"
        >
          <RefreshCw
            size={16}
            className={clsx(styles.refreshIcon, isRefreshing && styles.spinning)}
          />
        </Button>
      </div>
    </div>
  );
};
