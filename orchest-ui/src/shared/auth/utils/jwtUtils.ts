/**
 * JWT Token Utilities
 * Centralized JWT parsing and validation logic
 */

export interface JWTPayload {
  exp?: number;
  iat?: number;
  sub?: string;
  [key: string]: unknown;
}

/**
 * Parse a JWT token and extract its payload
 * @param token - The JWT token string
 * @returns Parsed payload or null if invalid
 */
export function parseJWT(token: string): JWTPayload | null {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;

    const base64Url = parts[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      window
        .atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );

    return JSON.parse(jsonPayload) as JWTPayload;
  } catch {
    return null;
  }
}

/**
 * Check if a token is expired or will expire soon
 * @param token - The JWT token string
 * @param bufferMinutes - Minutes before expiry to consider as "expiring soon" (default: 5)
 * @returns true if token is expired or expiring soon
 */
export function isTokenExpiredOrExpiringSoon(
  token: string,
  bufferMinutes: number = 5
): boolean {
  const decoded = parseJWT(token);
  if (!decoded?.exp) return false;

  const expiryTime = decoded.exp * 1000;
  const bufferTime = bufferMinutes * 60 * 1000;
  const now = Date.now();

  return now >= expiryTime - bufferTime;
}

/**
 * Calculate token expiry time in milliseconds
 * @param token - The JWT token string
 * @param fallbackMinutes - Fallback expiry in minutes if token doesn't contain exp claim (default: 60)
 * @returns Expiry timestamp in milliseconds
 */
export function getTokenExpiryTime(
  token: string,
  fallbackMinutes: number = 60
): number {
  const decoded = parseJWT(token);
  if (decoded?.exp) {
    return decoded.exp * 1000;
  }
  return Date.now() + fallbackMinutes * 60 * 1000;
}

/**
 * Calculate remaining minutes until token expiry
 * @param token - The JWT token string
 * @returns Minutes until expiry, or null if token is invalid
 */
export function getTokenExpiryMinutes(token: string): number | null {
  const decoded = parseJWT(token);
  if (!decoded?.exp) return null;

  const expiryTime = decoded.exp * 1000;
  const now = Date.now();
  const remainingMs = expiryTime - now;

  return Math.max(Math.floor(remainingMs / 60000), 0);
}
