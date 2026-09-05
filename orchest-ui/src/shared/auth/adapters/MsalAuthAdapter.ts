/**
 * MSAL Authentication Adapter
 * Production authentication using Azure AD / MSAL
 */

import { STORAGE_KEYS } from '@/shared/constants/storageConstants';
import { AccountInfo, PublicClientApplication } from '@azure/msal-browser';
import { activeRoleService } from '../services/ActiveRoleService';
import { AuthorizationService } from '../services/AuthorizationService';
import { SecureStorage } from '../services/SecureStorage';
import { tokenManager } from '../services/TokenManager';
import { isTokenExpiredOrExpiringSoon } from '../utils/jwtUtils';
import { resolveActiveAccount } from '../utils/resolveActiveAccount';
import { parseTokenClaims } from '../utils/tokenClaimsParser';
import { UserDetails } from '../utils/userAuthUtils';
import { AuthenticationResult, IAuthAdapter } from './AuthAdapter';

export class MsalAuthAdapter implements IAuthAdapter {
  constructor(private msalInstance: PublicClientApplication) {
    tokenManager.setMsalInstance(msalInstance);
  }

  async initialize(): Promise<void> {
    await this.msalInstance.initialize();
    this.restoreActiveAccount();
  }

  private restoreActiveAccount(): AccountInfo | null {
    return resolveActiveAccount(
      () => this.msalInstance.getActiveAccount(),
      () => this.msalInstance.getAllAccounts(),
      (account) => this.msalInstance.setActiveAccount(account)
    );
  }

  async login(): Promise<boolean> {
    return await tokenManager.login();
  }

  async logout(): Promise<void> {
    await tokenManager.logout();
  }

  async getAccessToken(): Promise<string | null> {
    try {
      return await tokenManager.getAccessToken();
    } catch {
      return null;
    }
  }

  /**
   * Check if user is authenticated
   * Verifies both MSAL account existence AND token validity
   * @returns {boolean} True if user has valid session with non-expired token
   */
  isAuthenticated(): boolean {
    // Check if MSAL account exists
    const account = this.restoreActiveAccount();
    if (!account) {
      return false;
    }

    // Check if we have a valid, non-expired token
    const token = SecureStorage.get<string>(STORAGE_KEYS.ID_TOKEN);
    if (!token) {
      return false;
    }

    // Verify token is not expired (with 0 minute buffer for strict check)
    return !isTokenExpiredOrExpiringSoon(token, 0);
  }

  getActiveAccount(): AccountInfo | null {
    return this.restoreActiveAccount();
  }

  async getUserDetails(): Promise<UserDetails | null> {
    const account = this.getActiveAccount();
    if (!account) return null;

    await this.getAccessToken(); // Ensure token is fresh

    const claims = account.idTokenClaims ?? {};
    return parseTokenClaims(claims);
  }

  async refresh(): Promise<AuthenticationResult> {
    try {
      const account = this.restoreActiveAccount();
      if (!account) {
        return { success: false, user: null };
      }

      tokenManager.setActiveAccount(account);
      await tokenManager.getAccessToken();

      const user = await this.getUserDetails();

      if (!user) {
        return { success: false, user: null };
      }

      const effectiveRoles = AuthorizationService.getEffectiveRoles(user.roles || []);
      activeRoleService.setRoles(effectiveRoles);

      return {
        success: effectiveRoles.length > 0,
        user,
        account,
      };
    } catch {
      return { success: false, user: null };
    }
  }
}
