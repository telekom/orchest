import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/design-system/components/ui/dropdown-menu';
import { useAuth, UserRoles } from '@/shared/auth';
import { ROUTES } from '@/shared/constants/routes';
import { useApprovalsBadge } from '@/shared/hooks/useApprovalsBadge';
import { extractDefinitionInfo } from '@/shared/utils';
import clsx from 'clsx';
import { Bell, CheckSquare, ExternalLink } from 'lucide-react';
import React, { useMemo } from 'react';
import { Link } from 'react-router-dom';
import styles from './ApprovalsBellButton.module.css';

const formatRequestedAt = (iso: string | undefined): string => {
  if (!iso) return '';
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) return '';
  return date.toLocaleString(undefined, {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

export const ApprovalsBellButton: React.FC = () => {
  const { hasRole, isAuthenticated } = useAuth();
  const isAdmin = hasRole(UserRoles.ADMIN, true);
  const { pendingApprovals, pendingCount, hasPendingApprovals, isLoading } =
    useApprovalsBadge();

  const items = useMemo(
    () =>
      pendingApprovals.map((approval) => {
        const info = extractDefinitionInfo(
          approval.resourceDeploymentRequest?.resourceUTF8XML ?? ''
        );
        const title =
          info.definitionName ||
          info.definitionId ||
          approval.resourceType ||
          'Deployment request';
        return {
          id: approval.id,
          title,
          subtitle: [
            approval.resourceType?.toUpperCase(),
            approval.nextVersion ? `v${approval.nextVersion}` : null,
            approval.requestedBy ? `by ${approval.requestedBy}` : null,
          ]
            .filter(Boolean)
            .join(' · '),
          requestedAt: formatRequestedAt(approval.createdAt),
        };
      }),
    [pendingApprovals]
  );

  if (!isAuthenticated || !isAdmin) {
    return null;
  }

  const pendingLabel =
    pendingCount === 1 ? '1 pending approval' : `${pendingCount} pending approvals`;

  return (
    <DropdownMenu modal={false}>
      <DropdownMenuTrigger asChild>
        <button
          type="button"
          className={clsx(styles.bellButton, hasPendingApprovals && styles.bellButtonActive)}
          aria-label={hasPendingApprovals ? pendingLabel : 'Approvals'}
        >
          <Bell size={18} strokeWidth={2.1} className={styles.bellIcon} />
          {hasPendingApprovals && (
            <span className={styles.badge} aria-hidden="true">
              {pendingCount > 99 ? '99+' : pendingCount}
            </span>
          )}
        </button>
      </DropdownMenuTrigger>

      <DropdownMenuContent align="end" className={styles.dropdownContent}>
        <div className={styles.dropdownHeader}>
          <span className={styles.dropdownTitle}>Pending approvals</span>
          {hasPendingApprovals && (
            <span className={styles.dropdownCount}>{pendingCount}</span>
          )}
        </div>

        {isLoading && items.length === 0 && (
          <div className={styles.emptyState}>Loading approvals…</div>
        )}

        {!isLoading && items.length === 0 && (
          <div className={styles.emptyState}>No pending approvals</div>
        )}

        {items.map((item) => (
          <DropdownMenuItem
            key={item.id}
            className={styles.menuItem}
            asChild
          >
            <Link to={`${ROUTES.APPROVALS}/${item.id}`}>
              <CheckSquare size={14} className={styles.itemIcon} aria-hidden />
              <div className={styles.itemBody}>
                <span className={styles.itemTitle}>{item.title}</span>
                {item.subtitle && (
                  <span className={styles.itemSubtitle}>{item.subtitle}</span>
                )}
                {item.requestedAt && (
                  <span className={styles.itemMeta}>{item.requestedAt}</span>
                )}
              </div>
            </Link>
          </DropdownMenuItem>
        ))}

        <DropdownMenuSeparator className={styles.separator} />

        <DropdownMenuItem
          className={styles.viewAllItem}
          asChild
        >
          <Link to={ROUTES.APPROVALS}>
            <ExternalLink size={14} className={styles.itemIcon} aria-hidden />
            <span>View all approvals</span>
          </Link>
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
};

export default ApprovalsBellButton;
