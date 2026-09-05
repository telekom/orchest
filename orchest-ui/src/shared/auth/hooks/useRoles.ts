import { useMemo } from 'react';
import { useAuth } from '../context/AuthContext';
import { UserRoles } from '../models/roles';
import { AuthorizationService } from '../services/AuthorizationService';

export const useRoles = () => {
  const { hasRole, getEffectiveRoles } = useAuth();

  return useMemo(() => {
    const effectiveRoles = getEffectiveRoles();
    const primaryRole = AuthorizationService.getHighestRole(effectiveRoles);

    const isAdmin = primaryRole === UserRoles.ADMIN;
    const isViewer = primaryRole === UserRoles.VIEWER;
    const isSensitive = primaryRole === UserRoles.SENSITIVE;

    const canView = isAdmin || isViewer;
    const canEdit = isAdmin;
    const canApprove = isAdmin;

    return {
      isAdmin,
      isViewer,
      isSensitive,
      primaryRole,
      canView,
      canEdit,
      canApprove,
      hasRole,
    };
  }, [hasRole, getEffectiveRoles]);
};
