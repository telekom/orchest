import { SpinnerLoader } from '@/shared/components/Loader/Loader';
import { StaggerList } from '@/shared/components';
import { ApprovalState } from '@/shared/enums';
import { CheckCircle2, Clock, Inbox } from 'lucide-react';
import React, { useMemo } from 'react';
import type { EnrichedApproval } from '../../hooks/useEnrichedApprovals';
import { ApprovalCard } from './ApprovalCard';
import styles from './ApprovalListView.module.css';

interface ApprovalListViewProps {
  approvals: EnrichedApproval[];
  isLoading: boolean;
  isPending: boolean;
  onViewDetails: (approvalId: string) => void;
  onApprove: (approval: EnrichedApproval) => void;
  onDecline: (approval: EnrichedApproval) => void;
}

export const ApprovalListView: React.FC<ApprovalListViewProps> = ({
  approvals,
  isLoading,
  isPending,
  onViewDetails,
  onApprove,
  onDecline,
}) => {
  const pendingApprovals = useMemo(
    () => approvals.filter((a) => a.state === ApprovalState.REQUESTED),
    [approvals]
  );

  const resolvedCount = approvals.length - pendingApprovals.length;

  if (isLoading) {
    return (
      <div className={styles.loadingContainer}>
        <SpinnerLoader size="lg" text="Loading approvals..." />
      </div>
    );
  }

  if (approvals.length === 0) {
    return (
      <div className={styles.emptyContainer}>
        <div className={styles.emptyIconWrap}>
          <Inbox className={styles.emptyIcon} />
        </div>
        <h3 className={styles.emptyTitle}>All caught up</h3>
        <p className={styles.emptyText}>
          No deployment requests are waiting for your review.
        </p>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <div className={styles.statsRow}>
        <div className={styles.statCard}>
          <Clock className={styles.statIconPending} size={16} />
          <div>
            <span className={styles.statValue}>{pendingApprovals.length}</span>
            <span className={styles.statLabel}>Pending</span>
          </div>
        </div>
        <div className={styles.statCard}>
          <CheckCircle2 className={styles.statIconResolved} size={16} />
          <div>
            <span className={styles.statValue}>{resolvedCount}</span>
            <span className={styles.statLabel}>Reviewed</span>
          </div>
        </div>
        <div className={styles.statCardWide}>
          <span className={styles.statLabel}>Total requests</span>
          <span className={styles.statValueInline}>{approvals.length}</span>
        </div>
      </div>

      <StaggerList className={styles.approvalsGrid}>
        {approvals.map((approval) => (
          <ApprovalCard
            key={approval.id}
            approval={approval}
            isPending={isPending}
            onViewDetails={() => onViewDetails(approval.id)}
            onApprove={(e) => {
              e.stopPropagation();
              onApprove(approval);
            }}
            onDecline={(e) => {
              e.stopPropagation();
              onDecline(approval);
            }}
          />
        ))}
      </StaggerList>
    </div>
  );
};

export type { EnrichedApproval };
