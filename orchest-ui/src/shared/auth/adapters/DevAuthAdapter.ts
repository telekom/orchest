/**
 * Development Authentication Adapter
 * Mock authentication for local development (no MSAL required)
 *
 * This adapter runs when VITE_BYPASS_LOGIN is 'true' (see environment.bypassLogin).
 * MSAL is not used for sign-in in that case.
 */

import { STORAGE_KEYS } from '@/shared/constants/storageConstants';
import { AccountInfo } from '@azure/msal-browser';
import { AuthorizationService } from '../services/AuthorizationService';
import { SecureStorage } from '../services/SecureStorage';
import { generateDevToken } from '../utils/devTokenGenerator';
import { createDevUser } from '../utils/tokenClaimsParser';
import { UserDetails } from '../utils/userAuthUtils';
import { AuthenticationResult, IAuthAdapter } from './AuthAdapter';

export class DevAuthAdapter implements IAuthAdapter {
  private devUser: UserDetails | null = null;

  async initialize(): Promise<void> {
    // Load stored mock roles or use default
    const storedRoles = AuthorizationService.getMockRoles();
    const roles = storedRoles.length > 0 ? storedRoles : ['ORCHEST_ADMIN'];

    if (storedRoles.length === 0) {
      AuthorizationService.setMockRoles(roles);
    }

    this.devUser = createDevUser(roles);
    SecureStorage.set(STORAGE_KEYS.USER_DETAILS, this.devUser);
  }

  async login(): Promise<boolean> {
    await this.initialize();
    return true;
  }

  async logout(): Promise<void> {
    this.devUser = null;
    SecureStorage.clearAll(true);
    AuthorizationService.clearMockRoles();
  }

  async getAccessToken(): Promise<string | null> {
    return generateDevToken();
  }

  isAuthenticated(): boolean {
    return !!this.devUser || AuthorizationService.getMockRoles().length > 0;
  }

  getActiveAccount(): AccountInfo | null {
    return null; // Dev mode doesn't use MSAL accounts
  }

  async getUserDetails(): Promise<UserDetails | null> {
    if (!this.devUser) {
      await this.initialize();
    }
    return this.devUser;
  }

  async refresh(): Promise<AuthenticationResult> {
    const mockRoles = AuthorizationService.getMockRoles();
    const roles = mockRoles.length > 0 ? mockRoles : ['ORCHEST_ADMIN'];

    this.devUser = createDevUser(roles);
    SecureStorage.set(STORAGE_KEYS.USER_DETAILS, this.devUser);

    return {
      success: roles.length > 0,
      user: this.devUser,
    };
  }

  /**
   * Dev-specific: Update mock roles at runtime
   */
  updateMockRoles(roles: string[]): void {
    AuthorizationService.setMockRoles(roles);
    this.devUser = createDevUser(roles);
    SecureStorage.set(STORAGE_KEYS.USER_DETAILS, this.devUser);

    // Dispatch event for reactive updates
    window.dispatchEvent(
      new CustomEvent('roleChange', { detail: { roles } })
    );
  }
}
