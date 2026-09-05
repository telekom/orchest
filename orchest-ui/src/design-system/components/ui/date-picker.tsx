import * as Popover from "@radix-ui/react-popover";
import clsx from "clsx";
import { format } from "date-fns";
import { Calendar, X } from "lucide-react";
import * as React from "react";
import { DayPicker } from "react-day-picker";
import styles from "./date-picker.module.css";

const TIMEZONE_OPTIONS = [
  { value: "local", label: "Local" },
  { value: "UTC", label: "UTC" },
  { value: "Europe/Berlin", label: "CET" },
  { value: "Asia/Kolkata", label: "IST" },
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

export interface DatePickerProps {
  value?: Date | null;
  defaultDate?: Date | string;
  onChange?: (date: Date | null) => void;
  onTimezoneChange?: (tz: string) => void;
  placeholder?: string;
  disabled?: boolean;
  minDate?: Date;
  maxDate?: Date;
  showTime?: boolean;
  showTimezone?: boolean;
  timezone?: string;
  disableFuture?: boolean;
  format?: string;
  inputProps?: Record<string, unknown>;
  size?: string;
  className?: string;
  [key: string]: unknown;
}

const DatePicker: React.FC<DatePickerProps> = ({
  value,
  defaultDate,
  onChange,
  onTimezoneChange,
  placeholder = "Select date",
  disabled = false,
  minDate,
  maxDate,
  showTime = true,
  showTimezone = true,
  timezone,
  disableFuture,
  format: _format,
  inputProps: _inputProps,
  size: _size,
  className,
  ...rest
}) => {
  const resolvedMaxDate = disableFuture ? new Date() : maxDate;
  const initialValue = value ?? (defaultDate instanceof Date ? defaultDate : defaultDate ? new Date(defaultDate) : null);
  const [internalDate, setInternalDate] = React.useState<Date | null>(initialValue);
  const currentDate = value !== undefined ? (value ?? null) : internalDate;
  const [open, setOpen] = React.useState(false);
  const [time, setTime] = React.useState(() => {
    if (currentDate) {
      return format(currentDate, "HH:mm");
    }
    return "00:00";
  });
  const [selectedTz, setSelectedTz] = React.useState(() => timezone || getBrowserTimezone());

  const handleSelect = (date: Date | undefined) => {
    if (!date) {
      setInternalDate(null);
      onChange?.(null);
      setOpen(false);
      return;
    }
    const [hours, minutes] = time.split(":").map(Number);
    date.setHours(hours || 0, minutes || 0, 0, 0);
    setInternalDate(date);
    onChange?.(date);
    if (!showTime) {
      setOpen(false);
    }
  };

  const handleTimeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newTime = e.target.value;
    setTime(newTime);
    const baseDate = currentDate ?? new Date();
    const updated = new Date(baseDate);
    const [hours, minutes] = newTime.split(":").map(Number);
    updated.setHours(hours || 0, minutes || 0, 0, 0);
    setInternalDate(updated);
    onChange?.(updated);
  };

  const handleTimezoneChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const tz = e.target.value;
    setSelectedTz(tz);
    onTimezoneChange?.(tz);
  };

  const handleClear = (e: React.MouseEvent) => {
    e.stopPropagation();
    onChange?.(null);
    setTime("00:00");
  };

  const displayText = currentDate
    ? showTime
      ? format(currentDate, "MMM d, yyyy HH:mm")
      : format(currentDate, "MMM d, yyyy")
    : placeholder;

  return (
    <Popover.Root open={open} onOpenChange={setOpen}>
      <Popover.Trigger asChild disabled={disabled}>
        <button
          type="button"
          className={clsx(styles.trigger, disabled && styles.disabled, className)}
        >
          <Calendar size={14} className={styles.calendarIcon} />
          <span className={clsx(styles.value, !currentDate && styles.placeholder)}>
            {displayText}
          </span>
          {currentDate && (
            <span className={styles.clearButton} onClick={handleClear} role="button" aria-label="Clear date">
              <X size={12} />
            </span>
          )}
        </button>
      </Popover.Trigger>
      <Popover.Portal>
        <Popover.Content className={styles.content} sideOffset={4} align="start">
          <DayPicker
            mode="single"
            selected={currentDate ?? undefined}
            onSelect={handleSelect}
            disabled={[
              ...(minDate ? [{ before: minDate }] : []),
              ...(resolvedMaxDate ? [{ after: resolvedMaxDate }] : []),
            ]}
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
            }}
          />
          {showTime && (
            <div className={styles.timeRow}>
              <input
                type="time"
                value={time}
                onChange={handleTimeChange}
                className={styles.timeInput}
              />
              {showTimezone && (
                <select
                  value={selectedTz}
                  onChange={handleTimezoneChange}
                  className={styles.tzSelect}
                >
                  {TIMEZONE_OPTIONS.map((tz) => (
                    <option key={tz.value} value={tz.value}>{tz.label}</option>
                  ))}
                </select>
              )}
            </div>
          )}
        </Popover.Content>
      </Popover.Portal>
    </Popover.Root>
  );
};

export { DatePicker };
