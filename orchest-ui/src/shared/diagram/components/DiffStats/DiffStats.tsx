import { Badge } from "@/design-system/components/ui/badge/badge";
import styles from './DiffStats.module.css';

interface DiffStatsProps {
  added: number;
  removed: number;
  changed: number;
  layoutChanged: number;
}

const STAT_CONFIGS = {
  added: {
    badgeClass: styles.addedBadge,
    dotClass: styles.addedDot,
  },
  removed: {
    badgeClass: styles.removedBadge,
    dotClass: styles.removedDot,
  },
  changed: {
    badgeClass: styles.changedBadge,
    dotClass: styles.changedDot,
  },
  layout: {
    badgeClass: styles.layoutBadge,
    dotClass: styles.layoutDot,
  },
};

export const DiffStats = ({ added, removed, changed, layoutChanged }: DiffStatsProps) => {
  const hasChanges = added + removed + changed + layoutChanged > 0;

  if (!hasChanges) {
    return (
      <Badge variant="outline" className={`${styles.badge} ${styles.noChangesBadge}`}>
        No changes detected
      </Badge>
    );
  }

  const stats = [
    { count: added, label: "Added", config: STAT_CONFIGS.added },
    { count: removed, label: "Removed", config: STAT_CONFIGS.removed },
    { count: changed, label: "Changed", config: STAT_CONFIGS.changed },
    { count: layoutChanged, label: "Layout Changed", config: STAT_CONFIGS.layout },
  ];

  return (
    <div className={styles.container}>
      {stats.map(
        ({ count, label, config }) =>
          count > 0 && (
            <Badge
              key={label}
              variant="outline"
              className={`${styles.badge} ${config.badgeClass}`}
            >
              <div className={`${styles.dot} ${config.dotClass}`} />
              {count} {label}
            </Badge>
          )
      )}
    </div>
  );
};
