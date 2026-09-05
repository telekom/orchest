/**
 * Authentication Adapter Factory
 * Selects appropriate adapter based on environment
 */

import { PublicClientApplication } from '@azure/msal-browser';
import { environment } from '@/shared/constants/environment';
import { IAuthAdapter } from './AuthAdapter';
import { DevAuthAdapter } from './DevAuthAdapter';
import { MsalAuthAdapter } from './MsalAuthAdapter';

export * from './AuthAdapter';
export * from './DevAuthAdapter';
export * from './MsalAuthAdapter';

/**
 * Create the appropriate authentication adapter
 */
export function createAuthAdapter(msalInstance?: PublicClientApplication): IAuthAdapter {
  if (environment.bypassLogin) {
    return new DevAuthAdapter();
  }

  if (!msalInstance) {
    throw new Error('MSAL instance required for production authentication');
  }

  return new MsalAuthAdapter(msalInstance);
}
