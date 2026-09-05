import clsx from "clsx"
import * as SeparatorPrimitive from "@radix-ui/react-separator"
import * as React from "react"
import styles from "./separator.module.css"

const Separator = React.forwardRef<
  React.ElementRef<typeof SeparatorPrimitive.Root>,
  React.ComponentPropsWithoutRef<typeof SeparatorPrimitive.Root>
>(
  (
    { className, orientation = "horizontal", decorative = true, ...props },
    ref
  ) => (
    <SeparatorPrimitive.Root
      ref={ref}
      decorative={decorative}
      orientation={orientation}
      className={clsx(
        styles.root,
        orientation === "horizontal" ? styles.horizontal : styles.vertical,
        className
      )}
      {...props}
    />
  )
)
Separator.displayName = SeparatorPrimitive.Root.displayName

export { Separator }
