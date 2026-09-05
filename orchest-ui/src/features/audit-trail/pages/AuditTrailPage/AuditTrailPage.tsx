import { DataTable } from '@/shared/components/DataTable/DataTable';
import { SmartText } from '@/shared/components/SmartText/SmartText';
import AppLayout from '@/shared/layouts/AppLayout';
import { format } from 'date-fns';
import { ScrollText, ShieldAlert } from 'lucide-react';
import React, { useMemo } from 'react';
import AuditFilterBar from '../../components/AuditFilterBar/AuditFilterBar';
import MethodBadge from '../../components/MethodBadge/MethodBadge';
import StatusCode from '../../components/StatusCode/StatusCode';
import { useAuditTrail } from '../../hooks/useAuditTrail';
import type { AuditTrailEntry } from '../../types/auditTrail';
import styles from './AuditTrailPage.module.css';

const AuditTrailPage: React.FC = () => {
  const {
    mode,
    setMode,
    userParams,
    timeParams,
    pathParams,
    updateUserParams,
    updateTimeParams,
    updatePathParams,
    entries,
    isLoading,
    refetch,
  } = useAuditTrail();

  const columns = useMemo(() => [
    {
      key: 'createdAt',
      header: 'Timestamp',
      render: (row: AuditTrailEntry) => (
        <span className={styles.mono}>
          {format(new Date(row.createdAt), 'yyyy-MM-dd HH:mm:ss')}
        </span>
      ),
    },
    {
      key: 'userEmail',
      header: 'User',
      render: (row: AuditTrailEntry) => (
        <div className={styles.userCell}>
          <SmartText text={row.userEmail} maxWidth="180px" />
          {row.admin && (
            <span className={styles.adminBadge}>
              <ShieldAlert size={10} />
              ADM
            </span>
          )}
        </div>
      ),
    },
    {
      key: 'httpMethod',
      header: 'Method',
      render: (row: AuditTrailEntry) => <MethodBadge method={row.httpMethod} />,
      align: 'center' as const,
    },
    {
      key: 'path',
      header: 'Path',
      render: (row: AuditTrailEntry) => (
        <span className={styles.mono}>
          <SmartText text={row.path} maxWidth="260px" />
        </span>
      ),
    },
    {
      key: 'responseStatus',
      header: 'Status',
      render: (row: AuditTrailEntry) => <StatusCode code={row.responseStatus} />,
      align: 'center' as const,
    },
    {
      key: 'correlationId',
      header: 'Correlation ID',
      render: (row: AuditTrailEntry) => (
        <span className={styles.correlationId}>
          <SmartText text={row.correlationId} maxWidth="140px" />
        </span>
      ),
    },
  ], []);

  return (
    <AppLayout>
      <div className={styles.page}>
        <div className={styles.header}>
          <div className={styles.titleRow}>
            <div className={styles.titleIconWrap}>
              <ScrollText className={styles.titleIcon} />
            </div>
            <div>
              <h1 className={styles.title}>Audit Trail</h1>
              <p className={styles.subtitle}>Security event log &mdash; API request history</p>
            </div>
          </div>
          <div className={styles.headerMeta}>
            <span className={styles.resultCount}>
              {entries.length > 0 && `${entries.length} records`}
            </span>
          </div>
        </div>

        <AuditFilterBar
          mode={mode}
          onModeChange={setMode}
          userParams={userParams}
          timeParams={timeParams}
          pathParams={pathParams}
          onUserParamsChange={updateUserParams}
          onTimeParamsChange={updateTimeParams}
          onPathParamsChange={updatePathParams}
          onRefresh={() => refetch()}
          isLoading={isLoading}
        />

        <div className={styles.tableWrap}>
          <DataTable<AuditTrailEntry>
            data={entries}
            columns={columns}
            keyExtractor={(row) => row.id}
            loading={isLoading}
            loadingText="Fetching audit records..."
            emptyText="No audit trail entries found for the selected filters."
            showCard={true}
            stickyHeader={true}
            maxHeight="100%"
          />
        </div>
      </div>
    </AppLayout>
  );
};

export default React.memo(AuditTrailPage);
