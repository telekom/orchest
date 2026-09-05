import type { AlertResponse } from '@/api/domains/alerts';
import { Badge } from '@/design-system/components/ui/badge/badge';
import { DataTable } from '@/shared/components/DataTable/DataTable';
import {
  toSortParam,
  type Column,
  type SortConfig,
} from '@/shared/components/DataTable/types';
import commonStyles from '@/shared/styles/common.module.css';
import {
  ALERT_SORT_VALUES,
  type AlertListUrlState,
} from '@/shared/url-state/configs/alertListUrlState';
import { useMemo, type ReactNode } from 'react';
import { Link } from 'react-router-dom';
import {
  getAlertSeverityBadgeConfig,
  getAlertStateBadgeConfig,
  getAlertVersion,
} from '../../utils/alertBadges';
import styles from './AlertTable.module.css';

type AlertSortToken = (typeof ALERT_SORT_VALUES)[number];

export interface AlertTableProps {
  alerts: AlertResponse[];
  page: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  sort: AlertListUrlState['sort'];
  selectedAlertId?: string | null;
  isLoading?: boolean;
  error?: ReactNode;
  loadingText?: string;
  emptyText?: string;
  onSelect: (id: string) => void;
  onSort: (sortToken: AlertSortToken) => void;
  onPageChange: (page: number) => void;
  onPageSizeChange?: (size: number) => void;
}

const formatDate = (value?: string) => (value ? new Date(value).toLocaleString() : '—');

const parseSort = (sort: string): SortConfig | undefined => {
  if (!ALERT_SORT_VALUES.includes(sort as AlertSortToken)) return undefined;

  return {
    field: sort.slice(1),
    direction: sort.startsWith('-') ? 'desc' : 'asc',
  };
};

export function AlertTable({
  alerts,
  page,
  pageSize,
  totalElements,
  totalPages,
  sort,
  selectedAlertId,
  isLoading = false,
  error,
  loadingText = 'Loading alerts…',
  emptyText = 'No alerts found with the current filters.',
  onSelect,
  onSort,
  onPageChange,
  onPageSizeChange,
}: Readonly<AlertTableProps>) {
  const columns = useMemo<Column<AlertResponse>[]>(
    () => [
      {
        key: 'severity',
        header: 'Severity',
        render: (alert) => {
          const cfg = getAlertSeverityBadgeConfig(alert.severity);
          const Icon = cfg.icon;
          return alert.severity ? (
            <Badge variant={cfg.variant} icon={<Icon />}>
              {cfg.label}
            </Badge>
          ) : (
            <span className={styles.muted}>—</span>
          );
        },
        sortable: true,
      },
      {
        key: 'state',
        header: 'State',
        render: (alert) => {
          const cfg = getAlertStateBadgeConfig(alert.state);
          const Icon = cfg.icon;
          return (
            <Badge variant={cfg.variant} icon={<Icon />}>
              {cfg.label}
            </Badge>
          );
        },
        sortable: true,
      },
      {
        key: 'subject',
        header: 'Subject',
        render: (alert) => alert.subject ?? '—',
        className: styles.subject,
      },
      {
        key: 'process',
        header: 'Process',
        render: (alert) => alert.metadata?.processDefinitionId ?? '—',
        className: styles.process,
      },
      {
        key: 'processInstance',
        header: 'Instance',
        render: (alert) => {
          const processInstanceId = alert.metadata?.processInstanceId?.trim();
          if (!processInstanceId) {
            return <span className={styles.muted}>—</span>;
          }

          return (
            <Link
              to={`/processes/${encodeURIComponent(processInstanceId)}`}
              className={commonStyles.link}
              onClick={(event) => event.stopPropagation()}
              title={processInstanceId}
            >
              {processInstanceId}
            </Link>
          );
        },
        className: styles.process,
      },
      {
        key: 'version',
        header: 'Version',
        render: (alert) => getAlertVersion(alert.metadata),
        className: styles.version,
      },
      {
        key: 'count',
        header: 'Count',
        align: 'right',
        sortable: true,
      },
      {
        key: 'lastTriggeredAt',
        header: 'Last triggered',
        render: (alert) => formatDate(alert.lastTriggeredAt),
        className: styles.date,
      },
      {
        key: 'updatedAt',
        header: 'Updated',
        render: (alert) => formatDate(alert.updatedAt),
        className: styles.date,
        sortable: true,
      },
    ],
    [],
  );

  const handleSortChange = (sorts: SortConfig[]) => {
    const nextSort = sorts[0];
    if (!nextSort) return;

    const token = toSortParam(nextSort);
    if (ALERT_SORT_VALUES.includes(token as AlertSortToken)) {
      onSort(token as AlertSortToken);
    }
  };

  if (error) {
    return (
      <div className={styles.error} role="alert">
        {error}
      </div>
    );
  }

  return (
    <DataTable
      data={alerts}
      columns={columns}
      keyExtractor={(alert) => alert.id}
      loading={isLoading}
      loadingText={loadingText}
      emptyText={emptyText}
      onRowClick={(alert) => onSelect(alert.id)}
      selectedRowKey={selectedAlertId ?? undefined}
      stickyHeader
      showCard
      sortConfig={parseSort(sort)}
      onSortChange={handleSortChange}
      pagination={{
        currentPage: page,
        totalPages,
        totalElements,
        pageSize,
        onPageSizeChange,
        onPageChange: (direction) => {
          let nextPage: number;
          if (direction === 'next') nextPage = page + 1;
          else if (direction === 'prev') nextPage = page - 1;
          else nextPage = direction;
          onPageChange(nextPage);
        },
      }}
    />
  );
}
