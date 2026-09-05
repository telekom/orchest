import { SecureStorage } from '@/shared/auth/services/SecureStorage';
import { tokenManager } from '@/shared/auth/services/TokenManager';
import { AuthErrorHandler, AuthErrorType } from '@/shared/auth/utils/authErrorHandler';
import { isTokenExpiredOrExpiringSoon } from '@/shared/auth/utils/jwtUtils';
import { TIMING } from '@/shared/constants';
import { STORAGE_KEYS } from '@/shared/constants/storageConstants';
import { clearIntervalRef } from '@/shared/utils/timerUtils';
import { useStableCallback } from './useStableCallback';
import { useCallback, useEffect, useRef } from 'react';

const TOKEN_RENEWAL_OFFSET_SECONDS = 300;
const TOKEN_REFRESH_INTERVAL_MS = TIMING.POLLING_TIMEOUT; // 60000ms

export interface UseTokenRefreshOptions {
  /** Whether the user is authenticated */
  isAuthenticated: boolean;
  /** Whether auth is still loading */
  isLoading: boolean;
  /** Whether we're in no-login/dev mode */
  isNoLoginMode: boolean;
  /** Custom refresh interval in ms (default: 60000) */
  refreshInterval?: number;
  /** Token renewal offset in seconds (default: 300) */
  renewalOffsetSeconds?: number;
  /** Callback when token refresh fails */
  onRefreshError?: (error: Error) => void;
  /** Callback when token is successfully refreshed */
  onRefreshSuccess?: () => void;
}

export interface UseTokenRefreshReturn {
  /** Manually trigger a token refresh check */
  checkAndRefresh: () => Promise<boolean>;
  /** Whether a refresh is currently in progress */
  isRefreshing: boolean;
}

/**
 * Hook for proactive token refresh management
 * 
 * Automatically checks token expiration and refreshes before it expires.
 * Runs on an interval when authenticated and not in dev mode.
 * 
 * @example
 * ```tsx
 * import { logger } from '@/shared/utils/logger';
 *
 * const { checkAndRefresh } = useTokenRefresh({
 *   isAuthenticated,
 *   isLoading,
 *   isNoLoginMode: isNoLoginMode(),
 *   onRefreshError: (error) => logger.error('Token refresh failed', error),
 * });
 * ```
 */
export function useTokenRefresh(options: UseTokenRefreshOptions): UseTokenRefreshReturn {
  const {
    isAuthenticated,
    isLoading,
    isNoLoginMode,
    refreshInterval = TOKEN_REFRESH_INTERVAL_MS,
    renewalOffsetSeconds = TOKEN_RENEWAL_OFFSET_SECONDS,
    onRefreshError,
    onRefreshSuccess,
  } = options;

  const isRefreshingRef = useRef(false);
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const isMountedRef = useRef(true);

  const stableOnRefreshError = useStableCallback(onRefreshError);
  const stableOnRefreshSuccess = useStableCallback(onRefreshSuccess);

  const checkAndRefresh = useCallback(async (): Promise<boolean> => {
    if (isRefreshingRef.current || !isMountedRef.current) {
      return false;
    }

    try {
      const token = SecureStorage.get<string>(STORAGE_KEYS.ACCESS_TOKEN);

      if (!token) {
        return false;
      }

      // Convert seconds to minutes for the utility function
      const offsetMinutes = renewalOffsetSeconds / 60;

      if (isTokenExpiredOrExpiringSoon(token, offsetMinutes)) {
        isRefreshingRef.current = true;

        await tokenManager.getAccessToken();

        if (isMountedRef.current) {
          stableOnRefreshSuccess?.();
        }

        return true;
      }

      return false;
    } catch (error) {
      if (isMountedRef.current) {
        const errorInstance = error instanceof Error ? error : new Error(String(error));

        AuthErrorHandler.handleError({
          type: AuthErrorType.TOKEN_REFRESH,
          message: 'Proactive token refresh failed',
          originalError: errorInstance,
        });

        stableOnRefreshError?.(errorInstance);
      }

      return false;
    } finally {
      isRefreshingRef.current = false;
    }
  }, [renewalOffsetSeconds, stableOnRefreshError, stableOnRefreshSuccess]);

  // Cleanup interval helper
  const clearRefreshInterval = useCallback(() => {
    clearIntervalRef(intervalRef);
  }, []);

  // Setup refresh interval
  useEffect(() => {
    isMountedRef.current = true;

    // Don't run in no-login mode or when not authenticated
    if (!isAuthenticated || isLoading || isNoLoginMode) {
      clearRefreshInterval();
      return;
    }

    // Setup interval for proactive token refresh
    intervalRef.current = setInterval(() => {
      if (isMountedRef.current) {
        checkAndRefresh();
      }
    }, refreshInterval);

    return () => {
      isMountedRef.current = false;
      clearRefreshInterval();
    };
  }, [isAuthenticated, isLoading, isNoLoginMode, refreshInterval, checkAndRefresh, clearRefreshInterval]);

  return {
    checkAndRefresh,
    get isRefreshing() {
      return isRefreshingRef.current;
    },
  };
}

export default useTokenRefresh;
