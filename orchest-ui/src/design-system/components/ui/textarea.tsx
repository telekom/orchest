import clsx from "clsx";
import * as React from "react";
import styles from "./textarea.module.css";

export type TextareaProps = React.TextareaHTMLAttributes<HTMLTextAreaElement>;

const Textarea = React.forwardRef<HTMLTextAreaElement, TextareaProps>(
  ({ className, ...props }, ref) => {
    return (
      <textarea
        ref={ref}
        className={clsx(styles.textarea, className)}
        {...props}
      />
    );
  }
);

Textarea.displayName = "Textarea";

export { Textarea };
