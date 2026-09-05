import { cva, type VariantProps } from "class-variance-authority";
import clsx from "clsx";
import * as React from "react";
import styles from "./badge.module.css";

const badgeVariants = cva(styles.badge, {
  variants: {
    variant: {
      default: styles.default,
      secondary: styles.secondary,
      destructive: styles.destructive,
      outline: styles.outline,
      success: styles.success,
      warning: styles.warning,
      incident: styles.incident,
      info: styles.info,
      purple: styles.purple,
      indigo: styles.indigo,
      pink: styles.pink,
    },
  },
  defaultVariants: {
    variant: "default",
  },
});

export interface BadgeProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof badgeVariants> {
  children?: React.ReactNode;
  icon?: React.ReactNode;
  /** @deprecated Use `icon` instead */
  withDot?: boolean;
  /** @deprecated Use `icon` instead */
  dotColor?: string;
}

function Badge({ className, variant, icon, withDot = false, dotColor, children, ...props }: BadgeProps) {
  return (
    <div
      className={clsx(badgeVariants({ variant }), className)}
      {...props}
    >
      {icon && <span className={styles.icon}>{icon}</span>}
      {!icon && withDot && (
        <span
          className={styles.dot}
          style={{ backgroundColor: dotColor }}
        />
      )}
      {children}
    </div>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export { Badge, badgeVariants };
