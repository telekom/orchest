import { globalErrorHandler } from '@/shared/error/globalErrorHandler';
import { isNoLoginMode } from '@/shared/utils/environmentUtils';
import { useEffect } from 'react';
import { MOCK_ROLES_STORAGE_KEY } from '../models/roles';
import { AuthorizationService } from '../services/AuthorizationService';
import { parseIdTokenRoles } from '../utils/idTokenRoles';

interface UseMockRoleListenerProps {
  setMockRoles: (roles: string[]) => void;
  setRoleVersion: () => void;
  setRolesLoaded: (loaded: boolean) => void;
}

const resolveDevEffectiveRoles = (mockRoles: string[]): string[] => {
  const tokenRoles = isNoLoginMode() ? [] : parseIdTokenRoles();
  return AuthorizationService.getEffectiveRoles(tokenRoles, mockRoles);
};

export const useMockRoleListener = ({
  setMockRoles,
  setRoleVersion,
  setRolesLoaded,
}: UseMockRoleListenerProps) => {
  useEffect(() => {
    // Only listen for mock role changes in dev/local/beta environments
    if (!isNoLoginMode() && !import.meta.env.DEV) return;

    const updateRoles = (incomingRoles: string[]) => {
      const effectiveRoles = resolveDevEffectiveRoles(incomingRoles);
      setMockRoles(effectiveRoles);
      setRoleVersion();
      setRolesLoaded(effectiveRoles.length > 0);
    };

    const handleStorageChange = (event: StorageEvent) => {
      if (event.key !== MOCK_ROLES_STORAGE_KEY) return;

      try {
        const mockRoles = event.newValue ? JSON.parse(event.newValue) : [];
        updateRoles(mockRoles);
      } catch (error) {
        globalErrorHandler.handleError(
          error instanceof Error ? error : new Error(String(error)),
          'runtime',
          { context: 'mock-role-listener-parse', severity: 'low' }
        );
      }
    };

    const handleRoleChangeEvent = (event: Event) => {
      const customEvent = event as CustomEvent;
      const mockRoles = customEvent.detail?.roles || AuthorizationService.getMockRoles();
      updateRoles(mockRoles);
    };

    window.addEventListener('storage', handleStorageChange);
    window.addEventListener('roleChange', handleRoleChangeEvent as EventListener);
    window.addEventListener('rolesUpdated', handleRoleChangeEvent as EventListener);

    return () => {
      window.removeEventListener('storage', handleStorageChange);
      window.removeEventListener('roleChange', handleRoleChangeEvent as EventListener);
      window.removeEventListener('rolesUpdated', handleRoleChangeEvent as EventListener);
    };
  }, [setMockRoles, setRoleVersion, setRolesLoaded]);
};
