/**
 * Authentication Redirect Utilities
 * Centralized redirect handling for auth flows
 */

import { STORAGE_KEYS } from '@/shared/constants/storageConstants';
import { sanitizeRedirectUrl } from '@/shared/utils/urlValidation';

/**
 * Gets the redirect URL after authentication, defaults to home if none stored or invalid
 * Automatically clears the stored URL after retrieval
 */
export function getRedirectUrl(defaultUrl = '/'): string {
  const storedUrl = localStorage.getItem(STORAGE_KEYS.REDIRECT_AFTER_LOGIN);

  // Clear the stored URL
  if (storedUrl) {
    localStorage.removeItem(STORAGE_KEYS.REDIRECT_AFTER_LOGIN);
  }

  // Validate and sanitize the URL, default to home if invalid
  return sanitizeRedirectUrl(storedUrl, defaultUrl);
}

/**
 * Stores a URL to redirect to after successful authentication
 */
export function storeRedirectUrl(url: string): void {
  localStorage.setItem(STORAGE_KEYS.REDIRECT_AFTER_LOGIN, url);
}

/**
 * Clears any stored redirect URL
 */
export function clearRedirectUrl(): void {
  localStorage.removeItem(STORAGE_KEYS.REDIRECT_AFTER_LOGIN);
}
