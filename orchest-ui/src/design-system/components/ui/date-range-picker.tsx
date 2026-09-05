import * as Popover from "@radix-ui/react-popover";
import clsx from "clsx";
import { format, subMinutes, subHours, subDays, subWeeks, subMonths } from "date-fns";
import { Calendar, X } from "lucide-react";
import * as React from "react";
import { DayPicker } from "react-day-picker";
import styles from "./date-range-picker.module.css";

const TIMEZONE_OPTIONS = [
  { value: "local", label: "Local" },
  { value: "UTC", label: "UTC" },
  { value: "Europe/Berlin", label: "CET" },
  { value: "Asia/Kolkata", label: "IST" },
];

const PRESET_AMOUNTS = [1, 5, 7, 10, 24, 30];
const PRESET_UNITS = [
  { value: "minutes", label: "Minutes", fn: subMinutes },
  { value: "hours", label: "Hours", fn: subHours },
  { value: "days", label: "Days", fn: subDays },
  { value: "weeks", label: "Weeks", fn: subWeeks },
  { value: "months", label: "Months", fn: subMonths },
];

function getBrowserTimezone(): string {
  try {
    const tz = Intl.DateTimeFormat().resolvedOptions().timeZone;
    if (tz === "Europe/Berlin" || tz === "Europe/Paris") return "Europe/Berlin";
    if (tz === "Asia/Kolkata" || tz === "Asia/Calcutta") return "Asia/Kolkata";
    if (tz === "UTC") return "UTC";
    return "local";
  } catch {
    return "local";
  }
}

export interface DateRangePickerProps {
  from?: Date | null;
  to?: Date | null;
  onChange?: (from: Date | null, to: Date | null) => void;
  onTimezoneChange?: (tz: string) => void;
  /**
   * Called when Apply or Go is pressed. Receives the range being committed
   * so parents do not rely on draft state that may not have flushed yet.
   */
  onApply?: (from: Date | null, to: Date | null) => void;
  onClear?: () => void;
  showClear?: boolean;
  disabled?: boolean;
  timezone?: string;
  className?: string;
}

const DateRangePicker: React.FC<DateRangePickerProps> = ({
  from,
  to,
  onChange,
  onTimezoneChange,
  onApply,
  onClear,
  showClear = false,
  disabled = false,
  timezone,
  className,
}) => {
  const [open, setOpen] = React.useState(false);
  const [selectedTz, setSelectedTz] = React.useState(() => timezone || getBrowserTimezone());
  const [fromTime, setFromTime] = React.useState(() =>
    from ? format(from, "HH:mm") : "00:00"
  );
  const [toTime, setToTime] = React.useState(() =>
    to ? format(to, "HH:mm") : "23:59"
  );

  React.useEffect(() => {
    if (from) setFromTime(format(from, "HH:mm"));
  }, [from]);

  React.useEffect(() => {
    if (to) setToTime(format(to, "HH:mm"));
  }, [to]);

  const handleRangeSelect = (range: { from?: Date; to?: Date } | undefined) => {
    if (!range) {
      onChange?.(null, null);
      return;
    }
    let newFrom = range.from ?? null;
    let newTo = range.to ?? null;

    if (newFrom) {
      const [h, m] = fromTime.split(":").map(Number);
      newFrom = new Date(newFrom);
      newFrom.setHours(h || 0, m || 0, 0, 0);
    }
    if (newTo) {
      const [h, m] = toTime.split(":").map(Number);
      newTo = new Date(newTo);
      newTo.setHours(h || 0, m || 0, 0, 0);
    }
    onChange?.(newFrom, newTo);
  };

  const handleFromTimeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    setFromTime(val);
    if (from) {
      const updated = new Date(from);
      const [h, m] = val.split(":").map(Number);
      updated.setHours(h || 0, m || 0, 0, 0);
      onChange?.(updated, to ?? null);
    }
  };

  const handleToTimeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    setToTime(val);
    if (to) {
      const updated = new Date(to);
      const [h, m] = val.split(":").map(Number);
      updated.setHours(h || 0, m || 0, 0, 0);
      onChange?.(from ?? null, updated);
    }
  };

  const handleTimezoneChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const tz = e.target.value;
    setSelectedTz(tz);
    onTimezoneChange?.(tz);
  };

  const [presetAmount, setPresetAmount] = React.useState(7);
  const [presetUnit, setPresetUnit] = React.useState("days");

  const handleQuickApply = () => {
    const unit = PRESET_UNITS.find((u) => u.value === presetUnit);
    if (!unit) return;
    const now = new Date();
    const start = unit.fn(now, presetAmount);
    setFromTime(format(start, "HH:mm"));
    setToTime(format(now, "HH:mm"));
    onChange?.(start, now);
    setOpen(false);
    // Pass dates directly — parent draft state may not have flushed yet.
    onApply?.(start, now);
  };

  const handleApply = () => {
    const appliedFrom = from ?? null;
    const appliedTo = to ?? null;
    setOpen(false);
    onApply?.(appliedFrom, appliedTo);
  };

  const displayText = from && to
    ? `${format(from, "MMM d, HH:mm")} – ${format(to, "MMM d, HH:mm")}`
    : from
      ? `${format(from, "MMM d, HH:mm")} – …`
      : "Select date range";

  return (
    <Popover.Root open={open} onOpenChange={setOpen}>
      <Popover.Trigger asChild disabled={disabled}>
        <button
          type="button"
          className={clsx(styles.trigger, disabled && styles.disabled, className)}
        >
          <Calendar size={14} className={styles.calendarIcon} />
          <span className={clsx(styles.value, !from && styles.placeholder)}>
            {displayText}
          </span>
          {showClear && (from || to) && (
            <span
              className={styles.clearButton}
              onClick={(e) => { e.stopPropagation(); onClear?.(); }}
              role="button"
              aria-label="Clear date range"
            >
              <X size={12} />
            </span>
          )}
        </button>
      </Popover.Trigger>
      <Popover.Portal>
        <Popover.Content className={styles.content} sideOffset={4} align="end">
          <div className={styles.layout}>
            {/* Calendar + time */}
            <div className={styles.calendarSection}>
              <DayPicker
                mode="range"
                selected={from && to ? { from, to } : from ? { from, to: undefined } : undefined}
                onSelect={handleRangeSelect}
                numberOfMonths={2}
                disabled={[{ after: new Date() }]}
                classNames={{
                  root: styles.calendar,
                  months: styles.months,
                  month: styles.month,
                  month_caption: styles.caption,
                  nav: styles.nav,
                  button_previous: styles.navButton,
                  button_next: styles.navButton,
                  month_grid: styles.table,
                  weekdays: styles.headRow,
                  weekday: styles.headCell,
                  week: styles.row,
                  day: styles.cell,
                  day_button: styles.day,
                  selected: styles.daySelected,
                  today: styles.dayToday,
                  outside: styles.dayOutside,
                  disabled: styles.dayDisabled,
                  range_start: styles.rangeStart,
                  range_end: styles.rangeEnd,
                  range_middle: styles.rangeMiddle,
                }}
              />
              <div className={styles.timeRow}>
                <div className={styles.timeGroup}>
                  <label className={styles.timeLabel}>From</label>
                  <input
                    type="time"
                    value={fromTime}
                    onChange={handleFromTimeChange}
                    className={styles.timeInput}
                  />
                </div>
                <div className={styles.timeGroup}>
                  <label className={styles.timeLabel}>To</label>
                  <input
                    type="time"
                    value={toTime}
                    onChange={handleToTimeChange}
                    className={styles.timeInput}
                  />
                </div>
                <select
                  value={selectedTz}
                  onChange={handleTimezoneChange}
                  className={styles.tzSelect}
                >
                  {TIMEZONE_OPTIONS.map((tz) => (
                    <option key={tz.value} value={tz.value}>{tz.label}</option>
                  ))}
                </select>
              </div>
              <div className={styles.footer}>
                <div className={styles.quickSelect}>
                  <span className={styles.quickSelectLabel}>Last</span>
                  <select
                    className={styles.quickSelectDropdown}
                    value={presetAmount}
                    onChange={(e) => setPresetAmount(Number(e.target.value))}
                  >
                    {PRESET_AMOUNTS.map((amt) => (
                      <option key={amt} value={amt}>{amt}</option>
                    ))}
                  </select>
                  <select
                    className={styles.quickSelectDropdown}
                    value={presetUnit}
                    onChange={(e) => setPresetUnit(e.target.value)}
                  >
                    {PRESET_UNITS.map((u) => (
                      <option key={u.value} value={u.value}>{u.label}</option>
                    ))}
                  </select>
                  <button type="button" className={styles.quickSelectApply} onClick={handleQuickApply}>
                    Go
                  </button>
                </div>
                <button type="button" className={styles.applyBtn} onClick={handleApply} disabled={!from || !to}>
                  Apply
                </button>
              </div>
            </div>
          </div>
        </Popover.Content>
      </Popover.Portal>
    </Popover.Root>
  );
};

export { DateRangePicker };