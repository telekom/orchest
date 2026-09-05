import { TIMING } from '@/shared/constants/ui.config';
import { useCallback, useRef } from 'react';
import { toast } from '@/design-system/components/ui/sonner';

/**
 * SECURITY: Rate Limiter Hook
 *
 * Prevents abuse by limiting the number of times an action can be performed
 * within a given time window. This is a client-side protection layer that
 * complements server-side rate limiting.
 *
 * Use cases:
 * - Form submissions
 * - API calls
 * - Search queries
 * - Button clicks
 *
 * @example
 * ```tsx
 * const { isRateLimited, executeWithRateLimit } = useRateLimiter({
 *   maxAttempts: 5,
 *   windowMs: 60000, // 1 minute
 *   onRateLimitExceeded: () => toast.error('Too many attempts. Please wait.')
 * });
 *
 * const handleSubmit = () => {
 *   executeWithRateLimit(() => {
 *     submitForm();
 *   });
 * };
 * ```
 */

export interface RateLimiterOptions {
  /** Maximum number of attempts allowed within the time window */
  maxAttempts: number;
  /** Time window in milliseconds */
  windowMs: number;
  /** Callback when rate limit is exceeded */
  onRateLimitExceeded?: () => void;
  /** Custom error message */
  errorMessage?: string;
  /** Identifier for this rate limiter (useful for multiple limiters) */
  id?: string;
}

export interface RateLimiterState {
  /** Whether the rate limit is currently exceeded */
  isRateLimited: boolean;
  /** Number of attempts remaining in current window */
  attemptsRemaining: number;
  /** Time until rate limit resets (ms) */
  resetTimeMs: number;
  /** Execute function with rate limiting */
  executeWithRateLimit: <T>(fn: () => T) => T | null;
  /** Reset the rate limiter */
  reset: () => void;
  /** Check if an action would be rate limited without executing */
  wouldBeRateLimited: () => boolean;
}

interface AttemptRecord {
  timestamp: number;
}

const DEFAULT_ERROR_MESSAGE = 'Too many attempts. Please wait and try again.';

export function useRateLimiter({
  maxAttempts,
  windowMs,
  onRateLimitExceeded,
  errorMessage = DEFAULT_ERROR_MESSAGE,
  id,
}: RateLimiterOptions): RateLimiterState {
  const attemptsRef = useRef<AttemptRecord[]>([]);
  const toastShownRef = useRef<boolean>(false);

  /**
   * Remove expired attempts from the history
   */
  const cleanupExpiredAttempts = useCallback(() => {
    const now = Date.now();
    attemptsRef.current = attemptsRef.current.filter(
      attempt => now - attempt.timestamp < windowMs
    );
  }, [windowMs]);

  /**
   * Get number of attempts in current window
   */
  const getAttemptCount = useCallback(() => {
    cleanupExpiredAttempts();
    return attemptsRef.current.length;
  }, [cleanupExpiredAttempts]);

  /**
   * Check if rate limit would be exceeded
   */
  const wouldBeRateLimited = useCallback(() => {
    return getAttemptCount() >= maxAttempts;
  }, [getAttemptCount, maxAttempts]);

  /**
   * Get time until oldest attempt expires
   */
  const getResetTimeMs = useCallback(() => {
    cleanupExpiredAttempts();
    if (attemptsRef.current.length === 0) return 0;

    const oldestAttempt = attemptsRef.current[0];
    const resetTime = oldestAttempt.timestamp + windowMs;
    return Math.max(0, resetTime - Date.now());
  }, [cleanupExpiredAttempts, windowMs]);

  /**
   * Execute a function with rate limiting
   */
  const executeWithRateLimit = useCallback(
    <T,>(fn: () => T): T | null => {
      cleanupExpiredAttempts();

      if (wouldBeRateLimited()) {
        // Show toast only once per rate limit period
        if (!toastShownRef.current) {
          const resetSeconds = Math.ceil(getResetTimeMs() / 1000);
          const message = `${errorMessage} Try again in ${resetSeconds}s.`;

          toast.error(message, {
            duration: TIMING.TOAST_ERROR_DURATION,
            id: id ? `rate-limit-${id}` : 'rate-limit',
          });

          toastShownRef.current = true;

          // Reset toast flag after rate limit expires
          setTimeout(() => {
            toastShownRef.current = false;
          }, getResetTimeMs());
        }

        onRateLimitExceeded?.();
        return null;
      }

      // Record this attempt
      attemptsRef.current.push({ timestamp: Date.now() });

      // Execute the function
      return fn();
    },
    [
      cleanupExpiredAttempts,
      wouldBeRateLimited,
      getResetTimeMs,
      errorMessage,
      id,
      onRateLimitExceeded,
    ]
  );

  /**
   * Reset the rate limiter
   */
  const reset = useCallback(() => {
    attemptsRef.current = [];
    toastShownRef.current = false;
  }, []);

  return {
    isRateLimited: wouldBeRateLimited(),
    attemptsRemaining: Math.max(0, maxAttempts - getAttemptCount()),
    resetTimeMs: getResetTimeMs(),
    executeWithRateLimit,
    reset,
    wouldBeRateLimited,
  };
}

/**
 * Pre-configured rate limiters for common use cases
 */

export const useFormSubmitRateLimiter = (id?: string) => {
  return useRateLimiter({
    maxAttempts: 5,
    windowMs: 60000, // 1 minute
    errorMessage: 'Too many form submissions',
    id: id ? `form-${id}` : 'form',
  });
};

export const useSearchRateLimiter = (id?: string) => {
  return useRateLimiter({
    maxAttempts: 30,
    windowMs: 60000, // 1 minute
    errorMessage: 'Too many search requests',
    id: id ? `search-${id}` : 'search',
  });
};

export const useApiCallRateLimiter = (id?: string) => {
  return useRateLimiter({
    maxAttempts: 10,
    windowMs: 10000, // 10 seconds
    errorMessage: 'Too many API requests',
    id: id ? `api-${id}` : 'api',
  });
};

export const useButtonClickRateLimiter = (id?: string) => {
  return useRateLimiter({
    maxAttempts: 3,
    windowMs: 3000, // 3 seconds
    errorMessage: 'Please wait before clicking again',
    id: id ? `button-${id}` : 'button',
  });
};
