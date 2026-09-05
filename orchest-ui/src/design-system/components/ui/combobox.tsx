import * as Popover from "@radix-ui/react-popover";
import clsx from "clsx";
import { Command } from "cmdk";
import { Check, ChevronsUpDown, Search } from "lucide-react";
import * as React from "react";
import styles from "./combobox.module.css";

export interface ComboboxItem {
  value: string;
  label: string;
}

export interface ComboboxProps {
  items: ComboboxItem[];
  value?: string;
  defaultValue?: string;
  onSelect?: (value: string) => void;
  /** @deprecated ODS compat — use onSelect */
  onSelectValue?: (label?: string, value?: string) => void;
  placeholder?: string;
  /** @deprecated ODS compat — use placeholder */
  placeholderText?: string;
  searchPlaceholder?: string;
  disabled?: boolean;
  loading?: boolean;
  emptyMessage?: string;
  /** @deprecated ODS compat — use emptyMessage */
  noItemsMessage?: string;
  className?: string;
  label?: string;
  editable?: boolean;
  filterOptions?: boolean;
  showExpandIcon?: boolean;
  size?: string;
  [key: string]: unknown;
}

const Combobox: React.FC<ComboboxProps> = ({
  items,
  value,
  defaultValue,
  onSelect,
  onSelectValue,
  placeholder = "Select...",
  placeholderText,
  searchPlaceholder = "Search...",
  disabled = false,
  loading: _loading,
  emptyMessage = "No results found.",
  noItemsMessage,
  className,
  label: _label,
  editable: _editable,
  filterOptions: _filterOptions,
  showExpandIcon: _showExpandIcon,
  size: _size,
  ...rest
}) => {
  const [open, setOpen] = React.useState(false);
  const resolvedValue = value ?? defaultValue;
  const resolvedPlaceholder = placeholderText || placeholder;
  const resolvedEmptyMessage = noItemsMessage || emptyMessage;
  const selectedItem = items.find((item) => item.value === resolvedValue);

  return (
    <Popover.Root open={open} onOpenChange={setOpen}>
      <Popover.Trigger asChild disabled={disabled}>
        <button
          type="button"
          role="combobox"
          aria-expanded={open}
          className={clsx(styles.trigger, disabled && styles.disabled, className)}
        >
          <span className={clsx(!selectedItem && styles.placeholder)}>
            {selectedItem ? selectedItem.label : resolvedPlaceholder}
          </span>
          <ChevronsUpDown size={14} className={styles.chevron} />
        </button>
      </Popover.Trigger>
      <Popover.Portal>
        <Popover.Content className={styles.content} sideOffset={4} align="start">
          <Command className={styles.command}>
            <div className={styles.searchWrapper}>
              <Search size={14} className={styles.searchIcon} />
              <Command.Input
                placeholder={searchPlaceholder}
                className={styles.searchInput}
              />
            </div>
            <Command.List className={styles.list}>
              <Command.Empty className={styles.empty}>{resolvedEmptyMessage}</Command.Empty>
              {items.map((item) => (
                <Command.Item
                  key={item.value}
                  value={item.label}
                  onSelect={() => {
                    const newValue = item.value === resolvedValue ? "" : item.value;
                    onSelect?.(newValue);
                    onSelectValue?.(item.label, newValue);
                    setOpen(false);
                  }}
                  className={styles.item}
                >
                  <Check
                    size={14}
                    className={clsx(styles.checkIcon, value === item.value && styles.checkVisible)}
                  />
                  {item.label}
                </Command.Item>
              ))}
            </Command.List>
          </Command>
        </Popover.Content>
      </Popover.Portal>
    </Popover.Root>
  );
};

export { Combobox };