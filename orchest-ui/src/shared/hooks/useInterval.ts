import { useCallback, useEffect, useRef } from 'react';
import { clearIntervalRef } from '@/shared/utils/timerUtils';

export interface UseIntervalOptions {
  /** Callback to execute on each interval */
  callback: () => void | Promise<void>;
  /** Interval delay in milliseconds. Pass null to pause the interval */
  delay: number | null;
  /** Whether to run the callback immediately on mount */
  immediate?: boolean;
  /** Whether the interval is enabled */
  enabled?: boolean;
}

export interface UseIntervalReturn {
  /** Manually trigger the callback */
  trigger: () => void;
  /** Reset the interval timer */
  reset: () => void;
}

/**
 * Hook for managing intervals with automatic cleanup
 * 
 * Features:
 * - Automatic cleanup on unmount
 * - Stable callback reference (no stale closure issues)
 * - Pause/resume by setting delay to null
 * - Optional immediate execution on mount
 * - Manual trigger capability
 * 
 * @example
 * ```tsx
 * // Basic usage
 * import { logger } from '@/shared/utils/logger';
 *
 * useInterval({
 *   callback: () => logger.info('tick'),
 *   delay: 1000
 * });
 * 
 * // With pause capability
 * const [isPaused, setIsPaused] = useState(false);
 * useInterval({
 *   callback: fetchData,
 *   delay: isPaused ? null : 5000
 * });
 * 
 * // With immediate execution
 * useInterval({
 *   callback: refreshToken,
 *   delay: 60000,
 *   immediate: true
 * });
 * ```
 */
export function useInterval(options: UseIntervalOptions): UseIntervalReturn {
  const { callback, delay, immediate = false, enabled = true } = options;
  
  const savedCallback = useRef(callback);
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const isMountedRef = useRef(true);

  // Keep callback ref in sync
  useEffect(() => {
    savedCallback.current = callback;
  }, [callback]);

  // Track mounted state
  useEffect(() => {
    isMountedRef.current = true;
    return () => {
      isMountedRef.current = false;
    };
  }, []);

  const trigger = useCallback(() => {
    if (isMountedRef.current) {
      savedCallback.current();
    }
  }, []);

  const reset = useCallback(() => {
    clearIntervalRef(intervalRef);

    if (delay !== null && enabled && isMountedRef.current) {
      intervalRef.current = setInterval(() => {
        if (isMountedRef.current) {
          savedCallback.current();
        }
      }, delay);
    }
  }, [delay, enabled]);

  useEffect(() => {
    if (delay === null || !enabled) {
      clearIntervalRef(intervalRef);
      return;
    }

    if (immediate) {
      savedCallback.current();
    }

    intervalRef.current = setInterval(() => {
      if (isMountedRef.current) {
        savedCallback.current();
      }
    }, delay);

    return () => clearIntervalRef(intervalRef);
  }, [delay, enabled, immediate]);

  return { trigger, reset };
}

/**
 * Simplified useInterval that just takes callback and delay
 */
export function useSimpleInterval(
  callback: () => void | Promise<void>,
  delay: number | null
): void {
  useInterval({ callback, delay });
}

export default useInterval;
