import clsx from "clsx"
import * as SwitchPrimitives from "@radix-ui/react-switch"
import * as React from "react"
import styles from "./switch.module.css"

interface SwitchProps extends Omit<React.ComponentPropsWithoutRef<typeof SwitchPrimitives.Root>, 'onChange'> {
  /** @deprecated ODS compat — use checked instead */
  selected?: boolean;
  /** @deprecated ODS compat — use onCheckedChange */
  inputProps?: { id?: string; name?: string; onChange?: (e: { target: { checked: boolean } }) => void };
  /** @deprecated ODS compat — ignored */
  label?: string;
  /** @deprecated ODS compat — ignored */
  size?: string;
}

const Switch = React.forwardRef<
  React.ElementRef<typeof SwitchPrimitives.Root>,
  SwitchProps
>(({ className, selected, inputProps, checked, onCheckedChange, label: _label, size: _size, ...props }, ref) => {
  const resolvedChecked = checked ?? selected;
  const handleCheckedChange = (val: boolean) => {
    onCheckedChange?.(val);
    inputProps?.onChange?.({ target: { checked: val } });
  };

  return (
    <SwitchPrimitives.Root
      className={clsx(styles.root, className)}
      checked={resolvedChecked}
      onCheckedChange={handleCheckedChange}
      id={inputProps?.id}
      name={inputProps?.name}
      {...props}
      ref={ref}
    >
      <SwitchPrimitives.Thumb className={styles.thumb} />
    </SwitchPrimitives.Root>
  );
})
Switch.displayName = SwitchPrimitives.Root.displayName

export { Switch }
