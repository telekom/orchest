import * as ScrollAreaPrimitive from "@radix-ui/react-scroll-area";
import clsx from "clsx";
import * as React from "react";
import styles from "./scroll-area.module.css";

const ScrollArea = React.forwardRef<
  React.ElementRef<typeof ScrollAreaPrimitive.Root>,
  React.ComponentPropsWithoutRef<typeof ScrollAreaPrimitive.Root> & {
    alwaysShowScrollbar?: boolean;
  }
>(({ className, children, alwaysShowScrollbar = false, ...props }, ref) => (
  <ScrollAreaPrimitive.Root
    ref={ref}
    className={clsx(styles.root, className)}
    {...props}
  >
    <ScrollAreaPrimitive.Viewport className={styles.viewport}>
      {children}
    </ScrollAreaPrimitive.Viewport>
    <ScrollBar forceMount={alwaysShowScrollbar ? true : undefined} />
    <ScrollAreaPrimitive.Corner />
  </ScrollAreaPrimitive.Root>
));
ScrollArea.displayName = ScrollAreaPrimitive.Root.displayName;

const ScrollBar = React.forwardRef<
  React.ElementRef<typeof ScrollAreaPrimitive.ScrollAreaScrollbar>,
  React.ComponentPropsWithoutRef<typeof ScrollAreaPrimitive.ScrollAreaScrollbar>
>(({ className, orientation = "vertical", ...props }, ref) => (
  <ScrollAreaPrimitive.ScrollAreaScrollbar
    ref={ref}
    orientation={orientation}
    className={clsx(
      styles.scrollbar,
      orientation === "vertical" && styles.scrollbarVertical,
      orientation === "horizontal" && styles.scrollbarHorizontal,
      className
    )}
    {...props}
  >
    <ScrollAreaPrimitive.ScrollAreaThumb className={styles.thumb} />
  </ScrollAreaPrimitive.ScrollAreaScrollbar>
));
ScrollBar.displayName = ScrollAreaPrimitive.ScrollAreaScrollbar.displayName;

export { ScrollArea, ScrollBar };
