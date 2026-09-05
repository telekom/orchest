import { Combobox } from "@/design-system/components/ui/combobox";
import { Select } from "@/design-system/components/ui/select";
import clsx from "clsx";
import React from "react";
import styles from "./ItemSelect.module.css";

export interface SelectOption {
  value: string;
  label: string;
}

export interface ItemSelectProps {
  value: string;
  onValueChange: (value: string) => void;
  options: SelectOption[];
  placeholder: string;
  variant?: 'default' | 'wide';
  loading?: boolean;
  disabled?: boolean;
  emptyMessage: string;
  loadingMessage: string;
  label?: string;
  searchable?: boolean;
}

export const ItemSelect: React.FC<ItemSelectProps> = ({
  value,
  onValueChange,
  options,
  variant = 'default',
  loading,
  disabled,
  emptyMessage,
  loadingMessage,
  label,
  placeholder,
  searchable = false,
}) => {
  const selectItems = React.useMemo(() => {
    if (loading) {
      return [{ value: 'loading', label: loadingMessage }];
    }
    if (options.length === 0) {
      return [{ value: 'none', label: emptyMessage }];
    }
    return options;
  }, [loading, options, loadingMessage, emptyMessage]);

  const isDisabled = disabled || loading || options.length === 0;

  const showLabel = Boolean(label && !value && !isDisabled && !searchable);

  const containerClassName = clsx(
    styles.selectContainer,
    variant === 'wide' && styles.selectContainerWide,
    searchable && variant === 'wide' && styles.selectContainerSearchable,
  );

  if (searchable) {
    return (
      <div className={containerClassName}>
        {showLabel && <span className={styles.selectLabel}>{label}</span>}
        <Combobox
          items={options}
          value={value}
          onSelect={onValueChange}
          placeholder={placeholder}
          searchPlaceholder="Search..."
          disabled={isDisabled}
          emptyMessage={emptyMessage}
          className={styles.odsCombobox}
        />
      </div>
    );
  }

  return (
    <div className={containerClassName}>
      {showLabel && <span className={styles.selectLabel}>{label}</span>}
      <Select
        items={selectItems}
        value={value}
        onValueChange={onValueChange}
        disabled={isDisabled}
        placeholder={placeholder}
        size="sm"
      />
    </div>
  );
};
