import { MOCK_ROLES_STORAGE_KEY, useAuth } from '@/shared/auth';
import { TelekomLogoMark } from '@/shared/components/OrchLogo';
import { environment, EVENT_NAMES, NAV_ITEMS, TIMING } from '@/shared/constants';
import { logger } from '@/shared/utils/logger';
import { Button } from '@/design-system/components/ui/button';
import clsx from 'clsx';
import React, { useEffect, useMemo, useState } from 'react';
import { useLocation, Link } from 'react-router-dom';
import { UserDropdown } from '../UserDropdown/UserDropdown';
import { createDebounceHandler, hasPermission } from '../utils/navbarUtils';
import styles from './Navbar.module.css';

const getEnvironmentBadgeClass = (mode: string): string => {
  return clsx(
    styles.envBadge,
    mode === 'development' ? styles.envBadgeDevelopment : styles.envBadgeOther
  );
};

export const Navbar: React.FC = () => {
  const location = useLocation();
  const { hasRole, rolesLoaded, isAuthenticated, getEffectiveRoles } = useAuth();
  const [, setRoleVersion] = useState(0);

  useEffect(() => {
    const { handler: handleRoleChange, cleanup: cleanupDebounce } = createDebounceHandler(
      () => setRoleVersion(prev => prev + 1),
      TIMING.ROLE_CHANGE_DEBOUNCE
    );

    const handleStorageChange = (event: StorageEvent) => {
      if (event.key === MOCK_ROLES_STORAGE_KEY) {
        handleRoleChange();
      }
    };

    window.addEventListener(EVENT_NAMES.ROLE_CHANGE, handleRoleChange as EventListener);
    window.addEventListener(EVENT_NAMES.ROLES_UPDATED, handleRoleChange as EventListener);
    window.addEventListener(EVENT_NAMES.STORAGE, handleStorageChange);

    return () => {
      cleanupDebounce();
      window.removeEventListener(EVENT_NAMES.ROLE_CHANGE, handleRoleChange as EventListener);
      window.removeEventListener(EVENT_NAMES.ROLES_UPDATED, handleRoleChange as EventListener);
      window.removeEventListener(EVENT_NAMES.STORAGE, handleStorageChange);
    };
  }, []);

  const visibleNavItems = useMemo(() => {
    // Defensive check: detect zombie session state
    // This occurs when token acquisition fails, clearing roles but leaving isAuthenticated true
    if (rolesLoaded && isAuthenticated) {
      const effectiveRoles = getEffectiveRoles();
      if (effectiveRoles.length === 0) {
        logger.error(
          '[Navbar] Auth state inconsistency detected: User authenticated but no roles available.',
          'This indicates a token acquisition failure. User may need to refresh or re-login.',
          { isAuthenticated, rolesLoaded, effectiveRolesCount: 0 }
        );
      }
    }

    return NAV_ITEMS.filter(item => hasPermission(item.requiredRole, hasRole))
      .map(item => ({ ...item, active: item.pathMatch(location.pathname) }));
  }, [hasRole, rolesLoaded, location.pathname, isAuthenticated, getEffectiveRoles]);

  return (
    <div className={styles.navbar}>
      <div className={styles.navbarInner}>
        <div className={styles.logoContainer}>
          <Link to="/dashboard" className={styles.logoLink}>
            <TelekomLogoMark size={24} />
            <h1 className={styles.logoText}>OrchesT</h1>
          </Link>

          {environment.mode !== 'prod' && (
            <span className={getEnvironmentBadgeClass(environment.mode)}>
              {environment.mode}
            </span>
          )}
        </div>

        <nav className={styles.nav}>
          {visibleNavItems.map((item) => {
            return (
              <Button
                key={item.name}
                asChild
                variant={item.active ? 'primary' : 'ghost'}
                size="sm"
                className={styles.navLink}
              >
                <Link to={item.href}>
                  {item.name}
                  {item.badge && <span className={styles.navBadge}>{item.badge}</span>}
                </Link>
              </Button>
            );
          })}
        </nav>

        <div className={styles.userSection}>
          {isAuthenticated && <UserDropdown collapsed={false} />}
        </div>
      </div>
    </div>
  );
};

export default Navbar;
