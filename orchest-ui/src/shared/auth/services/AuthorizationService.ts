import { toError } from '@/shared/hooks/useErrorHandling';
import { isNoLoginMode } from '@/shared/utils/environmentUtils';
import { StorageUtils } from '@/shared/utils/storageUtils';
import { MOCK_ROLES_STORAGE_KEY, ROLE_PRIORITY_ORDER, roleHierarchy, UserRoles } from '../models/roles';
import { AuthErrorHandler, AuthErrorType } from '../utils/authErrorHandler';

const filterValidRoles = (roles: string[]): string[] => (roles || []).filter(r => r != null);

const dedupeRolesIgnoreCase = (roles: string[]): string[] => {
  const seen = new Set<string>();
  return roles.filter((role) => {
    const key = role.toUpperCase();
    if (seen.has(key)) return false;
    seen.add(key);
    return true;
  });
};

const compareRolesIgnoreCase = (role1?: string, role2?: string): boolean => {
  return role1?.toUpperCase?.() === role2?.toUpperCase?.();
};

const isOverrideRole = (role: string, requiredRole: string): boolean => {
  if (!role) return false;
  const overridePrefix = `OVERRIDE_${requiredRole}`;
  return compareRolesIgnoreCase(role, overridePrefix);
};

const handleAuthError = (type: AuthErrorType, message: string, error: unknown): void => {
  AuthErrorHandler.handleError({
    type,
    message,
    originalError: toError(error),
  });
};

export class AuthorizationService {
  /** Coerce token claims (string or array) into a deduplicated role list */
  public static normalizeRoleList(roles: unknown): string[] {
    if (roles == null) return [];

    const raw = Array.isArray(roles) ? roles : [roles];
    const normalized = raw
      .filter((role) => role != null && role !== '')
      .map((role) => String(role).trim())
      .filter(Boolean);

    return dedupeRolesIgnoreCase(normalized);
  }

  public static hasRole(userRoles: string[], requiredRole: UserRoles, exactMatch: boolean = false): boolean {
    if (!userRoles || userRoles.length === 0) return false;

    const validRoles = filterValidRoles(userRoles);

    try {
      const isExplicitlyOverridden = validRoles.some(r => isOverrideRole(r, requiredRole));
      if (isExplicitlyOverridden) return false;

      const cleanRoles = validRoles.filter(r => typeof r === 'string' && !r.startsWith('OVERRIDE_'));
      if (cleanRoles.length === 0) return false;

      const userRolesUpper = cleanRoles.map(r => r.toUpperCase());
      const requiredRoleUpper = String(requiredRole).toUpperCase();

      if (!exactMatch && userRolesUpper.includes(String(UserRoles.ADMIN).toUpperCase())) return true;
      if (userRolesUpper.includes(requiredRoleUpper)) return true;

      if (!exactMatch) {
        for (const role of cleanRoles) {
          const roleEnum = Object.values(UserRoles).find(er => compareRolesIgnoreCase(er, role));
          if (roleEnum && roleHierarchy[roleEnum]?.some(h => compareRolesIgnoreCase(h, requiredRoleUpper))) {
            return true;
          }
        }
      }
    } catch (error) {
      handleAuthError(AuthErrorType.PERMISSION_ERROR, 'Error during permission check', error);
      return false;
    }
    return false;
  }

  public static hasAnyRole(userRoles: string[], required: UserRoles[]): boolean {
    return required.some(r => this.hasRole(userRoles, r));
  }

  public static hasAllRoles(userRoles: string[], required: UserRoles[]): boolean {
    return required.every(r => this.hasRole(userRoles, r));
  }

  public static isAdmin(userRoles: string[]): boolean {
    return this.hasRole(userRoles, UserRoles.ADMIN);
  }

  public static isViewer(userRoles: string[]): boolean {
    return this.hasRole(userRoles, UserRoles.VIEWER);
  }

  public static isSensitive(userRoles: string[]): boolean {
    return this.hasRole(userRoles, UserRoles.SENSITIVE);
  }

  public static isValidRole(role: string): boolean {
    if (!role) return false;
    return Object.values(UserRoles).some(er => compareRolesIgnoreCase(er, role));
  }

  public static getMockRoles(): string[] {
    return StorageUtils.getItem<string[]>(MOCK_ROLES_STORAGE_KEY) || [];
  }

  public static setMockRoles(roles: string[]): void {
    const validRoles = (roles || []).filter(Boolean);
    StorageUtils.setItem(MOCK_ROLES_STORAGE_KEY, validRoles);
  }

  public static clearMockRoles(): void {
    StorageUtils.removeItem(MOCK_ROLES_STORAGE_KEY);
  }

  public static getHighestRole(userRoles: string[]): UserRoles | null {
    const sorted = this.getEffectiveRoles(userRoles);
    if (sorted.length === 0) return null;

    for (const priorityRole of ROLE_PRIORITY_ORDER) {
      if (sorted.some((role) => compareRolesIgnoreCase(role, priorityRole))) {
        return priorityRole;
      }
    }

    return null;
  }

  /** Canonical role string for X-Active-Role (Admin → Read Sensitive → Read Non-Sensitive) */
  public static getPrimaryRole(userRoles: string[]): string | null {
    return this.getHighestRole(userRoles);
  }

  public static getEffectiveRoles(actualRoles: unknown, mockRoles: unknown = []): string[] {
    try {
      const safeActual = this.normalizeRoleList(actualRoles);
      const safeMock = this.normalizeRoleList(mockRoles);

      let roles: string[];
      if (isNoLoginMode()) {
        const allMock = safeMock.length > 0 ? [...safeMock] : this.getMockRoles();
        roles = [...allMock];
      } else {
        roles = [...safeActual];
      }

      return this.sortByPriority(roles);
    } catch (error) {
      handleAuthError(AuthErrorType.ROLE_ERROR, 'Error determining effective roles', error);
      return [];
    }
  }

  private static sortByPriority(roles: string[]): string[] {
    return [...roles].sort((a, b) => {
      const aIndex = ROLE_PRIORITY_ORDER.findIndex(p => compareRolesIgnoreCase(p, a));
      const bIndex = ROLE_PRIORITY_ORDER.findIndex(p => compareRolesIgnoreCase(p, b));
      const aPriority = aIndex === -1 ? ROLE_PRIORITY_ORDER.length : aIndex;
      const bPriority = bIndex === -1 ? ROLE_PRIORITY_ORDER.length : bIndex;
      return aPriority - bPriority;
    });
  }

  public static hasExactRole(roles: string[], role: UserRoles): boolean {
    try {
      const safe = filterValidRoles(roles);
      return safe.some(r => compareRolesIgnoreCase(r, role?.toString?.()));
    } catch (error) {
      handleAuthError(AuthErrorType.ROLE_ERROR, 'Error during exact role check', error);
      return false;
    }
  }

  public static isRouteAccessible(
    currentPath: string,
    userRoles: string[],
    pathPermissions: Record<string, UserRoles[]>,
    accessFreePaths: string[] = []
  ): boolean {
    if (accessFreePaths.includes(currentPath)) return true;
    if (currentPath === '/callback' || currentPath.startsWith('/auth/')) return true;
    if (currentPath === '/') return true;

    const validUserRoles = userRoles.filter(role => this.isValidRole(role));

    if (validUserRoles.length === 0 && !accessFreePaths.includes(currentPath)) {
      return false;
    }

    if (pathPermissions[currentPath]) {
      return pathPermissions[currentPath].some(role => this.hasRole(validUserRoles, role));
    }

    for (const [path, roles] of Object.entries(pathPermissions)) {
      if (path.endsWith('/') && currentPath.startsWith(path)) {
        return roles.some(role => this.hasRole(validUserRoles, role));
      }
    }

    return false;
  }
}
