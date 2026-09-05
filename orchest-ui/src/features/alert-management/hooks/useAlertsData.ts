import { alertService, type AlertState } from '@/api/domains/alerts';
import { toast } from '@/design-system/components/ui/sonner';
import { queryKeys } from '@/shared/constants/queryKeys';
import { useApiMutation, useApiQuery } from '@/shared/hooks/useApiQuery';
import type { AxiosError } from 'axios';
import type { AlertListUrlState } from './useAlertListUrlState';

const COUNT_QUERY_OPTIONS = {
  showErrorToast: false,
  staleTime: 15_000,
};

const getMutationErrorMessage = (error: AxiosError, fallback: string) => {
  const responseData = error.response?.data;
  if (
    responseData &&
    typeof responseData === 'object' &&
    'message' in responseData &&
    typeof responseData.message === 'string'
  ) {
    return responseData.message;
  }
  return fallback;
};

export type OpsAlertState = AlertState;

export function useAlertsData(filters: AlertListUrlState) {
  const listParams = {
    state: filters.state,
    processDefinitionId: filters.processDefinitionId,
    createdFrom: filters.from,
    createdTo: filters.to,
    timezone: filters.timezone,
    page: filters.page,
    size: filters.size,
    sort: filters.sort,
  };

  const listQuery = useApiQuery(
    queryKeys.alerts.list(listParams),
    () => alertService.listAlerts(listParams),
    { showErrorToast: true, staleTime: 15_000 },
  );

  const firingStatsQuery = useApiQuery(
    queryKeys.alerts.firingStats(),
    () => alertService.getFiringStats(),
    { showErrorToast: true, staleTime: 15_000 },
  );

  const sharedCountFilters = {
    processDefinitionId: filters.processDefinitionId,
    createdFrom: filters.from,
    createdTo: filters.to,
    timezone: filters.timezone,
  };

  const firingCountFilters = { ...sharedCountFilters, state: 'FIRING' as const };
  const firingCountQuery = useApiQuery(
    queryKeys.alerts.stateCount(firingCountFilters),
    () =>
      alertService.listAlerts({
        ...firingCountFilters,
        page: 0,
        size: 1,
        sort: '-count',
      }),
    COUNT_QUERY_OPTIONS,
  );

  const silencedCountFilters = { ...sharedCountFilters, state: 'SILENCED' as const };
  const silencedCountQuery = useApiQuery(
    queryKeys.alerts.stateCount(silencedCountFilters),
    () =>
      alertService.listAlerts({
        ...silencedCountFilters,
        page: 0,
        size: 1,
        sort: '-count',
      }),
    COUNT_QUERY_OPTIONS,
  );

  const disabledCountFilters = { ...sharedCountFilters, state: 'DISABLED' as const };
  const disabledCountQuery = useApiQuery(
    queryKeys.alerts.stateCount(disabledCountFilters),
    () =>
      alertService.listAlerts({
        ...disabledCountFilters,
        page: 0,
        size: 1,
        sort: '-count',
      }),
    COUNT_QUERY_OPTIONS,
  );

  const acknowledgedCountFilters = { ...sharedCountFilters, state: 'ACKNOWLEDGED' as const };
  const acknowledgedCountQuery = useApiQuery(
    queryKeys.alerts.stateCount(acknowledgedCountFilters),
    () =>
      alertService.listAlerts({
        ...acknowledgedCountFilters,
        page: 0,
        size: 1,
        sort: '-count',
      }),
    COUNT_QUERY_OPTIONS,
  );

  const resolvedCountFilters = { ...sharedCountFilters, state: 'RESOLVED' as const };
  const resolvedCountQuery = useApiQuery(
    queryKeys.alerts.stateCount(resolvedCountFilters),
    () =>
      alertService.listAlerts({
        ...resolvedCountFilters,
        page: 0,
        size: 1,
        sort: '-count',
      }),
    COUNT_QUERY_OPTIONS,
  );

  const detailQuery = useApiQuery(
    queryKeys.alerts.detail(filters.alertId ?? ''),
    () => alertService.getAlert(filters.alertId!),
    {
      enabled: !!filters.alertId,
      showErrorToast: false,
      staleTime: 10_000,
    },
  );

  const acknowledgeMutation = useApiMutation(
    ({ id, actor }: { id: string; actor: string }) =>
      alertService.acknowledge(id, { actor }),
    {
      showSuccessToast: true,
      successMessage: 'Alert acknowledged',
      onError: (error) =>
        toast.error(getMutationErrorMessage(error, 'Failed to acknowledge alert.')),
      invalidateQueries: [queryKeys.alerts.all],
    },
  );

  const silenceMutation = useApiMutation(
    ({ id, actor }: { id: string; actor: string }) =>
      alertService.silence(id, { actor }),
    {
      showSuccessToast: true,
      successMessage: 'Alert silenced',
      onError: (error) =>
        toast.error(getMutationErrorMessage(error, 'Failed to silence alert.')),
      invalidateQueries: [queryKeys.alerts.all],
    },
  );

  const unmuteMutation = useApiMutation(
    (id: string) => alertService.unmute(id),
    {
      showSuccessToast: true,
      successMessage: 'Alert unmuted',
      onError: (error) => toast.error(getMutationErrorMessage(error, 'Failed to unmute alert.')),
      invalidateQueries: [queryKeys.alerts.all],
    },
  );

  const resolveMutation = useApiMutation(
    (id: string) => alertService.resolve(id),
    {
      showSuccessToast: true,
      successMessage: 'Alert resolved',
      onError: (error) => toast.error(getMutationErrorMessage(error, 'Failed to resolve alert.')),
      invalidateQueries: [queryKeys.alerts.all],
    },
  );

  const firing = firingCountQuery.data?.totalElements ?? 0;
  const silenced = silencedCountQuery.data?.totalElements ?? 0;
  const disabled = disabledCountQuery.data?.totalElements ?? 0;
  const acknowledged = acknowledgedCountQuery.data?.totalElements ?? 0;
  const resolved = resolvedCountQuery.data?.totalElements ?? 0;

  return {
    listQuery,
    firingStatsQuery,
    stateCounts: {
      FIRING: firing,
      SILENCED: silenced,
      DISABLED: disabled,
      ACKNOWLEDGED: acknowledged,
      RESOLVED: resolved,
    } satisfies Record<OpsAlertState, number>,
    activeTotal: firing + silenced + disabled + acknowledged + resolved,
    stateCountsLoading: firingCountQuery.isLoading || silencedCountQuery.isLoading || disabledCountQuery.isLoading || acknowledgedCountQuery.isLoading || resolvedCountQuery.isLoading,
    detailQuery,
    acknowledgeMutation,
    silenceMutation,
    unmuteMutation,
    resolveMutation,
  };
}
