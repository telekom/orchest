import React from "react";
import { useJsonFormatter } from "@/shared/hooks/useJsonFormatter";
import styles from "./JsonDisplay.module.css";
import clsx from "clsx";

export interface JsonDisplayProps {
  value: string;
  type?: string;
  className?: string;
  indent?: number;
  codeBlock?: boolean;
}

export const JsonDisplay: React.FC<JsonDisplayProps> = ({
  value,
  type = "string",
  className = "",
  indent = 2,
  codeBlock = true,
}) => {
  const { formatted, isValid } = useJsonFormatter({ value, type, indent });

  if (type === "object" && codeBlock) {
    return (
      <pre
        className={clsx(
          styles.codeBlock,
          !isValid && styles.codeBlockInvalid,
          className
        )}
        title={!isValid ? "Invalid JSON" : undefined}
      >
        {formatted}
      </pre>
    );
  }

  return (
    <span
      className={clsx(styles.inlineText, className)}
      title={!isValid ? "Invalid JSON" : undefined}
    >
      {formatted}
    </span>
  );
};
