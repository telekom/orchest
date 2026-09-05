import { Slot } from "@radix-ui/react-slot";
import { cva, type VariantProps } from "class-variance-authority";
import clsx from "clsx";
import * as React from "react";
import styles from "./button.module.css";

const buttonVariants = cva(styles.base, {
  variants: {
    variant: {
      primary: styles.primary,
      secondary: styles.secondary,
      outline: styles.outline,
      ghost: styles.ghost,
      destructive: styles.destructive,
      success: styles.success,
      warning: styles.warning,
    },
    size: {
      sm: styles.sm,
      md: styles.md,
      lg: styles.lg,
      icon: styles.icon,
    },
  },
  defaultVariants: {
    variant: "primary",
    size: "md",
  },
});

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  asChild?: boolean;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
  /** @deprecated Legacy ODS compat — use children instead */
  label?: string;
  /** @deprecated Legacy ODS compat — ignored */
  buttonIcon?: string;
  /** @deprecated Legacy ODS compat — ignored */
  buttonType?: string;
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, asChild = false, leftIcon, rightIcon, children, label, buttonIcon: _bi, buttonType: _bt, ...props }, ref) => {
    const classes = clsx(buttonVariants({ variant, size }), className);
    const content = children || label;

    // Slot requires exactly one React element child — never wrap with icon spans
    if (asChild) {
      return (
        <Slot className={classes} ref={ref} {...props}>
          {content}
        </Slot>
      );
    }

    return (
      <button
        className={classes}
        ref={ref}
        {...props}
      >
        {leftIcon && <span className={styles.iconLeft}>{leftIcon}</span>}
        {content}
        {rightIcon && <span className={styles.iconRight}>{rightIcon}</span>}
      </button>
    );
  }
);

Button.displayName = "Button";

export { Button, buttonVariants };
