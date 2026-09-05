import { auditTrailService } from '@/api/domains/audit-trail';
import { useApiQuery } from '@/shared/hooks';
import { useCallback, useMemo, useState } from 'react';
import type {
  AuditFilterMode,
  AuditTrailEntry,
  PathFilterParams,
  TimeFilterParams,
  UserFilterParams,
} from '../types/auditTrail';

function getDefaultTimeRange() {
  const to = new Date();
  const from = new Date(Date.now() - 24 * 60 * 60 * 1000);
  return { from: from.toISOString(), to: to.toISOString() };
}

export function useAuditTrail() {
  const defaultRange = useMemo(() => getDefaultTimeRange(), []);

  const [mode, setMode] = useState<AuditFilterMode>('time');
  const [userParams, setUserParams] = useState<UserFilterParams>({ email: '', limit: 50 });
  const [timeParams, setTimeParams] = useState<TimeFilterParams>({
    ...defaultRange,
    limit: 100,
  });
  const [pathParams, setPathParams] = useState<PathFilterParams>({
    path: '',
    ...defaultRange,
    limit: 100,
  });

  const isEnabled = useMemo(() => {
    switch (mode) {
      case 'user':
        return !!userParams.email.trim();
      case 'time':
        return !!timeParams.from && !!timeParams.to;
      case 'path':
        return !!pathParams.path.trim() && !!pathParams.from && !!pathParams.to;
    }
  }, [mode, userParams, timeParams, pathParams]);

  const queryKey = useMemo(() => {
    switch (mode) {
      case 'user':
        return ['audit-trail', 'user', userParams.email, userParams.limit];
      case 'time':
        return ['audit-trail', 'time', timeParams.from, timeParams.to, timeParams.limit];
      case 'path':
        return ['audit-trail', 'path', pathParams.path, pathParams.from, pathParams.to, pathParams.limit];
    }
  }, [mode, userParams, timeParams, pathParams]);

  const queryFn = useCallback(() => {
    switch (mode) {
      case 'user':
        return auditTrailService.getByUser(userParams);
      case 'time':
        return auditTrailService.getByTimeRange(timeParams);
      case 'path':
        return auditTrailService.getByPath(pathParams);
    }
  }, [mode, userParams, timeParams, pathParams]);

  const { data: entries = [], isLoading, error, refetch } = useApiQuery<AuditTrailEntry[]>(
    queryKey,
    queryFn,
    {
      enabled: isEnabled,
      staleTime: 30000,
      showErrorToast: true,
    }
  );

  const updateUserParams = useCallback((patch: Partial<UserFilterParams>) => {
    setUserParams((prev) => ({ ...prev, ...patch }));
  }, []);

  const updateTimeParams = useCallback((patch: Partial<TimeFilterParams>) => {
    setTimeParams((prev) => ({ ...prev, ...patch }));
  }, []);

  const updatePathParams = useCallback((patch: Partial<PathFilterParams>) => {
    setPathParams((prev) => ({ ...prev, ...patch }));
  }, []);

  return {
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
    error,
    isEnabled,
    refetch,
  };
}
