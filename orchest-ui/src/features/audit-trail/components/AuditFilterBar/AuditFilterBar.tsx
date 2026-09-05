import { Button } from '@/design-system/components/ui/button';
import { DateRangePicker } from '@/design-system/components/ui/date-range-picker';
import { Input } from '@/design-system/components/ui/input';
import clsx from 'clsx';
import { RefreshCw, Search } from 'lucide-react';
import React, { useCallback } from 'react';
import type {
  AuditFilterMode,
  PathFilterParams,
  TimeFilterParams,
  UserFilterParams,
} from '../../types/auditTrail';
import styles from './AuditFilterBar.module.css';

interface AuditFilterBarProps {
  mode: AuditFilterMode;
  onModeChange: (mode: AuditFilterMode) => void;
  userParams: UserFilterParams;
  timeParams: TimeFilterParams;
  pathParams: PathFilterParams;
  onUserParamsChange: (patch: Partial<UserFilterParams>) => void;
  onTimeParamsChange: (patch: Partial<TimeFilterParams>) => void;
  onPathParamsChange: (patch: Partial<PathFilterParams>) => void;
  onRefresh: () => void;
  isLoading: boolean;
}

const MODE_OPTIONS: { value: AuditFilterMode; label: string }[] = [
  { value: 'time', label: 'Time' },
  { value: 'user', label: 'User' },
  { value: 'path', label: 'Path' },
];

const AuditFilterBar: React.FC<AuditFilterBarProps> = ({
  mode,
  onModeChange,
  userParams,
  timeParams,
  pathParams,
  onUserParamsChange,
  onTimeParamsChange,
  onPathParamsChange,
  onRefresh,
  isLoading,
}) => {
  const handleEmailChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      onUserParamsChange({ email: e.target.value });
    },
    [onUserParamsChange],
  );

  const handlePathChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      onPathParamsChange({ path: e.target.value });
    },
    [onPathParamsChange],
  );

  const handleTimeRangeChange = useCallback(
    (from: Date | null, to: Date | null) => {
      onTimeParamsChange({
        from: from ? from.toISOString() : undefined,
        to: to ? to.toISOString() : undefined,
      });
    },
    [onTimeParamsChange],
  );

  const handleTimeRangeApply = useCallback(
    (from: Date | null, to: Date | null) => {
      onTimeParamsChange({
        from: from ? from.toISOString() : undefined,
        to: to ? to.toISOString() : undefined,
      });
    },
    [onTimeParamsChange],
  );

  const handlePathRangeChange = useCallback(
    (from: Date | null, to: Date | null) => {
      onPathParamsChange({
        from: from ? from.toISOString() : undefined,
        to: to ? to.toISOString() : undefined,
      });
    },
    [onPathParamsChange],
  );

  const handlePathRangeApply = useCallback(
    (from: Date | null, to: Date | null) => {
      onPathParamsChange({
        from: from ? from.toISOString() : undefined,
        to: to ? to.toISOString() : undefined,
      });
    },
    [onPathParamsChange],
  );

  const canSearch =
    mode === 'time'
      ? !!(timeParams.from && timeParams.to)
      : mode === 'user'
        ? !!userParams.email.trim()
        : !!(pathParams.path.trim() && pathParams.from && pathParams.to);

  return (
    <div className={styles.container} role="search" aria-label="Audit trail filters">
      <div
        className={styles.modeSegment}
        role="tablist"
        aria-label="Search mode"
      >
        {MODE_OPTIONS.map((opt) => (
          <button
            key={opt.value}
            type="button"
            role="tab"
            aria-selected={mode === opt.value}
            className={clsx(
              styles.modeBtn,
              mode === opt.value && styles.modeBtnActive,
            )}
            onClick={() => onModeChange(opt.value)}
          >
            {opt.label}
          </button>
        ))}
      </div>

      <div className={styles.inputs}>
        {mode === 'user' && (
          <Input
            className={styles.emailInput}
            placeholder="user@example.com"
            value={userParams.email}
            onChange={handleEmailChange}
            type="email"
            aria-label="User email"
          />
        )}

        {mode === 'path' && (
          <Input
            className={styles.pathInput}
            placeholder="/api/v1/endpoint"
            value={pathParams.path}
            onChange={handlePathChange}
            type="text"
            aria-label="API path"
          />
        )}

        {(mode === 'time' || mode === 'path') && (
          <DateRangePicker
            from={
              mode === 'time'
                ? timeParams.from
                  ? new Date(timeParams.from)
                  : null
                : pathParams.from
                  ? new Date(pathParams.from)
                  : null
            }
            to={
              mode === 'time'
                ? timeParams.to
                  ? new Date(timeParams.to)
                  : null
                : pathParams.to
                  ? new Date(pathParams.to)
                  : null
            }
            onChange={
              mode === 'time' ? handleTimeRangeChange : handlePathRangeChange
            }
            onApply={
              mode === 'time' ? handleTimeRangeApply : handlePathRangeApply
            }
          />
        )}
      </div>

      <div className={styles.actions}>
        <Button
          variant="primary"
          size="sm"
          onClick={onRefresh}
          disabled={isLoading || !canSearch}
        >
          <Search size={14} />
          Search
        </Button>
        <Button
          variant="outline"
          size="sm"
          onClick={onRefresh}
          disabled={isLoading || !canSearch}
          aria-label="Refresh results"
        >
          <RefreshCw size={14} className={isLoading ? styles.spinning : ''} />
        </Button>
      </div>
    </div>
  );
};

export default React.memo(AuditFilterBar);
