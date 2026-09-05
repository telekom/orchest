import {
  DASHBOARD_REPORTING_TIMEZONES,
  isValidDashboardYmd,
  minYmd,
  normalizeDashboardTimeZoneId,
  plainDateTodayInZone,
  resolveReportingTimeZone,
  type ReportingRangeSlice,
} from '@/features/process-management/utils/dashboardReportingRange';
import { CalendarClock, ChevronDown, X } from 'lucide-react';
import React, { useEffect, useMemo, useRef, useState } from 'react';
import styles from './DashboardReportingRange.module.css';

export interface DashboardReportingRangeProps {
  slice: ReportingRangeSlice;
  onSliceChange: (partial: Partial<ReportingRangeSlice>) => void;
  disabled?: boolean;
}

const YMD = /^\d{4}-\d{2}-\d{2}$/;
const HM = /^([01]\d|2[0-3]):[0-5]\d$/;
const DATETIME_LOCAL = /^(\d{4}-\d{2}-\d{2})T([01]\d|2[0-3]):([0-5]\d)/;

const partsToInputValue = (
  date: string | null,
  time: string | null,
  fallbackTime: string,
): string => {
  if (!date || !YMD.test(date)) return '';
  const t = time && HM.test(time) ? time : fallbackTime;
  return `${date}T${t}`;
};

const inputValueToParts = (
  raw: string,
): { date: string; time: string } | null => {
  const m = raw.match(DATETIME_LOCAL);
  if (!m) return null;
  return { date: m[1], time: `${m[2]}:${m[3]}` };
};

const formatChip = (date: string | null, time: string | null): string | null => {
  if (!date || !YMD.test(date)) return null;
  const [y, mo, d] = date.split('-').map(Number);
  const local = new Date(y, mo - 1, d);
  const datePart = local.toLocaleDateString(undefined, {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  });
  const timePart = time && HM.test(time) ? ` · ${time}` : '';
  return `${datePart}${timePart}`;
};

const DashboardReportingRange: React.FC<DashboardReportingRangeProps> = ({
  slice,
  onSliceChange,
  disabled = false,
}) => {
  const timeZoneValue = normalizeDashboardTimeZoneId(slice.timeZoneId);
  const zone = resolveReportingTimeZone(timeZoneValue);
  const todayMax = plainDateTodayInZone(zone);

  const [open, setOpen] = useState(false);
  const [draftFrom, setDraftFrom] = useState<string>('');
  const [draftTo, setDraftTo] = useState<string>('');
  const [draftTz, setDraftTz] = useState<string>(timeZoneValue);

  const popoverRef = useRef<HTMLDivElement>(null);
  const triggerRef = useRef<HTMLButtonElement>(null);

  const fromChip = formatChip(slice.fromDate, slice.fromTime);
  const toChip = formatChip(slice.toDate, slice.toTime);

  const triggerLabel = useMemo(() => {
    if (fromChip && toChip) return `${fromChip}  →  ${toChip}`;
    if (fromChip) return `From ${fromChip}`;
    if (toChip) return `Until ${toChip}`;
    return 'All time';
  }, [fromChip, toChip]);

  const hasAnyChip = !!(fromChip || toChip);

  useEffect(() => {
    if (!open) return;
    setDraftFrom(partsToInputValue(slice.fromDate, slice.fromTime, '00:00'));
    setDraftTo(partsToInputValue(slice.toDate, slice.toTime, '23:59'));
    setDraftTz(timeZoneValue);
  }, [open, slice.fromDate, slice.fromTime, slice.toDate, slice.toTime, timeZoneValue]);

  useEffect(() => {
    if (!open) return;
    const onClick = (e: MouseEvent) => {
      const target = e.target as HTMLElement | null;
      if (!target) return;
      if (popoverRef.current?.contains(target)) return;
      if (triggerRef.current?.contains(target)) return;
      setOpen(false);
    };
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setOpen(false);
    };
    document.addEventListener('mousedown', onClick);
    document.addEventListener('keydown', onKey);
    return () => {
      document.removeEventListener('mousedown', onClick);
      document.removeEventListener('keydown', onKey);
    };
  }, [open]);

  const maxFromAttr = useMemo(() => {
    const cap =
      slice.toDate && isValidDashboardYmd(slice.toDate)
        ? minYmd(slice.toDate, todayMax)
        : todayMax;
    return `${cap}T23:59`;
  }, [slice.toDate, todayMax]);

  const minToAttr = useMemo(() => {
    return draftFrom || undefined;
  }, [draftFrom]);

  const maxToAttr = useMemo(() => `${todayMax}T23:59`, [todayMax]);

  const draftFromParts = inputValueToParts(draftFrom);
  const draftToParts = inputValueToParts(draftTo);

  const draftFromMs = draftFromParts
    ? new Date(`${draftFromParts.date}T${draftFromParts.time}`).getTime()
    : NaN;
  const draftToMs = draftToParts
    ? new Date(`${draftToParts.date}T${draftToParts.time}`).getTime()
    : NaN;

  const canApply =
    !!draftFromParts &&
    !!draftToParts &&
    !Number.isNaN(draftFromMs) &&
    !Number.isNaN(draftToMs) &&
    draftFromMs <= draftToMs;

  const handleApply = () => {
    if (!draftFromParts || !draftToParts) return;
    if (Number.isNaN(draftFromMs) || Number.isNaN(draftToMs)) return;
    if (draftFromMs > draftToMs) return;
    onSliceChange({
      fromDate: draftFromParts.date,
      fromTime: draftFromParts.time,
      toDate: draftToParts.date,
      toTime: draftToParts.time,
      timeZoneId: normalizeDashboardTimeZoneId(draftTz),
    });
    setOpen(false);
  };

  const handleClear = () => {
    onSliceChange({
      fromDate: null,
      fromTime: null,
      toDate: null,
      toTime: null,
    });
    setOpen(false);
  };

  return (
    <div className={styles.wrapper}>
      <button
        ref={triggerRef}
        type="button"
        className={styles.trigger}
        onClick={() => setOpen((v) => !v)}
        disabled={disabled}
        aria-haspopup="dialog"
        aria-expanded={open}
      >
        <CalendarClock size={16} aria-hidden strokeWidth={2} />
        <span className={styles.triggerLabel}>{triggerLabel}</span>
        {hasAnyChip && (
          <span
            role="button"
            tabIndex={0}
            className={styles.triggerClear}
            aria-label="Clear date range"
            onClick={(e) => {
              e.stopPropagation();
              handleClear();
            }}
            onKeyDown={(e) => {
              if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                e.stopPropagation();
                handleClear();
              }
            }}
          >
            <X size={14} aria-hidden />
          </span>
        )}
        <ChevronDown
          size={14}
          aria-hidden
          className={open ? styles.chevronOpen : styles.chevron}
        />
      </button>

      {open && (
        <div ref={popoverRef} className={styles.popover} role="dialog">
          <div className={styles.popHeader}>
            <span className={styles.popTitle}>Reporting window</span>
            <span className={styles.popHint}>
              Wall clock in selected time zone
            </span>
          </div>

          <div className={styles.fields}>
            <div className={styles.field}>
              <span className={styles.fieldLabel}>From</span>
              <input
                type="datetime-local"
                className={styles.dateInput}
                value={draftFrom}
                max={maxFromAttr}
                onChange={(e) => setDraftFrom(e.target.value)}
                disabled={disabled}
              />
            </div>

            <div className={styles.field}>
              <span className={styles.fieldLabel}>To</span>
              <input
                type="datetime-local"
                className={styles.dateInput}
                value={draftTo}
                min={minToAttr}
                max={maxToAttr}
                onChange={(e) => setDraftTo(e.target.value)}
                disabled={disabled}
              />
            </div>

            <div className={styles.field}>
              <span className={styles.fieldLabel}>Time zone</span>
              <select
                className={styles.tzNative}
                value={draftTz}
                onChange={(e) => setDraftTz(e.target.value)}
                disabled={disabled}
              >
                {DASHBOARD_REPORTING_TIMEZONES.map((tz) => (
                  <option key={tz.value} value={tz.value}>
                    {tz.label}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className={styles.actions}>
            <button
              type="button"
              className={styles.btnGhost}
              onClick={handleClear}
              disabled={!hasAnyChip}
            >
              Clear
            </button>
            <div className={styles.actionsRight}>
              <button
                type="button"
                className={styles.btnGhost}
                onClick={() => setOpen(false)}
              >
                Cancel
              </button>
              <button
                type="button"
                className={styles.btnSolid}
                onClick={handleApply}
                disabled={!canApply}
              >
                Apply
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default React.memo(DashboardReportingRange);
