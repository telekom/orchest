import { deploymentApprovalsService } from '@/api/domains';
import { useAuth } from '@/shared/auth';
import { PageLoader } from '@/shared/components';
import { queryKeys } from '@/shared/constants/queryKeys';
import { ApprovalState } from '@/shared/enums';
import { useApiMutation, useApiQuery } from '@/shared/hooks/useApiQuery';
import AppLayout from '@/shared/layouts/AppLayout';
import React, { useMemo } from 'react';
import { Navigate, useNavigate, useParams } from 'react-router-dom';
import { ApprovalDetailView } from '../../components/ApprovalDetailView/ApprovalDetailView';
import { useEnrichedApprovals } from '../../hooks/useEnrichedApprovals';
import styles from './ApprovalDetailPage.module.css';

const ApprovalDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();

  const { data: approvals = [], isLoading } = useApiQuery(
    queryKeys.deploymentApprovals.reviews(),
    () => deploymentApprovalsService.getReviewApprovals(),
    { showErrorToast: true, staleTime: 30000 }
  );

  const enrichedApprovals = useEnrichedApprovals(approvals);

  const approval = useMemo(
    () => enrichedApprovals.find((item) => item.id === id),
    [enrichedApprovals, id]
  );

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

  if (isLoading) {
    return (
      <AppLayout>
        <PageLoader text="Loading approval details..." />
      </AppLayout>
    );
  }

  if (!approval) {
    return <Navigate to="/approvals" replace />;
  }

  const handleBack = () => navigate('/approvals');

  const handleDecision = (state: ApprovalState) => {
    submitApprovalMutation.mutate(
      { deploymentRequestId: approval.id, state },
      { onSuccess: handleBack }
    );
  };

  return (
    <AppLayout>
      <div className={styles.page}>
        <ApprovalDetailView
          approval={approval}
          onBack={handleBack}
          onApprove={() => handleDecision(ApprovalState.ACCEPTED)}
          onDecline={() => handleDecision(ApprovalState.REJECTED)}
          isPending={submitApprovalMutation.isPending}
        />
      </div>
    </AppLayout>
  );
};

export default React.memo(ApprovalDetailPage);
