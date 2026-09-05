import clsx from "clsx";
import React, { memo } from "react";
import styles from "./SelectionCheckbox.module.css";

export interface SelectionCheckboxProps {
  checked: boolean;
  onChange: (checked: boolean) => void;
  indeterminate?: boolean;
  disabled?: boolean;
  className?: string;
  'aria-label'?: string;
}

export const SelectionCheckbox = memo<SelectionCheckboxProps>(({
  checked,
  onChange,
  indeterminate = false,
  disabled = false,
  className,
  'aria-label': ariaLabel,
}) => {
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    onChange(e.target.checked);
  };

  const handleClick = (e: React.MouseEvent) => {
    e.stopPropagation();
  };

  return (
    <div className={clsx(styles.checkboxWrapper, className)} onClick={handleClick}>
      <input
        type="checkbox"
        className={styles.checkbox}
        checked={checked}
        onChange={handleChange}
        disabled={disabled}
        ref={(input) => {
          if (input) {
            input.indeterminate = indeterminate;
          }
        }}
        aria-label={ariaLabel}
      />
    </div>
  );
});

SelectionCheckbox.displayName = 'SelectionCheckbox';
