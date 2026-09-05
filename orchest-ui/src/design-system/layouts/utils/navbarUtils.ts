import { UserRoles } from '@/shared/auth';

/**
 * Checks if a user has permission to access a navigation item
 * @param requiredRole - The role required to access the item (undefined means public)
 * @param hasRole - Function to check if user has a specific role
 * @returns True if user has permission
 */
export const hasPermission = (
  requiredRole: string | undefined,
  hasRole: (role: string, strict?: boolean) => boolean
): boolean => {
  if (!requiredRole) return true;
  if (requiredRole === UserRoles.ADMIN) return hasRole(UserRoles.ADMIN, true);
  return hasRole(requiredRole);
};

interface DebounceHandler {
  handler: () => void;
  cleanup: () => void;
}

/**
 * Creates a debounced handler with cleanup function
 * @param callback - Function to debounce
 * @param delay - Delay in milliseconds
 * @returns Object with handler and cleanup functions
 */
export const createDebounceHandler = (
  callback: () => void,
  delay: number
): DebounceHandler => {
  let timer: ReturnType<typeof setTimeout> | null = null;

  return {
    handler: () => {
      if (timer !== null) clearTimeout(timer);
      timer = setTimeout(() => {
        callback();
        timer = null;
      }, delay);
    },
    cleanup: () => {
      if (timer !== null) {
        clearTimeout(timer);
        timer = null;
      }
    }
  };
};
