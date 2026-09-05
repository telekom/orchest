import { ProcessStatus } from "@/shared/constants/status";
import { StaggerList } from "@/shared/components";
import { StatsCardColor } from "@/shared/enums";
import React, { useCallback } from "react";
import { type ProcessStats } from "../../hooks/useProcessStats";
import StatsCard from "../StatsCard/StatsCard";
import styles from "./ProcessStatsGrid.module.css";

export interface ProcessStatsGridProps {
  stats: ProcessStats;
  isLoading?: boolean;
  selectedStatus: string | null;
  onStatusClick: (status: string) => void;
  onClearFilters: () => void;
  hasActiveFilters?: boolean;
}

const ProcessStatsGrid: React.FC<ProcessStatsGridProps> = ({
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
      key: "running",
      label: "Running",
      color: StatsCardColor.GREEN,
      count: stats.running,
      status: ProcessStatus.RUNNING,
    },
    {
      key: "completed",
      label: "Completed",
      color: StatsCardColor.BLUE,
      count: stats.completed,
      status: ProcessStatus.COMPLETED,
    },
    {
      key: "failed",
      label: "Failed",
      color: StatsCardColor.RED,
      count: stats.failed,
      status: ProcessStatus.FAILED,
    },
    {
      key: "hold",
      label: "Hold",
      color: StatsCardColor.YELLOW,
      count: stats.hold,
      status: ProcessStatus.HOLD,
    },
    {
      key: "incident",
      label: "Incident",
      color: StatsCardColor.ORANGE,
      count: stats.incident,
      status: ProcessStatus.INCIDENT,
    },
    {
      key: "cancelled",
      label: "Cancelled",
      color: StatsCardColor.GRAY,
      count: stats.cancelled,
      status: ProcessStatus.CANCELLED,
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
    [onStatusClick, onClearFilters]
  );

  return (
    <StaggerList className={styles.grid}>
      {statsConfig.map(({ key, label, color, count, status, onClick }) => {
        const isSelected =
          status === null
            ? !hasActiveFilters
            : selectedStatus === status;

        return (
          <StatsCard
            key={key}
            count={count}
            label={label}
            color={color}
            total={stats.total}
            isSelected={isSelected}
            isLoading={isLoading}
            onClick={onClick || (() => handleCardClick(status))}
          />
        );
      })}
    </StaggerList>
  );
};

export default React.memo(ProcessStatsGrid);
