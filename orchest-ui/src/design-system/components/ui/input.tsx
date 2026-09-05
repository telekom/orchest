import clsx from "clsx";
import * as React from "react";
import styles from "./input.module.css";

interface InputProps extends React.ComponentProps<"input"> {
  errorMessage?: string;
  /** @deprecated ODS compat — use placeholder */
  labelText?: string;
  /** @deprecated ODS compat — use placeholder */
  placeholderText?: string;
  /** @deprecated ODS compat — ignored */
  inputProps?: Record<string, unknown>;
  /** @deprecated ODS compat — ignored */
  supportMessageProps?: unknown;
  /** @deprecated ODS compat — ignored */
  mode?: string;
}

const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ className, type = "text", errorMessage, labelText, placeholderText, inputProps: _ip, supportMessageProps: _smp, mode: _mode, placeholder, onChange, value, ...props }, ref) => {
    const resolvedPlaceholder = placeholderText || placeholder || labelText;
    const extraProps = (value !== undefined && !onChange && !props.readOnly)
      ? { readOnly: true }
      : {};
    return (
      <div className={clsx(styles.inputWrapper, className)}>
        <input
          type={type}
          ref={ref}
          placeholder={resolvedPlaceholder}
          className={clsx(styles.input, errorMessage && styles.error)}
          value={value}
          onChange={onChange}
          {...extraProps}
          {...props}
        />
        {errorMessage && <span className={styles.errorMessage}>{errorMessage}</span>}
      </div>
    );
  }
);

Input.displayName = "Input";

export { Input };
