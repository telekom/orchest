import clsx from "clsx"
import * as LabelPrimitive from "@radix-ui/react-label"
import * as React from "react"
import styles from "./label.module.css"

const Label = React.forwardRef<
  React.ElementRef<typeof LabelPrimitive.Root>,
  React.ComponentPropsWithoutRef<typeof LabelPrimitive.Root>
>(({ className, ...props }, ref) => (
  <LabelPrimitive.Root
    ref={ref}
    className={clsx(styles.label, className)}
    {...props}
  />
))
Label.displayName = LabelPrimitive.Root.displayName

export { Label }
