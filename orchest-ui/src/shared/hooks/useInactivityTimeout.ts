import { clearTimeoutRef } from '@/shared/utils/timerUtils';
import { useCallback, useEffect, useRef, useState } from 'react';

/**
 * SECURITY: Inactivity Timeout Hook
 *
 * Detects user inactivity and triggers automatic logout after a configurable period.
 * Monitors mouse movement, keyboard input, and touch events.
 *
 * @param onTimeout - Callback to execute when timeout occurs (e.g., logout)
 * @param timeoutMs - Inactivity period in milliseconds (default: 15 minutes)
 * @param warningMs - Time before timeout to show warning (default: 2 minutes before timeout)
 * @param onWarning - Optional callback when warning period starts
 * @param enabled - Whether the timeout is active (default: true)
 *
 * @example
 * ```tsx
 * const { remainingTime, resetTimer, isWarning } = useInactivityTimeout({
 *   onTimeout: () => logout(),
 *   timeoutMs: 900000, // 15 minutes
 *   warningMs: 120000, // 2 minutes warning
 *   onWarning: () => toast.warning('Session will expire soon'),
 *   enabled: isAuthenticated
 * });
 * ```
 */

export interface InactivityTimeoutOptions {
  onTimeout: () => void;
  timeoutMs?: number;
  warningMs?: number;
  onWarning?: () => void;
  enabled?: boolean;
}

export interface InactivityTimeoutState {
  remainingTime: number;
  isWarning: boolean;
  resetTimer: () => void;
  pauseTimer: () => void;
  resumeTimer: () => void;
}

const DEFAULT_TIMEOUT_MS = 1 * 60 * 1000; // 15 minutes
const DEFAULT_WARNING_MS = 1 * 60 * 1000; // 2 minutes
const UPDATE_INTERVAL_MS = 1000; // Update remaining time every second

export function useInactivityTimeout({
  onTimeout,
  timeoutMs = DEFAULT_TIMEOUT_MS,
  warningMs = DEFAULT_WARNING_MS,
  onWarning,
  enabled = true,
}: InactivityTimeoutOptions): InactivityTimeoutState {
  const [remainingTime, setRemainingTime] = useState(timeoutMs);
  const [isWarning, setIsWarning] = useState(false);
  const [isPaused, setIsPaused] = useState(false);

  const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const updateIntervalRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const lastActivityRef = useRef<number>(Date.now());
  const warningTriggeredRef = useRef<boolean>(false);

  const clearTimers = useCallback(() => {
    clearTimeoutRef(timeoutRef);
    if (updateIntervalRef.current) {
      clearInterval(updateIntervalRef.current);
      updateIntervalRef.current = null;
    }
  }, []);

  const resetTimer = useCallback(() => {
    if (!enabled || isPaused) return;

    clearTimers();
    lastActivityRef.current = Date.now();
    warningTriggeredRef.current = false;
    setRemainingTime(timeoutMs);
    setIsWarning(false);

    // Set timeout for inactivity
    timeoutRef.current = setTimeout(() => {
      onTimeout();
    }, timeoutMs);

    // Update remaining time every second
    updateIntervalRef.current = setInterval(() => {
      const elapsed = Date.now() - lastActivityRef.current;
      const remaining = Math.max(0, timeoutMs - elapsed);

      setRemainingTime(remaining);

      // Trigger warning when threshold reached
      if (remaining <= warningMs && !warningTriggeredRef.current) {
        warningTriggeredRef.current = true;
        setIsWarning(true);
        onWarning?.();
      }

      // Clear interval when timeout reached
      if (remaining === 0 && updateIntervalRef.current) {
        clearInterval(updateIntervalRef.current);
        updateIntervalRef.current = null;
      }
    }, UPDATE_INTERVAL_MS);
  }, [enabled, isPaused, timeoutMs, warningMs, onTimeout, onWarning, clearTimers]);

  const pauseTimer = useCallback(() => {
    setIsPaused(true);
    clearTimers();
  }, [clearTimers]);

  const resumeTimer = useCallback(() => {
    setIsPaused(false);
    resetTimer();
  }, [resetTimer]);

  // Activity event handlers
  useEffect(() => {
    if (!enabled) {
      clearTimers();
      return;
    }

    const activityEvents = [
      'mousedown',
      'mousemove',
      'keydown',
      'scroll',
      'touchstart',
      'click',
    ];

    const handleActivity = () => {
      if (!isPaused) {
        resetTimer();
      }
    };

    // Add event listeners
    activityEvents.forEach(event => {
      window.addEventListener(event, handleActivity, { passive: true });
    });

    // Initialize timer
    resetTimer();

    // Cleanup
    return () => {
      activityEvents.forEach(event => {
        window.removeEventListener(event, handleActivity);
      });
      clearTimers();
    };
  }, [enabled, isPaused, resetTimer, clearTimers]);

  // Handle visibility change (pause when tab not visible)
  useEffect(() => {
    if (!enabled) return;

    const handleVisibilityChange = () => {
      if (document.hidden) {
        // Don't pause timer when tab is hidden - security requirement
        // User should be logged out even if they switch tabs
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);

    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, [enabled]);

  return {
    remainingTime,
    isWarning,
    resetTimer,
    pauseTimer,
    resumeTimer,
  };
}
