import { MutableRefObject } from 'react';

/**
 * Timer utility functions for consistent timer management across the codebase.
 * These utilities ensure proper cleanup and null-safety when working with timers.
 */

type TimerId = ReturnType<typeof setTimeout> | ReturnType<typeof setInterval>;

/**
 * Safely clears an interval stored in a ref and sets the ref to null
 * 
 * @example
 * ```tsx
 * const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);
 * 
 * useEffect(() => {
 *   intervalRef.current = setInterval(() => {}, 1000);
 *   return () => clearIntervalRef(intervalRef);
 * }, []);
 * ```
 */
export const clearIntervalRef = (
  timerRef: MutableRefObject<TimerId | null>
): void => {
  if (timerRef.current !== null) {
    clearInterval(timerRef.current);
    timerRef.current = null;
  }
};

/**
 * Safely clears a timeout stored in a ref and sets the ref to null
 * 
 * @example
 * ```tsx
 * const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
 * 
 * useEffect(() => {
 *   timeoutRef.current = setTimeout(() => {}, 1000);
 *   return () => clearTimeoutRef(timeoutRef);
 * }, []);
 * ```
 */
export const clearTimeoutRef = (
  timerRef: MutableRefObject<TimerId | null>
): void => {
  if (timerRef.current !== null) {
    clearTimeout(timerRef.current);
    timerRef.current = null;
  }
};

/**
 * Clears multiple timer refs at once
 * Useful in cleanup functions with multiple timers
 * 
 * @example
 * ```tsx
 * return () => clearAllTimerRefs([intervalRef, timeoutRef, debounceRef]);
 * ```
 */
export const clearAllTimerRefs = (
  timerRefs: MutableRefObject<TimerId | null>[]
): void => {
  timerRefs.forEach((ref) => {
    if (ref.current !== null) {
      // Try both clear functions since we don't know the timer type
      clearInterval(ref.current);
      clearTimeout(ref.current);
      ref.current = null;
    }
  });
};

/**
 * Creates a debounced version of a function
 * Returns a cleanup function to cancel pending executions
 * 
 * @example
 * ```tsx
 * const [debouncedFn, cleanup] = createDebouncedFn(
 *   (value: string) => search(value),
 *   300
 * );
 * 
 * // Use in handler
 * onChange={(e) => debouncedFn(e.target.value)}
 * 
 * // Cleanup on unmount
 * useEffect(() => cleanup, []);
 * ```
 */
export const createDebouncedFn = <T extends (...args: Parameters<T>) => void>(
  fn: T,
  delay: number
): [(...args: Parameters<T>) => void, () => void] => {
  let timeoutId: ReturnType<typeof setTimeout> | null = null;

  const debouncedFn = (...args: Parameters<T>): void => {
    if (timeoutId) {
      clearTimeout(timeoutId);
    }
    timeoutId = setTimeout(() => {
      fn(...args);
      timeoutId = null;
    }, delay);
  };

  const cleanup = (): void => {
    if (timeoutId) {
      clearTimeout(timeoutId);
      timeoutId = null;
    }
  };

  return [debouncedFn, cleanup];
};

/**
 * Creates a throttled version of a function
 * 
 * @example
 * ```tsx
 * const throttledScroll = createThrottledFn(handleScroll, 100);
 * window.addEventListener('scroll', throttledScroll);
 * ```
 */
export const createThrottledFn = <T extends (...args: Parameters<T>) => void>(
  fn: T,
  limit: number
): ((...args: Parameters<T>) => void) => {
  let inThrottle = false;

  return (...args: Parameters<T>): void => {
    if (!inThrottle) {
      fn(...args);
      inThrottle = true;
      setTimeout(() => {
        inThrottle = false;
      }, limit);
    }
  };
};

/**
 * Safely sets an interval with automatic cleanup capability
 * Returns a cleanup function
 * 
 * @example
 * ```tsx
 * useEffect(() => {
 *   return setIntervalSafe(() => pollData(), 5000);
 * }, []);
 * ```
 */
export const setIntervalSafe = (
  callback: () => void,
  delay: number
): (() => void) => {
  const intervalId = setInterval(callback, delay);
  return () => clearInterval(intervalId);
};

/**
 * Safely sets a timeout with automatic cleanup capability
 * Returns a cleanup function
 * 
 * @example
 * ```tsx
 * useEffect(() => {
 *   return setTimeoutSafe(() => showNotification(), 3000);
 * }, []);
 * ```
 */
export const setTimeoutSafe = (
  callback: () => void,
  delay: number
): (() => void) => {
  const timeoutId = setTimeout(callback, delay);
  return () => clearTimeout(timeoutId);
};

/**
 * Creates a delayed execution that can be cancelled
 * 
 * @example
 * ```tsx
 * const delayed = createDelayedExecution(() => navigate('/home'), 2000);
 * delayed.start();
 * // Later...
 * delayed.cancel();
 * ```
 */
export const createDelayedExecution = (
  callback: () => void,
  delay: number
): { start: () => void; cancel: () => void; isScheduled: () => boolean } => {
  let timeoutId: ReturnType<typeof setTimeout> | null = null;

  return {
    start: () => {
      if (timeoutId) {
        clearTimeout(timeoutId);
      }
      timeoutId = setTimeout(() => {
        callback();
        timeoutId = null;
      }, delay);
    },
    cancel: () => {
      if (timeoutId) {
        clearTimeout(timeoutId);
        timeoutId = null;
      }
    },
    isScheduled: () => timeoutId !== null,
  };
};
