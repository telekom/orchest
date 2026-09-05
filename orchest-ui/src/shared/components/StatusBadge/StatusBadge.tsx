import React from 'react';
import { Badge } from '@/design-system/components/ui/badge/badge';
import { getBadgeStyleForStatus } from '@/shared/utils/badgeUtils';
import styles from './StatusBadge.module.css';
import clsx from 'clsx';

export interface StatusBadgeProps {
  status: string;
  className?: string;
  showTitle?: boolean;
  maxWidth?: string;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({
  status,
  className,
  showTitle = true,
  maxWidth = 'max-w-full',
}) => {
  if (!status) {
    return null;
  }

  const badgeStyle = getBadgeStyleForStatus(status);
  const IconComponent = badgeStyle.icon;

  return (
    <Badge
      variant={badgeStyle.variant}
      icon={IconComponent ? <IconComponent /> : undefined}
      className={clsx(badgeStyle.className, maxWidth, className)}
    >
      <span
        className={styles.badgeContent}
        title={showTitle ? status : undefined}
      >
        {status}
      </span>
    </Badge>
  );
};

export default StatusBadge;
