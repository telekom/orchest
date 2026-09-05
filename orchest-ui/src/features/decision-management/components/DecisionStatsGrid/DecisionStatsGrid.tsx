import StatsCard from "@/features/process-management/components/StatsCard/StatsCard";
import { StatsCardColor } from "@/shared/enums";
import React, { useCallback } from "react";
import { type DecisionStats } from "../../hooks/useDecisionStats";
import styles from "./DecisionStatsGrid.module.css";

const DECISION_STATUS = {
  EVALUATED: "EVALUATED",
  FAILED: "FAILED",
  UNKNOWN: "UNKNOWN",
} as const;

export interface DecisionStatsGridProps {
  stats: DecisionStats;
  isLoading?: boolean;
  selectedStatus: string | null;
  onStatusClick: (status: string) => void;
  onClearFilters: () => void;
  hasActiveFilters?: boolean;
}

const DecisionStatsGrid: React.FC<DecisionStatsGridProps> = ({
  stats,
  isLoading = false,
  selectedStatus,
  onStatusClick,
  onClearFilters,
  hasActiveFilters = false,
}) => {
  const statsConfig = [
    {
      key: "total",
      label: "Total",
      color: StatsCardColor.PRIMARY,
      count: stats.total,
      status: null,
      onClick: onClearFilters,
    },
    {
      key: "evaluated",
      label: "Evaluated",
      color: StatsCardColor.BLUE,
      count: stats.evaluated,
      status: DECISION_STATUS.EVALUATED,
    },
    {
      key: "failed",
      label: "Failed",
      color: StatsCardColor.RED,
      count: stats.failed,
      status: DECISION_STATUS.FAILED,
    },
    {
      key: "unknown",
      label: "Unknown",
      color: StatsCardColor.GRAY,
      count: stats.unknown,
      status: DECISION_STATUS.UNKNOWN,
    },
  ];

  const handleCardClick = useCallback(
    (status: string | null) => {
      if (status === null) {
        onClearFilters();
      } else {
        onStatusClick(status);
      }
    },
    [onStatusClick, onClearFilters],
  );

  return (
    <div className={styles.grid}>
      {statsConfig.map(({ key, label, color, count, status, onClick }) => {
        const isSelected =
          status === null ? !hasActiveFilters : selectedStatus === status;

        return (
          <StatsCard
            key={key}
            count={count}
            label={label}
            color={color}
            isSelected={isSelected}
            isLoading={isLoading}
            onClick={onClick || (() => handleCardClick(status))}
          />
        );
      })}
    </div>
  );
};

export default React.memo(DecisionStatsGrid);
