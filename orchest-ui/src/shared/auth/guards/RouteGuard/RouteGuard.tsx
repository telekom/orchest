import { toast } from '@/design-system/components/ui/sonner';
import { SpinnerLoader } from '@/shared/components/Loader/Loader';
import { isNoLoginMode } from '@/shared/utils/environmentUtils';
import React, { ReactNode, useEffect, useMemo } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { UserRoles } from '../../models/roles';
import { storeRedirectUrl } from '../../utils/redirectUtils';
import styles from './RouteGuard.module.css';

interface RouteGuardProps {
  children: ReactNode;
}

// When checking the current pathname we want a lightweight startsWith
// comparison so that "/login/" or "/login?foo" are treated as free.
const FREE_PATHS = ['/dev', '/no-roles', '/login', '/callback', '/feel-playground'];

const isFreePath = (pathname: string) =>
  FREE_PATHS.some(free => pathname.startsWith(free));

export const RouteGuard: React.FC<RouteGuardProps> = ({ children }) => {
  const { isRouteAccessible, getEffectiveRoles, isLoading, user } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const currentPath = location.pathname;

  // Filter to valid roles only (matching UserRoles enum)
  const validRoles = useMemo(() => {
    const roles = getEffectiveRoles();
    const roleValues = Object.values(UserRoles).map(r => r.toUpperCase());
    return roles.filter(role => roleValues.includes(role.toUpperCase()));
  }, [getEffectiveRoles]);

  const hasAccess = useMemo(
    () => user && validRoles.length > 0 && isRouteAccessible(currentPath),
    [user, validRoles.length, isRouteAccessible, currentPath]
  );

  useEffect(() => {
    // Skip guards when the path is explicitly unlocked or while we're
    // still fetching authentication state.  Using `isFreePath` guards
    // against trailing-slash/query-string variants that were triggering a
    // needless redirect loop.
    if (isFreePath(currentPath) || isLoading) return;

    // Guard 1: Require authentication
    if (!user) {
      const fullPath = location.pathname + location.search + location.hash;
      storeRedirectUrl(fullPath);
      navigate('/login', { replace: true });
      return;
    }

    // Guard 2: Require valid roles
    if (validRoles.length === 0) {
      const redirectPath = isNoLoginMode() ? '/dev' : '/no-roles';
      const message = isNoLoginMode()
        ? 'No roles set. Please configure roles in Dev Tools.'
        : 'No role permissions available. Access denied.';

      toast.info(message);
      navigate(redirectPath, { replace: true });
      return;
    }

    // Guard 3: Require route-specific permissions
    if (!isRouteAccessible(currentPath)) {
      toast.error("Access denied. You don't have permission to view this page");
      navigate('/processes', { replace: true });
    }
  }, [
    currentPath,
    location.search,
    location.hash,
    validRoles.length,
    isRouteAccessible,
    navigate,
    isLoading,
    user,
  ]);

  // Allow free paths immediately
  if (FREE_PATHS.includes(currentPath)) {
    return <>{children}</>;
  }

  // Show loading state
  if (isLoading) {
    return (
      <div className={styles.loadingContainer}>
        <SpinnerLoader text="Loading permissions..." />
      </div>
    );
  }

  // Block render until access is confirmed
  if (!hasAccess) {
    return null;
  }

  return <>{children}</>;
};
