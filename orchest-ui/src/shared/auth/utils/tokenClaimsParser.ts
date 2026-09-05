import { AuthorizationService } from '../services/AuthorizationService';
import { UserAuthUtils, UserDetails } from './userAuthUtils';

export interface TokenClaims {
  name?: string;
  preferred_username?: string;
  email?: string;
  given_name?: string;
  roles?: string[] | string;
  groups?: string[] | string;
}

export const parseTokenClaims = (claims: TokenClaims): UserDetails => {
  const name = claims?.name || claims?.preferred_username || claims?.email || '';
  const formatted = UserAuthUtils.formatFullName(name);
  const rawRoles = claims?.roles ?? claims?.groups;
  const roles = AuthorizationService.getEffectiveRoles(rawRoles);

  return {
    name: formatted,
    firstName: UserAuthUtils.getFirstName(formatted),
    givenName: claims?.given_name ?? undefined,
    email: claims?.email || claims?.preferred_username || '',
    loginHint: claims?.preferred_username ?? undefined,
    roles,
  };
};

export const createDevUser = (roles?: string[]): UserDetails => ({
  name: 'Dev User',
  firstName: 'Dev',
  givenName: 'Dev',
  email: 'dev@example.com',
  roles: AuthorizationService.getEffectiveRoles(roles || ['ORCHEST_ADMIN']),
});
