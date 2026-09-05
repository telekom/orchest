import React from 'react';
import { Badge } from '@/design-system/components/ui/badge/badge';
import { getBadgeStyleForPriority } from '@/shared/utils/badgeUtils';
import { getPriorityLabel } from '@/features/task-management/constants/taskPriority';
import styles from './PriorityBadge.module.css';
import clsx from 'clsx';

export interface PriorityBadgeProps {
  priority: number;
  className?: string;
  showTooltip?: boolean;
}

export const PriorityBadge: React.FC<PriorityBadgeProps> = React.memo(({
  priority,
  className,
  showTooltip = true,
}) => {
  const label = getPriorityLabel(priority);
  const badgeStyle = getBadgeStyleForPriority(priority);
  const IconComponent = badgeStyle.icon;

  const tooltipText = showTooltip ? `Priority: ${label} (${priority})` : undefined;

  return (
    <Badge
      variant={badgeStyle.variant}
      icon={IconComponent ? <IconComponent /> : undefined}
      className={clsx(styles.badge, className)}
      title={tooltipText}
    >
      <span className={styles.label}>
        {label}
      </span>
    </Badge>
  );
});

PriorityBadge.displayName = 'PriorityBadge';

export default PriorityBadge;
