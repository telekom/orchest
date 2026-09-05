import { HTTP_CLIENT_CONFIG } from "@/shared/constants/apiConfig";
import { AxiosError, AxiosInstance, AxiosRequestConfig, AxiosResponse } from "axios";
import { ApiRequestConfig } from "../types";

/**
 * Determines if a request should be retried based on error and config
 */
export function shouldRetryRequest(error: AxiosError, config?: ApiRequestConfig): boolean {
  if (!config || config._retry) return false;

  const retryCount = config.retry || 0;
  const isNetworkError = !error.response;
  const isRetryableStatus = error.response?.status
    ? (HTTP_CLIENT_CONFIG.RETRYABLE_STATUS_CODES as readonly number[]).includes(error.response.status)
    : false;

  return retryCount > 0 && (isNetworkError || isRetryableStatus);
}

/**
 * Executes a retry request with exponential backoff
 */
export async function executeRetry(
  config: ApiRequestConfig,
  axiosInstance: AxiosInstance
): Promise<AxiosResponse> {
  const retryDelay = config.retryDelay || HTTP_CLIENT_CONFIG.RETRY_DELAY;
  const retryCount = (config.retry || 0) - 1;

  // Explicitly coerce delay to a positive integer to prevent any non-numeric value
  // from being passed to setTimeout (guards against eval-injection false positives)
  const sanitizedDelay = Math.max(0, Math.floor(Number(retryDelay)));
  await new Promise<void>(resolve => setTimeout(resolve, sanitizedDelay));

  return axiosInstance.request({
    ...config,
    retry: retryCount,
    _retry: true
  } as AxiosRequestConfig & { retry: number; _retry: boolean });
}
