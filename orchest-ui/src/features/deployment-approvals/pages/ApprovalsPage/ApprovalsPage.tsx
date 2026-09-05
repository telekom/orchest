import { deploymentApprovalsService } from '@/api/domains';
import { Button } from '@/design-system/components/ui/button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/design-system/components/ui/tabs';
import { useAuth } from '@/shared/auth';
import { queryKeys } from '@/shared/constants/queryKeys';
import { ApprovalState } from '@/shared/enums';
import { useApprovalsBadge } from '@/shared/hooks/useApprovalsBadge';
import { useApiMutation, useApiQuery } from '@/shared/hooks/useApiQuery';
import AppLayout from '@/shared/layouts/AppLayout';
import { CheckSquare, RefreshCw, Users } from 'lucide-react';
import React, { useCallback } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { ApprovalListView } from '../../components/ApprovalListView/ApprovalListView';
import { DeputyManagementView } from '../../components/DeputyManagementView/DeputyManagementView';
import { useEnrichedApprovals } from '../../hooks/useEnrichedApprovals';
import styles from './ApprovalsPage.module.css';

type TabValue = 'approvals' | 'ownership';

const ApprovalsPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const { pendingCount, refetch: refetchBadge } = useApprovalsBadge();

  const activeTab: TabValue =
    searchParams.get('tab') === 'ownership' ? 'ownership' : 'approvals';

  const { data: approvals = [], isLoading, refetch, isFetching } = useApiQuery(
    queryKeys.deploymentApprovals.reviews(),
    () => deploymentApprovalsService.getReviewApprovals(),
    { showErrorToast: true, staleTime: 30000 }
  );

  const enrichedApprovals = useEnrichedApprovals(approvals);

  const submitApprovalMutation = useApiMutation(
    (variables: { deploymentRequestId: string; state: ApprovalState }) =>
      deploymentApprovalsService.submitApproval({
        deploymentRequestId: variables.deploymentRequestId,
        approver: user?.email || '',
        state: variables.state,
      }),
    {
      showSuccessToast: true,
      successMessage: 'Approval submitted successfully',
      invalidateQueries: [queryKeys.deploymentApprovals.reviews()],
    }
  );

  const handleRefresh = useCallback(() => {
    refetch();
    refetchBadge();
  }, [refetch, refetchBadge]);

  const handleTabChange = (value: string) => {
    if (value === 'ownership') {
      setSearchParams({ tab: 'ownership' });
    } else {
      setSearchParams({});
    }
  };

  const handleViewDetails = (approvalId: string) => {
    navigate(`/approvals/${approvalId}`);
  };

  const handleApproveDecline = (
    approvalId: string,
    state: ApprovalState
  ) => {
    submitApprovalMutation.mutate({ deploymentRequestId: approvalId, state });
  };

  return (
    <AppLayout>
      <div className={styles.page}>
        <header className={styles.header}>
          <div className={styles.titleRow}>
            <div className={styles.titleIconWrap}>
              <CheckSquare className={styles.titleIcon} />
            </div>
            <div>
              <h1 className={styles.title}>Deployment Approvals</h1>
              <p className={styles.subtitle}>
                Review pending deployments &amp; manage process ownership
              </p>
            </div>
          </div>
          <div className={styles.headerMeta}>
            {activeTab === 'approvals' && pendingCount > 0 && (
              <span className={styles.pendingPill}>
                <span className={styles.pendingDot} />
                {pendingCount} pending
              </span>
            )}
            <Button
              variant="outline"
              size="sm"
              onClick={handleRefresh}
              disabled={isFetching}
              aria-label="Refresh approvals"
            >
              <RefreshCw className={isFetching ? styles.spinning : undefined} size={14} />
              Refresh
            </Button>
          </div>
        </header>

        <Tabs value={activeTab} onValueChange={handleTabChange} className={styles.tabs}>
          <TabsList className={styles.tabsList}>
            <TabsTrigger value="approvals" className={styles.tabTrigger}>
              <CheckSquare size={14} className={styles.tabIcon} aria-hidden />
              Pending Approvals
              {pendingCount > 0 && (
                <span className={styles.tabBadge}>{pendingCount}</span>
              )}
            </TabsTrigger>
            <TabsTrigger value="ownership" className={styles.tabTrigger}>
              <Users size={14} className={styles.tabIcon} aria-hidden />
              Manage Ownership
            </TabsTrigger>
          </TabsList>

          <TabsContent value="approvals" className={styles.tabPanel}>
            <ApprovalListView
              approvals={enrichedApprovals}
              isLoading={isLoading}
              isPending={submitApprovalMutation.isPending}
              onViewDetails={handleViewDetails}
              onApprove={(approval) =>
                handleApproveDecline(approval.id, ApprovalState.ACCEPTED)
              }
              onDecline={(approval) =>
                handleApproveDecline(approval.id, ApprovalState.REJECTED)
              }
            />
          </TabsContent>

          <TabsContent value="ownership" className={styles.tabPanel}>
            <div className={styles.ownershipPanel}>
              <DeputyManagementView />
            </div>
          </TabsContent>
        </Tabs>
      </div>
    </AppLayout>
  );
};

export default React.memo(ApprovalsPage);
