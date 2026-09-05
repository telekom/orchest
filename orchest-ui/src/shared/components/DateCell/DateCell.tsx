import React from 'react';
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/design-system/components/ui/tooltip/tooltip';
import { formatDate, formatDateDetailed, formatDateFull } from '@/shared/utils/dateUtils';
import styles from './DateCell.module.css';
import clsx from 'clsx';

export type DateFormat = 'compact' | 'detailed' | 'full';

export interface DateCellProps {
  date: string | null | undefined;
  label?: string;
  format?: DateFormat;
  showTooltip?: boolean;
  className?: string;
  textClassName?: string;
  fallback?: string;
  tooltipDelay?: number;
  tooltipSide?: 'top' | 'right' | 'bottom' | 'left';
}

export const DateCell: React.FC<DateCellProps> = ({
  date,
  label,
  format = 'detailed',
  showTooltip = true,
  className,
  textClassName,
  fallback = '-',
  tooltipDelay = 300,
  tooltipSide = 'top',
}) => {
  if (!date) {
    return (
      <span className={clsx(styles.fallback, className)}>
        {fallback}
      </span>
    );
  }

  const getFormattedDate = () => {
    switch (format) {
      case 'compact':
        return formatDate(date, fallback);
      case 'full':
        return formatDateFull(date, fallback);
      case 'detailed':
      default:
        return formatDateDetailed(date, fallback);
    }
  };

  const displayDate = getFormattedDate();
  const fullDate = formatDateFull(date, fallback);

  if (!showTooltip) {
    return (
      <span className={clsx(styles.dateText, className, textClassName)}>
        {displayDate}
      </span>
    );
  }

  return (
    <Tooltip delayDuration={tooltipDelay}>
      <TooltipTrigger asChild>
        <span
          className={clsx(styles.dateText, styles.dateTextWithTooltip, className, textClassName)}
        >
          {displayDate}
        </span>
      </TooltipTrigger>
      <TooltipContent side={tooltipSide}>
        {label && (
          <p className={styles.tooltipLabel}>{label}</p>
        )}
        <p className={styles.tooltipDate}>{fullDate}</p>
      </TooltipContent>
    </Tooltip>
  );
};

export default DateCell;
