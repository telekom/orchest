import { TIMING } from '@/shared/constants';
import { toError } from '@/shared/hooks/useErrorHandling';
import { useStableCallback } from '@/shared/hooks/useStableCallback';
import { clearIntervalRef, clearTimeoutRef } from '@/shared/utils/timerUtils';
import { useCallback, useEffect, useRef, useState } from 'react';

export enum PollingStatus {
  IDLE = 'idle',
  POLLING = 'polling',
  SUCCESS = 'success',
  ERROR = 'error',
  TIMEOUT = 'timeout',
}

export interface PollResult {
  shouldContinue: boolean;
  data?: unknown;
  error?: Error;
}

export interface UsePollingOptions {
  interval?: number;
  timeout?: number | null;
  startImmediately?: boolean;
  onSuccess?: (data?: unknown) => void;
  onError?: (error: Error) => void;
  onTimeout?: () => void;
}

/**
 * Hook for polling with automatic timeout and cleanup
 */
export function usePolling(options: UsePollingOptions = {}) {
  const {
    interval = TIMING.POLLING_INTERVAL,
    timeout = TIMING.POLLING_TIMEOUT,
    onSuccess,
    onError,
    onTimeout,
  } = options;

  const [status, setStatus] = useState<PollingStatus>(PollingStatus.IDLE);
  const [data, setData] = useState<unknown>(undefined);
  const [error, setError] = useState<Error | null>(null);

  const intervalIdRef = useRef<NodeJS.Timeout | null>(null);
  const timeoutIdRef = useRef<NodeJS.Timeout | null>(null);
  const pollFnRef = useRef<(() => Promise<PollResult>) | null>(null);
  const isMountedRef = useRef(true);

  const cleanup = useCallback(() => {
    clearIntervalRef(intervalIdRef);
    clearTimeoutRef(timeoutIdRef);
  }, []);

  const stableOnError = useStableCallback(onError);
  const stableOnSuccess = useStableCallback(onSuccess);
  const stableOnTimeout = useStableCallback(onTimeout);

  const handleError = useCallback((err: Error) => {
    if (!isMountedRef.current) return;
    cleanup();
    setStatus(PollingStatus.ERROR);
    setError(err);
    stableOnError?.(err);
  }, [cleanup, stableOnError]);

  const handleSuccess = useCallback((resultData?: unknown) => {
    if (!isMountedRef.current) return;
    cleanup();
    setStatus(PollingStatus.SUCCESS);
    setData(resultData);
    stableOnSuccess?.(resultData);
  }, [cleanup, stableOnSuccess]);

  const stopPolling = useCallback(() => {
    cleanup();
    if (isMountedRef.current) {
      setStatus(PollingStatus.IDLE);
    }
  }, [cleanup]);

  const executePoll = useCallback(async () => {
    if (!isMountedRef.current || !pollFnRef.current) return;

    try {
      const result = await pollFnRef.current();
      if (!isMountedRef.current) return;

      if (result.error) {
        handleError(result.error);
        return;
      }

      if (!result.shouldContinue) {
        handleSuccess(result.data);
      }
    } catch (err) {
      if (!isMountedRef.current) return;
      handleError(toError(err));
    }
  }, [handleError, handleSuccess]);

  const startPolling = useCallback(
    (pollFn: () => Promise<PollResult>) => {
      cleanup();

      if (isMountedRef.current) {
        setStatus(PollingStatus.POLLING);
        setData(undefined);
        setError(null);
      }

      pollFnRef.current = pollFn;
      intervalIdRef.current = setInterval(executePoll, interval);

      if (timeout !== null) {
        timeoutIdRef.current = setTimeout(() => {
          if (!isMountedRef.current) return;
          cleanup();
          setStatus(PollingStatus.TIMEOUT);
          stableOnTimeout?.();
        }, timeout);
      }
    },
    [interval, timeout, stableOnTimeout, cleanup, executePoll]
  );

  const reset = useCallback(() => {
    cleanup();
    if (isMountedRef.current) {
      setStatus(PollingStatus.IDLE);
      setData(undefined);
      setError(null);
    }
  }, [cleanup]);

  useEffect(() => {
    return () => {
      isMountedRef.current = false;
      cleanup();
    };
  }, [cleanup]);

  return {
    startPolling,
    stopPolling,
    reset,
    status,
    data,
    error,
    isPolling: status === PollingStatus.POLLING,
    isSuccess: status === PollingStatus.SUCCESS,
    isError: status === PollingStatus.ERROR,
    isTimeout: status === PollingStatus.TIMEOUT,
  };
}
