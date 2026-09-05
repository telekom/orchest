import { useAuth, UserRoles } from "@/shared/auth";

/**
 * Hook to check if user has required role for an action
 * Returns null if user doesn't have permission (for early return pattern)
 */
export const useRoleBasedAction = (requiredRole: UserRoles = UserRoles.ADMIN) => {
  const { hasRole } = useAuth();
  const canPerformAction = hasRole(requiredRole);

  return { canPerformAction };
};
