import {
    Tooltip,
    TooltipContent,
    TooltipTrigger,
} from "@/design-system/components/ui/tooltip/tooltip";
import styles from "./SmartText.module.css";
import React from 'react';
import clsx from 'clsx';

export interface SmartTextProps {
  text: string;
  className?: string;
  maxWidth?: string;
  showTooltip?: boolean;
}

const SmartTextComponent = (
  {
    text,
    className = "",
    maxWidth = "100%",
    showTooltip = true
  }: SmartTextProps,
  ref: React.ForwardedRef<HTMLSpanElement>
) => {
  const innerRef = React.useRef<HTMLSpanElement>(null);
  const combinedRef = React.useCallback((node: HTMLSpanElement | null) => {
    if (typeof ref === 'function') {
      ref(node);
    } else if (ref) {
      (ref as React.MutableRefObject<HTMLSpanElement | null>).current = node;
    }
    innerRef.current = node;
  }, [ref]);
  const [isOverflowing, setIsOverflowing] = React.useState(false);
  const [shouldTruncate, setShouldTruncate] = React.useState(false);

  React.useEffect(() => {
    const checkOverflow = () => {
      if (innerRef.current) {
        innerRef.current.classList.remove('truncate');
        const element = innerRef.current;
        if (element && element.clientWidth !== undefined) {
          const isOverflow = element.scrollWidth > element.clientWidth;
          if (isOverflow) {
            setShouldTruncate(true);
            setIsOverflowing(true);
            element.classList.add('truncate');
          } else {
            setShouldTruncate(false);
            setIsOverflowing(false);
          }
        }
      }
    };
    const timeoutId = setTimeout(checkOverflow, 10);
    window.addEventListener('resize', checkOverflow);
    return () => {
      clearTimeout(timeoutId);
      window.removeEventListener('resize', checkOverflow);
    };
  }, [text]);

  const content = (
    <span
      ref={combinedRef}
      className={clsx(
        styles.text,
        className,
        shouldTruncate && styles.textTruncated,
        showTooltip && isOverflowing && styles.textWithTooltip
      )}
      style={{ maxWidth }} // INLINE STYLE: Dynamic maxWidth prop
      title={!showTooltip ? text : undefined}
    >
      {text}
    </span>
  );

  if (!showTooltip || !isOverflowing) {
    return content;
  }
  return (
    <Tooltip delayDuration={300}>
      <TooltipTrigger asChild>
        {content}
      </TooltipTrigger>
      <TooltipContent side="top" className={styles.tooltipContent}>
        <p>{text}</p>
      </TooltipContent>
    </Tooltip>
  );
};

export const SmartText = React.forwardRef<HTMLSpanElement, SmartTextProps>(SmartTextComponent);
SmartText.displayName = "SmartText";
