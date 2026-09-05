import {
  useOrchestDashboardData,
  type UseOrchestDashboardDataOptions,
} from '@/features/process-management/hooks/useOrchestDashboardData';

/**
 * Usage page stats — same consolidated `/orchest/stats` feed as the Dashboard.
 */
export function useUsageStats(options: UseOrchestDashboardDataOptions = {}) {
  return useOrchestDashboardData(options);
}
