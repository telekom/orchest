import { Badge } from '@/design-system/components/ui/badge/badge';
import { ActionIconButton } from '@/shared/components/ActionIconButton/ActionIconButton';
import { STATE_BADGE_VARIANTS } from '@/shared/constants/approvalConfig';
import { ApprovalState } from '@/shared/enums';
import clsx from 'clsx';
import { Calendar, FileCode, User } from 'lucide-react';
import React from 'react';
import type { EnrichedApproval } from '../../hooks/useEnrichedApprovals';
import styles from './ApprovalListView.module.css';

interface ApprovalCardProps {
  approval: EnrichedApproval;
  isPending: boolean;
  onViewDetails: () => void;
  onApprove: (e: React.MouseEvent) => void;
  onDecline: (e: React.MouseEvent) => void;
}

const formatDate = (dateString: string) =>
  new Date(dateString).toLocaleDateString(undefined, {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  });

export const ApprovalCard: React.FC<ApprovalCardProps> = ({
  approval,
  isPending: _isPending,
  onViewDetails,
  onApprove,
  onDecline,
}) => {
  const isRequested = approval.state === ApprovalState.REQUESTED;

  return (
    <article
      onClick={onViewDetails}
      className={clsx(
        styles.approvalCard,
        isRequested && styles.approvalCardPending
      )}
    >
      <div className={styles.cardHeader}>
        <div className={styles.cardTitleGroup}>
          <FileCode className={styles.fileIcon} />
          <div>
            <h3 className={styles.definitionId}>
              {approval.definitionInfo.definitionId}
            </h3>
            {approval.definitionInfo.definitionName && (
              <p className={styles.definitionName}>
                {approval.definitionInfo.definitionName}
              </p>
            )}
          </div>
        </div>
        <Badge
          variant={STATE_BADGE_VARIANTS[approval.state] ?? 'secondary'}
          className={styles.stateBadge}
        >
          {approval.state}
        </Badge>
      </div>

      <div className={styles.cardMeta}>
        <span className={styles.metaChip}>{approval.resourceType}</span>
        <span className={styles.metaChip}>
          <User className={styles.metaChipIcon} />
          {approval.requestedBy}
        </span>
        {approval.nextVersion && (
          <span className={styles.versionBadge}>v{approval.nextVersion}</span>
        )}
      </div>

      <div className={styles.cardFooter}>
        <span className={styles.requestedDate}>
          <Calendar className={styles.dateIcon} />
          {formatDate(approval.createdAt)}
        </span>
        {isRequested && (
          <div className={styles.actionsContainer}>
            <ActionIconButton
              icon="close"
              onClick={onDecline}
              title="Decline approval"
              variant="danger"
            />
            <ActionIconButton
              icon="checkmark-type-standard"
              onClick={onApprove}
              title="Approve deployment"
              variant="success"
            />
          </div>
        )}
      </div>
    </article>
  );
};
