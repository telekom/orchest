import {
  DEFAULT_ALERT_LIST_URL_STATE,
  alertListUrlSchema,
  type AlertListUrlState,
} from '@/shared/url-state/configs/alertListUrlState';
import { useUrlFilters } from '@/shared/url-state/hooks/useUrlFilters';
import { useMemo } from 'react';

export function useAlertListUrlState() {
  const urlState = useUrlFilters<AlertListUrlState>({
    schema: alertListUrlSchema,
    defaults: DEFAULT_ALERT_LIST_URL_STATE,
    pushHistoryKeys: [
      'state',
      'processDefinitionId',
      'from',
      'to',
      'timezone',
      'sort',
      'alertId',
    ],
  });

  const hasActiveAlertFilters = useMemo(() => {
    const filters = urlState.filters;
    const defaults = DEFAULT_ALERT_LIST_URL_STATE;
    return (
      filters.state !== defaults.state ||
      filters.processDefinitionId !== defaults.processDefinitionId ||
      filters.from !== defaults.from ||
      filters.to !== defaults.to ||
      filters.timezone !== defaults.timezone ||
      filters.sort !== defaults.sort
    );
  }, [urlState.filters]);

  const { hasActiveFilters: _hasActiveFilters, ...alertUrlState } = urlState;

  return {
    ...alertUrlState,
    hasActiveAlertFilters,
  };
}

export type { AlertListUrlState };
