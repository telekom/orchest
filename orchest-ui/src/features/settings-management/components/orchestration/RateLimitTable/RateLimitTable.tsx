import type { RateLimit } from "@/api/domains";
import { Badge } from "@/design-system/components/ui/badge/badge";
import { DataTable, type PaginationConfig } from "@/shared/components";
import { ActionIconButton } from "@/shared/components/ActionIconButton/ActionIconButton";
import { SmartText } from "@/shared/components/SmartText/SmartText";
import { BADGE_STYLES, PAGINATION } from "@/shared/constants";
import commonStyles from "@/shared/styles/common.module.css";
import React, { useMemo } from "react";
import styles from "./RateLimitTable.module.css";

interface RateLimitTableProps {
  data: RateLimit[];
  loading: boolean;
  canEdit: boolean;
  onEdit: (config: RateLimit) => void;
  headerAction?: React.ReactNode;
  pagination?: PaginationConfig;
}

interface StatusBadgeProps {
  enabled: boolean;
}

const StatusBadge: React.FC<StatusBadgeProps> = ({ enabled }) => {
  const badgeStyle = enabled ? BADGE_STYLES.SUCCESS : BADGE_STYLES.ERROR;
  const Icon = badgeStyle.icon;
  const label = enabled ? "ENABLED" : "DISABLED";

  return (
    <Badge
      variant={badgeStyle.variant}
      icon={Icon ? <Icon /> : undefined}
      className={badgeStyle.className}
    >
      <span className="truncate" title={label}>{label}</span>
    </Badge>
  );
};

const RateLimitTable: React.FC<RateLimitTableProps> = ({
  data,
  loading,
  canEdit,
  onEdit,
  headerAction,
  pagination,
}) => {
  const paginationConfig = useMemo(() => {
    if (!pagination) return undefined;
    return {
      ...pagination,
      pageSizeOptions: PAGINATION.PAGE_SIZE_OPTIONS,
    };
  }, [pagination]);

  const columns = useMemo(() => [
    {
      key: "processId",
      header: "Process Name",
      render: (row: RateLimit) => (
        <SmartText text={row.processId} className={styles.cellText} maxWidth="100%" />
      ),
      className: commonStyles.textPrimary,
    },
    {
      key: "windowDuration",
      header: "Window Duration",
      render: (row: RateLimit) => (
        <SmartText text={String(row.windowDuration)} className={styles.cellText} maxWidth="100%" showTooltip />
      ),
      className: commonStyles.textPrimary,
      align: "center" as const,
    },
    {
      key: "allowedSize",
      header: "Allowed Size",
      render: (row: RateLimit) => (
        <SmartText text={String(row.allowedSize)} className={styles.cellText} maxWidth="100%" showTooltip />
      ),
      className: commonStyles.textPrimary,
      align: "center" as const,
    },
    {
      key: "switchToNewCamunda",
      header: "New Camunda",
      render: (row: RateLimit) => {
        const badgeStyle = row.switchToNewCamunda ? BADGE_STYLES.SUCCESS : BADGE_STYLES.ERROR;
        const Icon = badgeStyle.icon;
        const label = row.switchToNewCamunda ? "YES" : "NO";
        return (
          <Badge
            variant={badgeStyle.variant}
            icon={Icon ? <Icon /> : undefined}
            className={badgeStyle.className}
          >
            <span className="truncate" title={label}>{label}</span>
          </Badge>
        );
      },
      align: "center" as const,
    },
    {
      key: "status",
      header: "Status",
      render: (row: RateLimit) => <StatusBadge enabled={row.enabled} />,
      align: "center" as const,
    },
    ...(canEdit ? [{
      key: "actions",
      header: headerAction ?? "",
      render: (row: RateLimit) => (
        <div className={styles.actionContainer}>
          <ActionIconButton
            icon="edit-type-standard"
            onClick={() => onEdit(row)}
            title="Edit Orchestration Setting"
          />
        </div>
      ),
      className: styles.actionsColumn,
      headerClassName: styles.actionsHeaderColumn,
    }] : []),
  ], [canEdit, headerAction, onEdit]);

  return (
    <DataTable<RateLimit>
      data={data}
      columns={columns}
      keyExtractor={(row) => row.id ?? row.processId}
      loading={loading}
      loadingText="Loading..."
      emptyText="No data found with these filter settings."
      onRowClick={canEdit ? (row) => onEdit(row) : undefined}
      showCard={true}
      stickyHeader={false}
      pagination={paginationConfig}
    />
  );
};

export default React.memo(RateLimitTable);
