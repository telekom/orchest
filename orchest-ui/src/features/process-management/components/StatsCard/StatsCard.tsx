import React, { useMemo } from "react";
import { StatsCardColor } from "@/shared/enums";
import { Activity, Ban, CheckCircle2, Layers, OctagonAlert, Pause, TriangleAlert } from "lucide-react";
import clsx from "clsx";
import styles from "./StatsCard.module.css";

export type StatsCardProps = {
  count: number | string;
  label: string;
  color: StatsCardColor;
  total?: number;
  isSelected?: boolean;
  isLoading?: boolean;
  onClick?: () => void;
};

const colorClassMap: Record<StatsCardColor, string> = {
  primary: styles.primary,
  green: styles.green,
  blue: styles.blue,
  red: styles.red,
  yellow: styles.yellow,
  orange: styles.orange,
  gray: styles.gray,
};

const iconMap: Record<StatsCardColor, React.ReactNode> = {
  primary: <Layers size={18} />,
  green: <Activity size={18} />,
  blue: <CheckCircle2 size={18} />,
  red: <OctagonAlert size={18} />,
  yellow: <Pause size={18} />,
  orange: <TriangleAlert size={18} />,
  gray: <Ban size={18} />,
};

export const StatsCard: React.FC<StatsCardProps> = ({
  count,
  label,
  color,
  total = 0,
  isSelected = false,
  isLoading = false,
  onClick,
}) => {
  const colorClass = colorClassMap[color] || colorClassMap[StatsCardColor.PRIMARY];
  const percent = useMemo(() => {
    if (color === 'primary') return null;
    const num = typeof count === 'number' ? count : parseInt(count as string, 10) || 0;
    if (total <= 0 || num <= 0) return 0;
    return Math.round((num / total) * 100);
  }, [count, total, color]);

  return (
    <button
      onClick={onClick}
      className={clsx(
        styles.card,
        colorClass,
        isSelected && styles.selected
      )}
    >
      <div className={styles.bgIcon}>{iconMap[color]}</div>
      <div className={styles.content}>
        <div className={styles.countRow}>
          <span className={styles.count}>
            {isLoading ? "..." : count}
          </span>
          {percent !== null && (
            <span className={styles.percent}>{percent}%</span>
          )}
        </div>
        <span className={styles.label}>{label}</span>
      </div>
    </button>
  );
};

export default StatsCard;
