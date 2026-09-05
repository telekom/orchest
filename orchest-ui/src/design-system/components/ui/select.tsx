import * as SelectPrimitive from "@radix-ui/react-select";
import clsx from "clsx";
import { Check, ChevronDown, ChevronUp } from "lucide-react";
import * as React from "react";
import styles from "./select.module.css";

export interface SelectItem {
  value: string;
  label: string;
}

export interface SelectProps {
  value?: string;
  onValueChange?: (value: string) => void;
  placeholder?: string;
  disabled?: boolean;
  items: SelectItem[];
  label?: string;
  size?: "sm" | "md" | string;
  className?: string;
  [key: string]: unknown;
}

const Select: React.FC<SelectProps> = ({
  value,
  onValueChange,
  placeholder = "Select...",
  disabled = false,
  items,
  label,
  size = "md",
  className,
}) => {
  return (
    <div className={clsx(styles.wrapper, className)}>
      {label && <label className={styles.label}>{label}</label>}
      <SelectPrimitive.Root value={value} onValueChange={onValueChange} disabled={disabled}>
        <SelectPrimitive.Trigger className={clsx(styles.trigger, styles[size])}>
          <SelectPrimitive.Value placeholder={placeholder} />
          <SelectPrimitive.Icon className={styles.icon}>
            <ChevronDown size={14} />
          </SelectPrimitive.Icon>
        </SelectPrimitive.Trigger>
        <SelectPrimitive.Portal>
          <SelectPrimitive.Content className={styles.content} position="popper" sideOffset={4}>
            <SelectPrimitive.ScrollUpButton className={styles.scrollButton}>
              <ChevronUp size={14} />
            </SelectPrimitive.ScrollUpButton>
            <SelectPrimitive.Viewport className={styles.viewport}>
              {items.map((item) => (
                <SelectPrimitive.Item key={item.value} value={item.value} className={styles.item}>
                  <SelectPrimitive.ItemText>{item.label}</SelectPrimitive.ItemText>
                  <SelectPrimitive.ItemIndicator className={styles.itemIndicator}>
                    <Check size={14} />
                  </SelectPrimitive.ItemIndicator>
                </SelectPrimitive.Item>
              ))}
            </SelectPrimitive.Viewport>
            <SelectPrimitive.ScrollDownButton className={styles.scrollButton}>
              <ChevronDown size={14} />
            </SelectPrimitive.ScrollDownButton>
          </SelectPrimitive.Content>
        </SelectPrimitive.Portal>
      </SelectPrimitive.Root>
    </div>
  );
};

export { Select };