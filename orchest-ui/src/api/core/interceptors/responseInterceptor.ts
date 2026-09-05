import { tokenManager } from "@/shared/auth/services/TokenManager";
import { HTTP_STATUS } from "@/shared/constants";
import { environment } from "@/shared/constants/environment";
import { globalErrorHandler } from "@/shared/error/globalErrorHandler";
import { AxiosError, AxiosInstance } from "axios";
import { ApiRequestConfig } from "../types";

interface RetryConfig extends ApiRequestConfig {
  _isRetryAfter401?: boolean;
}

/**
 * Handles 401 errors by refreshing token and retrying request
 * If refresh fails or retry still returns 401, forces logout and redirect
 */
export async function handle401Error(
  error: AxiosError,
  axiosInstance: AxiosInstance
): Promise<unknown> {
  const originalRequest = error.config as RetryConfig;

  // First 401 - attempt token refresh and retry
  if (error.response?.status === HTTP_STATUS.UNAUTHORIZED && !originalRequest._isRetryAfter401) {
    try {
      const newToken = await tokenManager.getAccessToken();

      if (newToken) {
        originalRequest._isRetryAfter401 = true;
        originalRequest.headers = originalRequest.headers || {};
        originalRequest.headers.Authorization = `Bearer ${newToken}`;
        return axiosInstance.request(originalRequest);
      }
    } catch (refreshError) {
      globalErrorHandler.handleError(
        refreshError instanceof Error ? refreshError : new Error(String(refreshError)),
        'auth',
        { context: '401-handler-refresh-error' }
      );
    }

    // Token refresh failed - force logout
    forceLogoutAndRedirect();
    throw new Error('Authentication required');
  }

  // Second 401 after retry - session is truly expired
  if (error.response?.status === HTTP_STATUS.UNAUTHORIZED && originalRequest._isRetryAfter401) {
    forceLogoutAndRedirect();
    throw new Error('Session expired - authentication required');
  }

  throw error;
}

/**
 * Forces logout and redirects to login page
 * Ensures complete session termination
 */
function forceLogoutAndRedirect(): void {
  // Clear tokens and MSAL cache
  tokenManager.logout();
  
  // In bypassLogin mode, MSAL won't redirect, so we do it manually
  // In production, MSAL.logoutRedirect() handles the navigation
  if (typeof window !== 'undefined' && environment.bypassLogin) {
    window.location.replace('/login');
  }
}

/**
 * Enriches error with request context before handling
 */
export function enrichErrorContext(error: AxiosError, config?: ApiRequestConfig) {
  return globalErrorHandler.handleApiError(error, {
    url: config?.url,
    method: config?.method,
    requestId: config?.headers?.['X-Request-Id']
  });
}
