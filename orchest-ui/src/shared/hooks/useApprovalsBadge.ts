import { deploymentApprovalsService } from '@/api/domains';
import type { DeploymentApprovalDTO } from '@/api/types/orchest-api';
import { queryKeys } from '@/shared/constants/queryKeys';
import { TIMING } from '@/shared/constants';
import { ApprovalState } from '@/shared/enums';
import { useAuth, UserRoles } from '@/shared/auth';
import { useMemo } from 'react';
import { useApiQuery } from './useApiQuery';

export const useApprovalsBadge = () => {
  const { isAuthenticated, hasRole } = useAuth();
  const isAdmin = hasRole(UserRoles.ADMIN, true);

  const { data: approvals = [], isLoading, refetch } = useApiQuery(
    queryKeys.deploymentApprovals.reviews(),
    () => deploymentApprovalsService.getReviewApprovals(),
    {
      enabled: isAuthenticated && isAdmin,
      showErrorToast: false,
      refetchInterval: isAuthenticated && isAdmin ? TIMING.APPROVALS_POLL_INTERVAL : false,
    }
  );

  const pendingApprovals = useMemo(
    () =>
      approvals.filter(
        (approval: DeploymentApprovalDTO) => approval.state === ApprovalState.REQUESTED
      ),
    [approvals]
  );

  const pendingCount = pendingApprovals.length;

  return {
    pendingApprovals,
    pendingCount,
    isLoading,
    hasPendingApprovals: pendingCount > 0,
    refetch,
  };
};
