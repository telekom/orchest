/**
 * Authentication Adapter Interface
 * Defines contract for different authentication strategies (MSAL vs Dev)
 *
 * This pattern cleanly separates dev/production auth logic without pollution
 */

import { AccountInfo } from '@azure/msal-browser';
import { UserDetails } from '../utils/userAuthUtils';

export interface AuthenticationResult {
  success: boolean;
  user: UserDetails | null;
  account?: AccountInfo | null;
}

export interface IAuthAdapter {
  /**
   * Initialize authentication
   */
  initialize(): Promise<void>;

  /**
   * Perform login
   */
  login(): Promise<boolean>;

  /**
   * Perform logout — clears all tokens, caches, and session data.
   * For MSAL: also triggers Azure AD logout redirect.
   */
  logout(): Promise<void>;

  /**
   * Get access token
   */
  getAccessToken(): Promise<string | null>;

  /**
   * Check if user is authenticated
   */
  isAuthenticated(): boolean;

  /**
   * Get current user details
   */
  getUserDetails(): Promise<UserDetails | null>;

  /**
   * Get active account (MSAL-specific, optional for dev)
   */
  getActiveAccount(): AccountInfo | null;

  /**
   * Refresh authentication state
   */
  refresh(): Promise<AuthenticationResult>;
}
