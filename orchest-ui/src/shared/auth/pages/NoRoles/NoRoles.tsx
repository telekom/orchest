import { Button } from "@/design-system/components/ui/button";
import clsx from "clsx";
import React, { useCallback, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { UserRoles } from '../../models/roles';
import styles from './NoRoles.module.css';

export const NoRoles: React.FC = () => {
  const navigate = useNavigate();
  const { logout, isLoading, getEffectiveRoles, user } = useAuth();

  const checkAndRedirectIfHasRoles = useCallback(() => {
    if (!isLoading && user) {
        const effectiveRoles = getEffectiveRoles();

        const validRoles = effectiveRoles.filter(role =>
          Object.values(UserRoles).includes(role as UserRoles)
        );

        if (validRoles && validRoles.length > 0) {
          navigate('/processes', { replace: true });
          return true; // Redirection occurred
        }
    }
    return false; // No redirection occurred
  }, [getEffectiveRoles, navigate, isLoading, user]);

  useEffect(() => {
    const timer = setTimeout(() => {
      if (!isLoading) {
        checkAndRedirectIfHasRoles();
      }
    }, 100); // Short delay for more stable behavior

    return () => clearTimeout(timer);
  }, [checkAndRedirectIfHasRoles, isLoading, user]);

  return (
    <div className={styles.overlay}>
      <div className={styles.card}>
        <div className={styles.header}>
          <h2 className={styles.title}>
            No Role Permissions
          </h2>
          <p className={styles.description}>
            You haven't been assigned any roles required to access the application.
          </p>
        </div>

        <div className={styles.actions}>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => logout()}
            label="Sign Out"
            className={clsx(styles.logoutButton)}
          />
        </div>
      </div>
    </div>
  );
};

export default NoRoles;
