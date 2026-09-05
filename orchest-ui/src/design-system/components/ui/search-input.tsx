import clsx from "clsx";
import { Search, X } from "lucide-react";
import * as React from "react";
import styles from "./search-input.module.css";

export interface SearchInputProps {
  value?: string;
  onChange?: (value: string) => void;
  onInput?: (value: string) => void;
  onClear?: () => void;
  placeholder?: string;
  /** @deprecated Legacy ODS compat — use placeholder instead */
  labelText?: string;
  disabled?: boolean;
  className?: string;
  size?: string;
  [key: string]: unknown;
}

const SearchInput: React.FC<SearchInputProps> = ({
  value = "",
  onChange,
  onInput,
  onClear,
  placeholder = "Search...",
  labelText,
  disabled = false,
  className,
  size: _size,
  ...rest
}) => {
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    onChange?.(e.target.value);
    onInput?.(e.target.value);
  };
  const resolvedPlaceholder = labelText || placeholder;

  const handleClear = () => {
    onChange?.("");
    onClear?.();
  };

  return (
    <div className={clsx(styles.wrapper, disabled && styles.disabled, className)}>
      <Search size={14} className={styles.searchIcon} />
      <input
        type="text"
        value={value}
        onChange={handleChange}
        placeholder={resolvedPlaceholder}
        disabled={disabled}
        className={styles.input}
      />
      {value && (
        <button
          type="button"
          onClick={handleClear}
          className={styles.clearButton}
          aria-label="Clear search"
        >
          <X size={14} />
        </button>
      )}
    </div>
  );
};

export { SearchInput };
