import { activeRoleService } from "@/shared/auth/services/ActiveRoleService";
import { tokenManager } from "@/shared/auth/services/TokenManager";
import { tenantService } from "@/shared/services/TenantService";
import { globalErrorHandler } from "@/shared/error/globalErrorHandler";
import { InternalAxiosRequestConfig } from "axios";
import { v4 as uuidv4 } from "uuid";

export interface RequestConfigWithAuth extends InternalAxiosRequestConfig {
  skipAuthHeader?: boolean;
}

/**
 * Enriches request with auth token and security headers.
 * Validates and sanitizes request before sending to prevent SSRF and injection attacks.
 *
 * @throws {Error} When token acquisition fails - request is blocked to prevent 401 errors
 */
export async function enrichRequest(config: RequestConfigWithAuth): Promise<InternalAxiosRequestConfig> {
  // Add authentication token
  if (!config.skipAuthHeader) {
    try {
      const token = await tokenManager.getAccessToken();
      config.headers.Authorization = `Bearer ${token}`;
    } catch (error) {
      globalErrorHandler.handleError(error, 'auth', {
        context: 'request-interceptor-token-acquisition',
      });

      throw new Error(
        'Authentication required: Failed to acquire access token. Request blocked.'
      );
    }
  }

  // Add active role header (highest priority: ADMIN > VIEWER > SENSITIVE)
  const activeRole = activeRoleService.getActiveRole();
  if (activeRole) {
    config.headers['X-Active-Role'] = activeRole;
  }

  // Add tenant header
  const tenant = tenantService.getTenant();
  if (tenant) {
    config.headers['X-Tenant-Id'] = tenant;
  }

  // Add request tracking headers
  config.headers['X-Request-Id'] = uuidv4();
  config.headers['X-Request-Timestamp'] = new Date().toISOString();

  // Ensure Content-Type is set for requests with data
  if (config.data && !config.headers['Content-Type']) {
    config.headers['Content-Type'] = 'application/json';
  }

  // Prevent MIME type confusion
  config.headers['X-Content-Type-Options'] = 'nosniff';

  // Validate URL to prevent SSRF attacks
  if (config.url) {
    validateRequestURL(config.url);
  }

  // Sanitize request parameters
  if (config.params) {
    config.params = sanitizeRequestParams(config.params);
  }

  return config;
}

/**
 * Validates request URL to prevent SSRF attacks.
 * Blocks localhost, private IPs (RFC 1918), and non-HTTP(S) protocols.
 */
function validateRequestURL(url: string): void {
  // Relative URLs are safe (handled by baseURL)
  if (url.startsWith('/')) {
    return;
  }

  try {
    const parsedUrl = new URL(url);
    const hostname = parsedUrl.hostname.toLowerCase();

    // Only allow HTTP/HTTPS protocols
    if (!['http:', 'https:'].includes(parsedUrl.protocol)) {
      throw new Error(`Protocol ${parsedUrl.protocol} is not allowed`);
    }

    // Block localhost and private IPs
    const dangerousPatterns = [
      /^localhost$/i,
      /^127\./,                                   // IPv4 loopback
      /^0\.0\.0\.0$/,                             // IPv4 any
      /^::1$/,                                    // IPv6 loopback
      /^fe80:/i,                                  // IPv6 link-local
      /^10\./,                                    // RFC 1918: 10.0.0.0/8
      /^172\.(1[6-9]|2[0-9]|3[0-1])\./,          // RFC 1918: 172.16.0.0/12
      /^192\.168\./,                              // RFC 1918: 192.168.0.0/16
    ];

    if (dangerousPatterns.some(pattern => pattern.test(hostname))) {
      throw new Error('Requests to local or private addresses are not allowed');
    }
  } catch (error) {
    // If URL parsing fails (TypeError), it's a relative URL - allow it
    if (error instanceof TypeError) {
      return;
    }
    throw error;
  }
}

/**
 * Sanitizes request parameters by removing null/undefined values
 * and limiting string length to prevent DoS attacks.
 */
function sanitizeRequestParams(params: Record<string, unknown>): Record<string, unknown> {
  const sanitized: Record<string, unknown> = {};
  const MAX_STRING_LENGTH = 10000;

  for (const [key, value] of Object.entries(params)) {
    if (value == null) continue;

    sanitized[key] = typeof value === 'string' && value.length > MAX_STRING_LENGTH
      ? value.substring(0, MAX_STRING_LENGTH)
      : value;
  }

  return sanitized;
}
