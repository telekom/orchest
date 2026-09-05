/**
 * Validates that a redirect URL is safe for internal navigation
 * @param url - The URL to validate
 * @returns true if the URL is safe, false otherwise
 */
export const isValidRedirectUrl = (url: string | null | undefined): boolean => {
  if (!url || typeof url !== 'string') {
    return false;
  }

  // Trim whitespace
  const trimmed = url.trim();

  // Must start with / (relative path)
  if (!trimmed.startsWith('/')) {
    return false;
  }

  // Reject URLs with protocol schemes (javascript:, data:, http:, etc.)
  if (trimmed.includes(':')) {
    return false;
  }

  // Reject URLs with double slashes (protocol-relative URLs like //evil.com)
  if (trimmed.startsWith('//')) {
    return false;
  }

  // Reject URLs with backslashes (Windows path separators, potential bypass)
  if (trimmed.includes('\\')) {
    return false;
  }

  // Max length check (prevent DoS via extremely long URLs)
  if (trimmed.length > 2000) {
    return false;
  }

  return true;
};

/**
 * Sanitizes a redirect URL by validating and returning a safe default if invalid
 * @param url - The URL to sanitize
 * @param defaultUrl - The default URL to use if validation fails (default: '/')
 * @returns A safe redirect URL
 */
export const sanitizeRedirectUrl = (
  url: string | null | undefined,
  defaultUrl: string = '/'
): string => {
  return isValidRedirectUrl(url) ? url!.trim() : defaultUrl;
};
