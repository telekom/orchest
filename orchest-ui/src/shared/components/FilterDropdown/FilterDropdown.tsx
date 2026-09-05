import { Select } from '@/design-system/components/ui/select';
import clsx from 'clsx';
import { useMemo } from 'react';
import styles from './FilterDropdown.module.css';

export interface FilterDropdownOption<T extends string> {
  value: T;
  label: string;
}

export interface FilterDropdownProps<T extends string> {
  label: string;
  value: T;
  onValueChange: (value: T) => void;
  options: Array<FilterDropdownOption<T>>;
  placeholder?: string;
  disabled?: boolean;
  className?: string;
  triggerClassName?: string;
  contentClassName?: string;
  itemClassName?: string;
  maxWidth?: string;
  truncateValues?: boolean;
}

export function FilterDropdown<T extends string>({
  label,
  value,
  onValueChange,
  options,
  disabled = false,
  className,
}: FilterDropdownProps<T>) {
  const handleChange = (value: string) => {
    
    if (value) {
      onValueChange(value as T);
    }
  };

  // Convert options to ODS format
  const odsOptions = useMemo(() => {
    return options.map(opt => ({
      id: opt.value,
      value: opt.value,
      label: opt.label,
    }));
  }, [options]);

  return (
    <div className={clsx(styles.container, className)}>
      {label && (
        <label className={styles.label}>
          {label}
        </label>
      )}
      <Select
        label=""
        value={value}
        items={odsOptions}
        onValueChange={handleChange}
        disabled={disabled}
        size="sm"
        className={styles.odsSelect}
      />
    </div>
  );
}